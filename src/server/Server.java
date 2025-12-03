package server;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*; //package for multithreading (ExecutorService, ThreadPool, ConcurrentHashMap)

import javax.swing.JOptionPane;

import server.Packet;
import server.ClientHandler;
import server.User;
//import server.Log;
import server.Message;

public class Server {
	//lists stored in mem for now 
	private List<User> users = new ArrayList<>();
	private List<DirectMessage> directChats = new ArrayList<>();
	private List<Group> groups = new ArrayList<>();
	//private List<Log> logs = new ArrayList<>();
	private List<Message> masterLog = new ArrayList<>(); // all msgs sent thru server
	
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
	
	
	//constructor
	public Server(int port) {
		modified = false;
		seedUsers();
		loadGroupsFromFile(); // Load groups and messages from file
		try {
			serverSocket = new ServerSocket(port);
			
			//create pool of reusable threads for handling clients and 
			//newCachedThreadPool is scalable and reuses threads
			threadPool = Executors.newCachedThreadPool();
			if (port == 12345) {
				System.out.println("Started on port: 12345");
			}
		}catch(IOException e) {
			// Error starting server
		}
	}

	private void seedUsers() {
		try {
			// Try multiple possible locations for the file
			File file = new File("All_Users.txt");
			if (!file.exists()) {
				// Try in src directory
				file = new File("src/All_Users.txt");
			}
			
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty()) {
						continue;
					}
					// Format: username|password|admin(boolean)
					String[] parts = line.split("\\|");
					if (parts.length == 3) {
						String username = parts[0].trim();
						String password = parts[1].trim();
						boolean isAdmin = Boolean.parseBoolean(parts[2].trim());
						// Prevent duplicate users
						boolean alreadyExists = false;
						for (User u : users) {
							if (u.getUsername().equals(username)) {
								alreadyExists = true;
								break;
							}
						}
						if (!alreadyExists) {
							users.add(new User(username, password, isAdmin));
						}
					}
				}
				br.close();
			}
		} catch (IOException e) {
			// Failed to read users
		}
	}
	
	public synchronized void saveUsersToFile() {
		// Try multiple possible locations for the file
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
			// Failed to save users
		}
	}
	//start server and accept clients
	public void startServer() {
		System.out.println("Waiting for client connections...");
		while(true) {
			try {
				//waits until client connects
				Socket socket = serverSocket.accept();
				System.out.println("New client connected: " + socket.getInetAddress());
				
				//make new clienthandler for this socket as it talks to one client
				ClientHandler handler = new ClientHandler(socket,this);
				
				//threadpool managed by java's pool instead of new Thread(handler).start();
				// which creates a new thread every time (heavy, slower if many)
				//threadpool instead bc Reuses threads instead of always creating new ones. Limits how many threads run at once (prevents overload). Easier to manage and shut down cleanly
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
			try {
				ObjectOutputStream out = entry.getValue();
				if(out != null) {
					out.writeObject(packet);
					out.flush(); //send immediately
				}
			}catch(IOException e) {
				// Failed to send packet
			}
		}
	}
	
	//targetting packet to specific client
	public synchronized void sendToClient(User targetUser, Packet packet) {
		ObjectOutputStream out = clientOutputStreams.get(targetUser.getUsername());
		if(out != null) {
			try {
				out.writeObject(packet);
				out.flush();
			}catch (IOException e) {
				// Remove from maps if stream is broken
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
	}
	
	//remove client from list when they disconnect
	public synchronized void removeClient(User u) {
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
	//public Log getLog() {return null;}
	//public List<Log> getLogs(String UID){return null;}
	//public Log ViewUserLog(String username) {return null;}
	
	//getters
	public List<User> getUsers(){
		return users;
	}
	
	public synchronized Optional<User> findUserByCredentials(String username, String password){
		return users.stream()
				.filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
				.findFirst();
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
		
		// Search through all groups
		for (Group group : groups) {
			for (Message msg : group.getMessages()) {
				if (msg.getSender().equals(username)) {
					userMessages.add(msg);
				}
			}
		}
		
		// Search through all direct messages
		for (DirectMessage dm : directChats) {
			for (Message msg : dm.getMessage()) {
				if (msg.getSender().equals(username)) {
					userMessages.add(msg);
				}
			}
		}
		
		// Sort by timestamp (oldest first)
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
		// Check if exactly 2 participants (DirectMessage)
		if (participants.size() == 2) {
			// Check existing direct messages
			for (DirectMessage dm : directChats) {
				if (participantsMatch(dm.getGroupUsers(), participants)) {
					return dm;
				}
			}
			// Create new DirectMessage
			String recipient = participants.get(0).equals(sender) ? participants.get(1) : participants.get(0);
			DirectMessage newDM = new DirectMessage(sender, recipient, messageText, timestamp);
			directChats.add(newDM);
			modified = true;
			return newDM;
		} else {
			// Check existing groups
			for (Group group : groups) {
				if (participantsMatch(group.getGroupUsers(), participants)) {
					return group;
				}
			}
			// Create new Group
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
				// Force read of all messages to ensure they're in memory before adding to list
				List<Message> msgs = group.getMessages();
				for (Message m : msgs) {
					m.getSender(); // Force read
				}
				userGroups.add(group);
			}
		}
		
		for (DirectMessage dm : directChats) {
			List<String> participants = dm.getGroupUsers();
			if (participants.contains(username)) {
				// Force read of all messages to ensure they're in memory before adding to list
				List<Message> msgs = dm.getMessage();
				for (Message m : msgs) {
					m.getSender(); // Force read
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
	
	/**
	 * Sorts all messages in all groups and direct messages
	 */
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
		sortMessagesByTimestamp(group); // Keep sorted
		group.sendNotification();
		modified = true;
		// Don't save to file here - do it asynchronously to avoid blocking serialization
		// saveGroupsToFile(); // Persist after update - moved to async
	}
	
	// Async file save to avoid blocking
	public void saveGroupsToFileAsync() {
		new Thread(() -> {
			saveGroupsToFile();
		}).start();
	}
	

	public synchronized void addMessageToDirectMessage(DirectMessage dm, Message message) {
		dm.getMessage().add(message);
		sortMessagesByTimestamp(dm); // Keep sorted
		dm.sendNotifcation();
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
		String s = "";
		for (int i = 0; i < msgs.size()-1; i++) {
			s += msgs.get(i).toString() + "\n\n";
		}
		s += msgs.get(msgs.size()-1).toString();
		return s;
	}

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
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//System.out.println("IO filewriter error");
			JOptionPane.showMessageDialog(null, "IO filewriter error");
			//e.printStackTrace();
			return;
		}
		modified = false;
	}

	public String loadData(String filename) {
		// Note: msgsFile is final, cannot be reassigned
		// This method signature may need to be refactored
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
	
		String s = loadData(msgsFile);
		if (s == null || s.isEmpty()) {
			return;
		}
		
		// Parse file content - this is a simplified version
		// Full implementation would need proper serialization/deserialization
		String[] parts = s.split("~");
		if (parts.length >= 4) {
			String dmsStr = parts[1];
			String groupsStr = parts[3];
			
			// Parse direct messages
			String[] dmLines = dmsStr.split("\n");
			for (String line : dmLines) {
				if (line != null && !line.trim().isEmpty()) {
					// TODO: Deserialize DirectMessage from line
				}
			}
			
			// Parse groups
			String[] groupLines = groupsStr.split("\n");
			for (String line : groupLines) {
				if (line != null && !line.trim().isEmpty()) {
					// TODO: Deserialize Group from line
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
			// Save DirectMessages - one message per line
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
			
			// Save Groups - one message per line
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
		// Try multiple possible locations for the file
		File file = new File("All_Messages.txt");
		if (!file.exists()) {
			// Try in src directory
			file = new File("src/All_Messages.txt");
		}
		if (!file.exists()) {
			return;
		}
		
		// Map to store messages by group: "TYPE|UID" -> List<Message>
		Map<String, List<Message>> messagesByGroup = new HashMap<>();
		// Map to store participants: "TYPE|UID" -> Set<String>
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
					String type = parts[1]; // "DM" or "GROUP"
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
					
					// Group key: "TYPE|UID"
					String groupKey = type + "|" + uid;
					
					// Add message to group
					if (!messagesByGroup.containsKey(groupKey)) {
						messagesByGroup.put(groupKey, new ArrayList<>());
						participantsByGroup.put(groupKey, new HashSet<>());
					}
					messagesByGroup.get(groupKey).add(msg);
					
					// Track participants (sender + recipients)
					participantsByGroup.get(groupKey).add(sender);
					participantsByGroup.get(groupKey).addAll(recipientList);
					
					messageCount++;
				} catch (Exception e) {
				}
			}
			
			// Reconstruct Group and DirectMessage objects
			int groupCount = 0;
			for (Map.Entry<String, List<Message>> entry : messagesByGroup.entrySet()) {
				String groupKey = entry.getKey();
				List<Message> messages = entry.getValue();
				Set<String> participantSet = participantsByGroup.get(groupKey);
				
				String[] keyParts = groupKey.split("\\|");
				String type = keyParts[0];
				// UID is not used - new UIDs will be assigned
				
				List<String> participantList = new ArrayList<>(participantSet);
				
				// Sort messages by timestamp
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
		
		//make new server istening on that port
		Server server = new Server(port);
		
		//start server waiting for clients forvever till stopped
		server.startServer();
	}
	
}