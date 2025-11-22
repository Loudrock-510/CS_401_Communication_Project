package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*; //package for multithreading (ExecutorService, ThreadPool, ConcurrentHashMap)

import server.Packet;
import server.ClientHandler;
import server.User;
import server.Log;
import server.Message;

public class Server {
	//lists stored in mem for now
	private List<User> users = new ArrayList<>();
	private List<DirectMessage> directChats = new ArrayList<>();
	private List<GroupMessage> groupChats = new ArrayList<>();
	private List<Log> logs = new ArrayList<>();
	private List<Message> masterLog = new ArrayList<>(); // all msgs sent thru server
	
	private ServerSocket serverSocket;
	
	//mutithreading and client management
	//threadpoool reusable group of threads managed by java
	private ExecutorService threadPool; //executorservice manage pool of threads to run tasks so dont need to create and manage threads manually
	
	//map storing which user connected on which socket
	//concurrenthashmap threadsafe version when mult threads edit at once
	private final Map<User, Socket> activeClients = new ConcurrentHashMap<>();
	
	
	//constructor
	public Server(int port) {
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
	
	//driver
	public static void main(String[] args) {
		int port = 12345; //ex port number change when figure out which port using which client connects to
		
		//make new server istening on that port
		Server server = new Server(port);
		
		//start server waiting for clients forvever till stopped
		server.startServer();
	}
	
}
