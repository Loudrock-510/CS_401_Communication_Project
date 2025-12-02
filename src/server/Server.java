package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*; //package for multithreading (ExecutorService, ThreadPool, ConcurrentHashMap)

import javax.swing.JOptionPane;

import server.Packet;
import server.ClientHandler;
import server.User;
import server.Log;
import server.Message;

public class Server {
	//lists stored in mem for now 
	private List<User> users = new ArrayList<>();
	private List<DirectMessage> directChats = new ArrayList<>();
	private List<Group> groups = new ArrayList<>();
	private List<Log> logs = new ArrayList<>();
	private List<Message> masterLog = new ArrayList<>(); // all msgs sent thru server
	
	private Boolean chatsModified; //UPDATE TO TRUE ANY TIME ADDING MESSAGES TO directChats OR groups********
	private final String msgsFile = "AllChats.txt"; //filename to write messages to
	
	private ServerSocket serverSocket;
	
	//mutithreading and client management
	//threadpoool reusable group of threads managed by java
	private ExecutorService threadPool; //executorservice manage pool of threads to run tasks so dont need to create and manage threads manually
	
	//map storing which user connected on which socket
	//concurrenthashmap threadsafe version when mult threads edit at once
	private final Map<User, Socket> activeClients = new ConcurrentHashMap<>();
	
	
	//constructor
	public Server(int port) {
		chatsModified = false;
		try {
			serverSocket = new ServerSocket(port);
			
			//create pool of reusable threads for handling clients and 
			//newCachedThreadPool is scalable and reuses threads
			threadPool = Executors.newCachedThreadPool();
			System.out.println("Started on port: " + port);
		}catch(IOException e) {
			System.err.println("Error starting server: " + e.getMessage());
;		}
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
				System.err.println("Connection err: " + e.getMessage());
				break;
			}
		}
	}
	
	//communication helpers
	//like lets server send msg to all connected clients like gc message or server announcement
	public synchronized void broadcast(Packet packet) {
		//loop thru every connected clients socket 
		//grab the value key pair of client
		for(Socket s : activeClients.values()) {
			try {
				//create stream
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				
				//send packet like a msg
				out.writeObject(packet);
				out.flush(); //send immediately
			}catch(IOException e) {
				System.err.println("failed to send packet: " + e.getMessage());
			}
		}
	}
	
	//*********
	//targetting packet to specific client
	public synchronized void sendToClient(User targetUser, Packet packet) {
		Socket s = activeClients.get(targetUser);
		if(s != null && !s.isClosed()) {
			try {
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				out.writeObject(packet);
				out.flush();
			}catch (IOException e) {
				System.err.println("Failed to send packet to: " + targetUser.getUsername());
			}
		}
	}
	
	//add new client to activeClients map when they login
	public synchronized void registeredClient(User u, Socket s) {
		activeClients.put(u,s);
		System.out.println("Registered: " + u.getUsername());
	}
	
	//remove client from list when they disconnect
	public synchronized void removeClient(User u) {
		activeClients.remove(u); //removes their socket key value pair
		System.out.println("Disconnected: " + u.getUsername());
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
		System.out.println("Saving logs...");
	}
	
	public void saveUser() {
		System.out.println("Saving users...");
	}
	
	public void stringToLog() {}
	public void stringToUser() {}
	public void createLog() {}
	
	private Group stringToGroup(String s) {
		//String sMsgs = s.split('~')[2]
		List<Message> msgs = null;
		List<String> users = null;
		//for(int i = 0; i < )
		msgs.add(null);
		Group g = new Group(users, msgs);
		return null; //STUB: FINISH
	}
	private DirectMessage stringToDirMsg(String s) {
		return null; //STUB: FINISH
	}
	
	public Log getLog() {return null;}
	public List<Log> getLogs(String UID){return null;}
	public Log ViewUserLog(String username) {return null;}
	
	//getters
	public List<User> getUsers(){
		return users;
	}
	
	public List<Message> getMasterLog(){
		return masterLog;
	}
	
	//shutting down
	public void shutdown() {
		try {
			//stop all client threads
			threadPool.shutdownNow();
			
			//close socket so no connections
			serverSocket.close();
			
			System.out.println("shutting down server...");
		}catch(IOException e) {
			System.err.println("err shutting down server: " + e.getMessage());
		}
	}

	public String msgListToString(List<Message> msgs) {
		String s = "";
		for (int i = 0; i < msgs.size()-1; i++) {
			s += msgs.get(i).toString() + "\n\n";
		}
		s += msgs.get(msgs.size()-1).toString();
		return s;
	}

	private void saveMsgs() throws IOException {
		String buf = "";
		File file = new File(msgsFile);
		if(file.delete()) 
    			file.createNewFile();
		else {
    		throw new IOException("File couldn't be deleted!");
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
			buf = groups.get(groups.size()-1).toString();
			fw.write(buf);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//System.out.println("IO filewriter error");
			JOptionPane.showMessageDialog(null, "IO filewriter error");
			//e.printStackTrace();
			return;
		}
		chatsModified = false;
	}

	public String loadData(String filename) {
		//msgsFile = filename;
		String buf = "";
		try {
		File file = new File(filename);
		Scanner scan = new Scanner(file);
		while (scan.hasNextLine()) {
			buf += scan.nextLine() + '\n';
		}
		buf.trim();
		scan.close();
			return buf;
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "File not found!");
			return "\nLOADING ERROR\n";
		}
	}

	private void loadMsgs() {
		String s = loadData(msgsFile);
		String dms = s.split("~")[1];
		String fGroups = s.split("~")[3]; //file groups
		int lastIn = 0;
		
		for (int i = 0; i < (dms.length() - dms.replace("\n", "").length()); i++) {
			directChats.set(i, stringToDirMsg(dms.split("\n")[i]));
			lastIn = i;
		}
		if (lastIn > directChats.size() - 1) {
			for (int i = 0; i < directChats.size(); i++) {
				directChats.remove(i);
			}
		}
		for (int i = 0; i < (fGroups.length() - fGroups.replace("\n", "").length()); i++) {
			groups.set(i, stringToGroup(fGroups.split("\n")[i]));
			lastIn = i;
		}
		if (lastIn > groups.size() - 1) {
			for (int i = 0; i < groups.size(); i++) {
				groups.remove(i);
			}
		}
		
		chatsModified = false;
	}
	
	private Boolean msgsSorted(List<Message> msgs) {
		for (int i = 0; i < msgs.size()-1; i++) {
			if (msgs.get(i).getTimestamp().compareTo(msgs.get(i+1).getTimestamp()) > 0)
				return false;
		}
		return true;
	}
	
	private void sortMsgs(List<Message> msgs) {
		if (msgs.size() < 2)
			return;
		if (msgs.size() == 2) {
			if (msgs.get(0).getTimestamp().compareTo(msgs.get(1).getTimestamp()) > 0) {
				Message temp = msgs.get(1);
				msgs.set(1, msgs.get(0));
				msgs.set(0, temp);
				return;
			}
			else if (msgs.get(0).getTimestamp().compareTo(msgs.get(1).getTimestamp()) == 0) {
				return;
			}
			else //first comes before second; do nothing
				return;
		}
		
		//msgs.size() > 2, so:
		int newLow = 0;
		int index = 0;
		Boolean swapped = false;
		while(!msgsSorted(msgs)) {
		for (int i = index; i < msgs.size(); i++) {
			if (msgs.get(i).getTimestamp().compareTo(msgs.get(newLow).getTimestamp()) < 0) {
				newLow = i;
				swapped = true;
			}
		}
		Message temp = msgs.get(index);
		msgs.set(index, msgs.get(newLow));
		msgs.set(newLow, temp); 
		index++; //skip the swapped DVD next time
		if (index >= msgs.size())
			return;
		swapped = false;

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