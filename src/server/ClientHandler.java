package server;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*; //package for multithreading (ExecutorService, ThreadPool, ConcurrentHashMap)

import server.User;
import server.Packet;

public class ClientHandler implements Runnable{
	
	private Socket socket;
	private Server server;
	
	private ObjectInputStream in; // read packet frm client
	private ObjectOutputStream out; //send packet to client
	
	private User loggedInUser = null; //after login becomes non null
	
	//maps for clientsocket clientinput clientoutput
	private Map<String, Socket> clientSockets = new ConcurrentHashMap<>();
	private Map<String, ObjectInputStream> clientInput = new ConcurrentHashMap<>();
	private Map<String, ObjectOutputStream> clientOutput = new ConcurrentHashMap<>();
	
	//threadpool
	private static ExecutorService threadPool = Executors.newCachedThreadPool();
	
	private PacketHandler handler; // each client get their own handler
	
	
	
	//constructor
	public ClientHandler(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		this.handler = new PacketHandler(); //server side packet handler
	}
	
	@Override
	public void run() {
		try{
			//create stream
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.out.flush();
			this.in = new ObjectInputStream(socket.getInputStream());
			
			//MAIN LISTEN LOOP
			while(true) {
				Packet packet = (Packet) in.readObject();
				
				//hand packet to packethandler
				handler.handle(packet,this);
			}
			
		}catch(ClassNotFoundException| IOException e) {
			System.out.println("Client disconnected");
		}
	}
	
	//call AFTER successful login to register client
	public void registerClient(User user, Socket s){
		loggedInUser = user;
		
		clientSockets.put(user.getUsername(), s);
		clientInput.put(user.getUsername(), in);
		clientOutput.put(user.getUsername(),out);
		
		System.out.println("Registered Client: " + user.getUsername());
	}
	
	//remove client on logout or disconnect
	public void disconnectClient(User user) {
		clientSockets.remove(user.getUsername());
		clientInput.remove(user.getUsername());
		clientOutput.remove(user.getUsername());
		
		System.out.println("Disconnected User: " + user.getUsername());
	}
	
	//get output stream for user
	public OutputStream getOutputStream(User user) {
		return clientOutput.get(user.getUsername());
	}
	
	//get input stream for user
	public InputStream getInputStream(User user) {
		return clientInput.get(user.getUsername());
	}
	
	//SEND PACKET TO THIS CLIENT
	public void send(Packet packet) {
		try {
			out.writeObject(packet);
			out.flush();
		}catch(IOException e) {
			System.out.println("failed to send packet");
		}
	}
	
	public User getLoggedInUser() {
		return loggedInUser;
	}
	
}
