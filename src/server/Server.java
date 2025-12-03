package server;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*; //package for multithreading (ExecutorService, ThreadPool, ConcurrentHashMap)

import javax.swing.JOptionPane;

import server.Packet;
import server.ClientHandler;
import server.User;
import server.Message;

public class Server {
	//lists stored in mem for now 
	private List<User> users = new ArrayList<>();
	private List<DirectMessage> directChats = new ArrayList<>();
	private List<Group> groups = new ArrayList<>();
	//private List<Log> logs = new ArrayList<>();
	private List<Message> masterLog = new ArrayList<>(); // all msgs sent thru server
	
	private Boolean modified; //UPDATE TO TRUE ANY TIME ADDING MESSAGES TO directChats OR groups********
	private Boolean modified; //UPDATE TO TRUE ANY TIME ADDING MESSAGES TO directChats OR groups********
	private final String msgsFile = "AllChats.txt"; //filename to write messages to
	
	private ServerSocket serverSocket;
	
	//mutithreading and client management
	//threadpoool reusable group of threads managed by java
	private ExecutorService threadPool; //executorservice manage pool of threads to run tasks so dont need to create and manage threads manually
	
	//map storing which user connected on which socket
	//concurrenthashmap threadsafe version when mult threads edit at once
	//Using username as key since User objects don't have equals/hashCode
	private final Map<String, Socket> activeClients = new ConcurrentHashMap<>();
	//map storing ObjectOutputStreams for each client (must reuse, not recreate)
	private final Map<String, ObjectOutputStream> clientOutputStreams = new ConcurrentHashMap<>();
	//Using username as key since User objects don't have equals/hashCode
	private final Map<String, Socket> activeClients = new ConcurrentHashMap<>();
	//map storing ObjectOutputStreams for each client (must reuse, not recreate)
	private final Map<String, ObjectOutputStream> clientOutputStreams = new ConcurrentHashMap<>();
	
	
	//constructor
	public Server(int port) {
		modified = false;
		seedUsers();
		loadGroupsFromFile(); // Load groups and messages from file
		modified = false;
		seedUsers();
		loadGroupsFromFile(); // Load groups and messages from file
		try {
			serverSocket = new ServerSocket(port);
			
			//create pool of reusable threads for handling clients and
			//newcachedthreadpool is scalable and reuses threads
			threadPool = Executors.newCachedThreadPool();
			System.out.println("Server started on port: " + port);
		}catch(IOException e) {
			System.err.println("Error starting server on port " + port + ": " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to start server", e);
		}
	}

	private void seedUsers() {
		try {
			//try multiple possible locations for the file
			String projectRoot = System.getProperty("user.dir");
			System.out.println("Current working directory: " + projectRoot);
			File file = new File("All_Users.txt");
			System.out.println("Checking: " + file.getAbsolutePath() + " - exists: " + file.exists());
			if (!file.exists()) {
				//try in src directory
				file = new File("src/All_Users.txt");
				System.out.println("Checking: " + file.getAbsolutePath() + " - exists: " + file.exists());
			}
			if (!file.exists()) {
				//try in project root
				file = new File(projectRoot, "All_Users.txt");
				System.out.println("Checking: " + file.getAbsolutePath() + " - exists: " + file.exists());
			}
			if (!file.exists()) {
				//try in project root src directory
				file = new File(projectRoot, "src/All_Users.txt");
				System.out.println("Checking: " + file.getAbsolutePath() + " - exists: " + file.exists());
			}
			
			if (file.exists()) {
				System.out.println("Loading users from: " + file.getAbsolutePath());
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				int loadedCount = 0;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty()) {
						continue;
					}
					//format: username|password|admin(boolean)
					String[] parts = line.split("\\|");
					if (parts.length == 3) {
						String username = parts[0].trim();
						String password = parts[1].trim();
						boolean isAdmin = Boolean.parseBoolean(parts[2].trim());
						//prevent duplicate users
						boolean alreadyExists = false;
						for (User u : users) {
							if (u.getUsername().equals(username)) {
								alreadyExists = true;
								break;
							}
						}
						if (!alreadyExists) {
							users.add(new User(username, password, isAdmin));
							loadedCount++;
						}
					} else {
						System.out.println("WARNING: Skipping invalid user line (expected format: username|password|admin): " + line);
					}
				}
				br.close();
				System.out.println("Loaded " + loadedCount + " user(s) from file. Total users: " + users.size());
			} else {
				System.out.println("WARNING: All_Users.txt not found. No users loaded. Login will not work until users are created.");
				System.out.println("Searched in: " + new File("All_Users.txt").getAbsolutePath());
				System.out.println("Searched in: " + new File("src/All_Users.txt").getAbsolutePath());
			}
		} catch (IOException e) {
			System.err.println("ERROR: Failed to read users file: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public synchronized void saveUsersToFile() {
		//try multiple possible locations for the file
		File file = new File("All_Users.txt");
		if (!file.exists() && new File("src/All_Users.txt").exists()) {
			file = new File("src/All_Users.txt");
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for (User user : users) {
				String line = user.getUsername() + "|" + user.getPassword() + "|" + user.isAdmin();
				bw.write(line);
				bw.newLine();
			}
		} catch (IOException e) {
			//failed to save users
		}
	}
	//start server and accept clients
	public void startServer() {
		if (serverSocket == null) {
			throw new IllegalStateException("Server socket not initialized. Server failed to start.");
		}
		System.out.println("Waiting for client connections...");
		while(true) {
			try {
				//waits until client connects
				Socket socket = serverSocket.accept();
				System.out.println("New client connected: " + socket.getInetAddress());
				
				//make new clienthandler for this socket as it talks to one client
				ClientHandler handler = new ClientHandler(socket,this);
				
				//threadpool managed by java's pool instead of new thread(handler).start();
				//which creates a new thread every time (heavy, slower if many)
				//threadpool instead bc reuses threads instead of always creating new ones. limits how many threads run at once (prevents overload). easier to manage and shut down cleanly
				threadPool.execute(handler);
			}catch (IOException e) {
				break;
			}
		}
	}
	
	//communication helpers
	//like lets server send msg to all connected clients like gc message or server announcement
	public synchronized void broadcast(Packet packet) {
		//loop thru every connected clients output stream
		for(Map.Entry<String, ObjectOutputStream> entry : clientOutputStreams.entrySet()) {
		//loop thru every connected clients output stream
		for(Map.Entry<String, ObjectOutputStream> entry : clientOutputStreams.entrySet()) {
			try {
				ObjectOutputStream out = entry.getValue();
				if(out != null) {
					out.writeObject(packet);
					out.flush(); //send immediately
				}
				ObjectOutputStream out = entry.getValue();
				if(out != null) {
					out.writeObject(packet);
					out.flush(); //send immediately
				}
			}catch(IOException e) {
				//failed to send packet
			}
		}
	}
	
	//targetting packet to specific client
	public synchronized void sendToClient(User targetUser, Packet packet) {
		ObjectOutputStream out = clientOutputStreams.get(targetUser.getUsername());
		if(out != null) {
		ObjectOutputStream out = clientOutputStreams.get(targetUser.getUsername());
		if(out != null) {
			try {
				out.writeObject(packet);
				out.flush();
			}catch (IOException e) {
				//remove from maps if stream is broken
				clientOutputStreams.remove(targetUser.getUsername());
				activeClients.remove(targetUser.getUsername());
			}
		}
	}
	
	//add new client to activeClients map when they login
	public synchronized void registeredClient(User u, Socket s, ObjectOutputStream out) {
		activeClients.put(u.getUsername(), s);
		clientOutputStreams.put(u.getUsername(), out);
		System.out.println("SERVER: Registered client: " + u.getUsername() + " (Total active clients: " + activeClients.size() + ")");
	public synchronized void registeredClient(User u, Socket s, ObjectOutputStream out) {
		activeClients.put(u.getUsername(), s);
		clientOutputStreams.put(u.getUsername(), out);
		System.out.println("SERVER: Registered client: " + u.getUsername() + " (Total active clients: " + activeClients.size() + ")");
	}
	
	//remove client from list when they disconnect
	public synchronized void removeClient(User u) {
		activeClients.remove(u.getUsername());
		clientOutputStreams.remove(u.getUsername());
		activeClients.remove(u.getUsername());
		clientOutputStreams.remove(u.getUsername());
	}
	
	//login verification hceck if username and pass match any known user
	public synchronized boolean verifyLogin(String username, String password) {
		for(User u : users) {
			if(u.getUsername().equals(username) && u.getPassword().equals(password)) {
				return true; //found matching
			}
		}
		return false; //not found
	}
	
	//save load methods
	//not implemented yet
	public void saveLog() {
	}
	
	public void saveUser() {
	}
	
	public void stringToLog() {}
	public void stringToUser() {}
	public void createLog() {}
	
	//getters
	public List<User> getUsers(){
		return users;
	}
	
	public synchronized Optional<User> findUserByCredentials(String username, String password){
		if (users.isEmpty()) {
			System.out.println("WARNING: Attempted login but no users are loaded in the system.");
		}
		Optional<User> found = users.stream()
				.filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
				.findFirst();
		if (!found.isPresent()) {
			System.out.println("Login attempt failed for username: " + username + " (Total users in system: " + users.size() + ")");
		}
		return found;
	}
	
	public synchronized Optional<User> findUserByUsername(String username){
		return users.stream()
				.filter(u -> u.getUsername().equals(username))
				.findFirst();
	}
	
	public List<Message> getMasterLog(){
		return masterLog;
	}
	
	public synchronized List<Message> getAllMessagesByUser(String username) {
		List<Message> userMessages = new ArrayList<>();
		
		//search through all groups
		for (Group group : groups) {
			for (Message msg : group.getMessages()) {
				if (msg.getSender().equals(username)) {
					userMessages.add(msg);
				}
			}
		}
		
		//search through all direct messages
		for (DirectMessage dm : directChats) {
			for (Message msg : dm.getMessage()) {
				if (msg.getSender().equals(username)) {
					userMessages.add(msg);
				}
			}
		}
		
		//sort by timestamp (oldest first)
		Collections.sort(userMessages, (m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
		
		return userMessages;
	}
	
	public List<Group> getGroups() {
		return groups;
	}
	
	public List<DirectMessage> getDirectChats() {
		return directChats;
	}
	
	public synchronized Group getGroupById(int groupUID) {
		for (Group group : groups) {
			if (group.getGroupUID() == groupUID) {
				return group;
			}
		}
		return null;
	}
	
	public synchronized DirectMessage getDirectMessageById(int chatUID) {
		for (DirectMessage dm : directChats) {
			if (dm.getChatUID() == chatUID) {
				return dm;
			}
		}
		return null;
	}
	
	private boolean participantsMatch(List<String> list1, List<String> list2) {
		if (list1.size() != list2.size()) {
			return false;
		}
		List<String> sorted1 = new ArrayList<>(list1);
		List<String> sorted2 = new ArrayList<>(list2);
		Collections.sort(sorted1);
		Collections.sort(sorted2);
		return sorted1.equals(sorted2);
	}
	
	public synchronized Object findOrCreateGroup(List<String> participants, String sender, String messageText, LocalDateTime timestamp) {
		//check if exactly 2 participants (DirectMessage)
		if (participants.size() == 2) {
			//check existing direct messages
			for (DirectMessage dm : directChats) {
				if (participantsMatch(dm.getGroupUsers(), participants)) {
					return dm;
				}
			}
			//create new DirectMessage
			String recipient = participants.get(0).equals(sender) ? participants.get(1) : participants.get(0);
			Optional<User> senderUser = findUserByUsername(sender);
			Optional<User> recipientUser = findUserByUsername(recipient);
			if (senderUser.isPresent() && recipientUser.isPresent()) {
				DirectMessage newDM = new DirectMessage(senderUser.get(), recipientUser.get(), messageText);
				directChats.add(newDM);
				modified = true;
				return newDM;
			}
			return null;
		} else {
			//check existing groups
			for (Group group : groups) {
				if (participantsMatch(group.getGroupUsers(), participants)) {
					return group;
				}
			}
			//create new Group
			List<String> recipients = new ArrayList<>(participants);
			recipients.remove(sender);
			Group newGroup = new Group(sender, recipients, messageText, timestamp);
			groups.add(newGroup);
			modified = true;
			return newGroup;
		}
	}
	
	public synchronized List<Object> getGroupsForUser(String username) {
		List<Object> userGroups = new ArrayList<>();
		
		for (Group group : groups) {
			List<String> participants = group.getGroupUsers();
			if (participants.contains(username)) {
				//force read of all messages to ensure they're in memory before adding to list
				List<Message> msgs = group.getMessages();
				for (Message m : msgs) {
					m.getSender(); //force read
				}
				userGroups.add(group);
			}
		}
		
		for (DirectMessage dm : directChats) {
			List<String> participants = dm.getGroupUsers();
			if (participants.contains(username)) {
				//force read of all messages to ensure they're in memory before adding to list
				List<Message> msgs = dm.getMessage();
				for (Message m : msgs) {
					m.getSender(); //force read
				}
				userGroups.add(dm);
			}
		}
		
		return userGroups;
	}
	
	private void sortMessagesByTimestamp(Group group) {
		Collections.sort(group.getMessages(), (m1, m2) -> 
			m1.getTimestamp().compareTo(m2.getTimestamp()));
	}
	
	private void sortMessagesByTimestamp(DirectMessage dm) {
		Collections.sort(dm.getMessage(), (m1, m2) -> 
			m1.getTimestamp().compareTo(m2.getTimestamp()));
	}
	
	public synchronized void sortAllMessages() {
		for (Group group : groups) {
			sortMessagesByTimestamp(group);
		}
		for (DirectMessage dm : directChats) {
			sortMessagesByTimestamp(dm);
		}
	}
	
	public synchronized void addMessageToGroup(Group group, Message message) {
		group.getMessages().add(message);
		sortMessagesByTimestamp(group); //keep sorted
		group.sendNotification();
		modified = true;
		// Don't save to file here - do it asynchronously to avoid blocking serialization
		// saveGroupsToFile(); // Persist after update - moved to async
	}
	
	//async file save to avoid blocking
	public void saveGroupsToFileAsync() {
		new Thread(() -> {
			saveGroupsToFile();
		}).start();
	}
	
	public synchronized void addMessageToDirectMessage(DirectMessage dm, Message message) {
		dm.getMessage().add(message);
		sortMessagesByTimestamp(dm); //keep sorted
		dm.sendNotification();
		modified = true;
		// Don't save to file here - do it asynchronously to avoid blocking serialization
		// saveGroupsToFile(); // Persist after update - moved to async
	}
	
	//shutting down
	public void shutdown() {
		try {
			//stop all client threads
			threadPool.shutdownNow();
			
			//close socket so no connections
			serverSocket.close();
			
		}catch(IOException e) {
		}
	}

	public String toString(List<Message> msgs) {
	public String toString(List<Message> msgs) {
		String s = "";
		for (int i = 0; i < msgs.size()-1; i++) {
			s += msgs.get(i).toString() + "\n\n";
		}
		s += msgs.get(msgs.size()-1).toString();
		return s;
	}

	private void saveMsgs() {
	private void saveMsgs() {
		String buf = "";
		File file = new File(msgsFile);
		try {
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
		} catch (IOException e) {
			return;
		try {
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
		} catch (IOException e) {
			return;
		}
		try {
			FileWriter fw = new FileWriter(file);
			fw.write("-----DIRECT MESSAGES-----\n\n");
			for (int i = 0; i < directChats.size(); i++) {
				buf = directChats.get(i).toString();
				fw.write(buf + "\n\n");
			}
			fw.write("-----GROUP CHATS-----\n\n");
			for (int i = 0; i < groups.size(); i++) {
				buf = groups.get(i).toString();
				fw.write(buf + "\n\n");
			}
			if (!groups.isEmpty()) {
				buf = groups.get(groups.size() - 1).toString();
				fw.write(buf);
			}
			if (!groups.isEmpty()) {
				buf = groups.get(groups.size() - 1).toString();
				fw.write(buf);
			}
			fw.close();
		} catch (IOException e) {
		//todo auto-generated catch block
		//system.out.println("io filewriter error");
			JOptionPane.showMessageDialog(null, "IO filewriter error");
			//e.printstacktrace();
			return;
		}
		modified = false;
		modified = false;
	}

	public String loadData(String filename) {
		//note: msgsfile is final, cannot be reassigned
		//this method signature may need to be refactored
		String buf = "";
		try {
		File file = new File(filename);
		Scanner scan = new Scanner(file);
		while (scan.hasNextLine()) {
			buf += scan.nextLine() + '\n';
		}
		scan.close();
			return buf;
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "File not found!");
			return "\nLOADING ERROR\n";
		}
	}

	private void loadMsgs() {
		//todo: implement proper loading from file
		//this method needs to be refactored to properly deserialize group and directmessage objects
		//for now, leaving as placeholder to prevent compilation errors
		String s = loadData(msgsFile);
		if (s == null || s.isEmpty()) {
			return;
		}
		
		//parse file content - this is a simplified version
		//full implementation would need proper serialization/deserialization
		String[] parts = s.split("~");
		if (parts.length >= 4) {
			String dmsStr = parts[1];
			String groupsStr = parts[3];
			
			//parse direct messages
			String[] dmLines = dmsStr.split("\n");
			for (String line : dmLines) {
				if (line != null && !line.trim().isEmpty()) {
					//todo: deserialize directmessage from line
				}
			}
			
			//parse groups
			String[] groupLines = groupsStr.split("\n");
			for (String line : groupLines) {
				if (line != null && !line.trim().isEmpty()) {
					//todo: deserialize group from line
				}
			}
		}
		
		modified = false;
	}
	
	public synchronized void saveGroupsToFile() {
		// Try multiple possible locations for the file
		File file = new File("All_Messages.txt");
		if (!file.exists() && new File("src/All_Messages.txt").exists()) {
			file = new File("src/All_Messages.txt");
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			//save directmessages - one message per line
			for (DirectMessage dm : directChats) {
				for (Message msg : dm.getMessage()) {
					bw.write("MESSAGE|DM|" + dm.getChatUID() + "|");
					bw.write(msg.getTimestamp().toString() + "|");
					bw.write(msg.getSender() + "|");
					bw.write(msg.getMessage().replace("|", "\\|").replace("\n", "\\n") + "|");
					// Write recipients
					for (int i = 0; i < msg.getRecipients().size(); i++) {
						if (i > 0) bw.write(",");
						bw.write(msg.getRecipients().get(i));
					}
					bw.newLine();
				}
			}
			
			//save groups - one message per line
			for (Group group : groups) {
				for (Message msg : group.getMessages()) {
					bw.write("MESSAGE|GROUP|" + group.getGroupUID() + "|");
					bw.write(msg.getTimestamp().toString() + "|");
					bw.write(msg.getSender() + "|");
					bw.write(msg.getMessage().replace("|", "\\|").replace("\n", "\\n") + "|");
					// Write recipients
					for (int i = 0; i < msg.getRecipients().size(); i++) {
						if (i > 0) bw.write(",");
						bw.write(msg.getRecipients().get(i));
					}
					bw.newLine();
				}
			}
		} catch (IOException e) {
		}
	}
	
	private synchronized void loadGroupsFromFile() {
		//try multiple possible locations for the file
		File file = new File("All_Messages.txt");
		if (!file.exists()) {
			//try in src directory
			file = new File("src/All_Messages.txt");
		}
		if (!file.exists()) {
			return;
		}
		
		//map to store messages by group: "type|uid" -> list<message>
		Map<String, List<Message>> messagesByGroup = new HashMap<>();
		//map to store participants: "type|uid" -> set<string>
		Map<String, Set<String>> participantsByGroup = new HashMap<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			int messageCount = 0;
			int lineNumber = 0;
			while ((line = br.readLine()) != null) {
				lineNumber++;
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				if (!line.startsWith("MESSAGE|")) {
					continue;
				}
				
				String[] parts = line.split("\\|", -1);
				if (parts.length < 7) {
					continue;
				}
				
				try {
					String type = parts[1]; //"dm" or "group"
					int uid = Integer.parseInt(parts[2]);
					LocalDateTime timestamp = LocalDateTime.parse(parts[3]);
					String sender = parts[4];
					String messageText = parts[5].replace("\\|", "|").replace("\\n", "\n");
					String[] recipients = parts[6].split(",");
					
					List<String> recipientList = new ArrayList<>();
					for (String r : recipients) {
						if (!r.trim().isEmpty()) {
							recipientList.add(r.trim());
						}
					}
					
					Message msg = new Message(timestamp, messageText, sender, recipientList);
					
					//group key: "type|uid"
					String groupKey = type + "|" + uid;
					
					//add message to group
					if (!messagesByGroup.containsKey(groupKey)) {
						messagesByGroup.put(groupKey, new ArrayList<>());
						participantsByGroup.put(groupKey, new HashSet<>());
					}
					messagesByGroup.get(groupKey).add(msg);
					
					//track participants (sender + recipients)
					participantsByGroup.get(groupKey).add(sender);
					participantsByGroup.get(groupKey).addAll(recipientList);
					
					messageCount++;
				} catch (Exception e) {
				}
			}
			
			//reconstruct group and directmessage objects
			int groupCount = 0;
			for (Map.Entry<String, List<Message>> entry : messagesByGroup.entrySet()) {
				String groupKey = entry.getKey();
				List<Message> messages = entry.getValue();
				Set<String> participantSet = participantsByGroup.get(groupKey);
				
				String[] keyParts = groupKey.split("\\|");
				String type = keyParts[0];
				//uid is not used - new uids will be assigned
				
				List<String> participantList = new ArrayList<>(participantSet);
				
				//sort messages by timestamp
				Collections.sort(messages, (m1, m2) -> 
					m1.getTimestamp().compareTo(m2.getTimestamp()));
				
				if ("DM".equals(type)) {
					DirectMessage dm = new DirectMessage(participantList, messages);
					directChats.add(dm);
					groupCount++;
				} else if ("GROUP".equals(type)) {
					Group group = new Group(participantList, messages);
					groups.add(group);
					groupCount++;
				}
			}
			
		} catch (IOException e) {
		}
	}
	
	//driver
	public static void main(String[] args) {
		int port = 12345; //ex port number change when figure out which port using which client connects to
		
		//make new server listening on that port
		Server server = new Server(port);
		
		//start server waiting for clients forever till stopped
		server.startServer();
	}
	
}