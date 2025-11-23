package server;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*; //package for multithreading (ExecutorService, ThreadPool, ConcurrentHashMap)

import server.User;

public class ClientHandler implements Runnable{
	
	private Socket socket;
	private Server server;
	//threadpool
	private ExecutorService threadPool = Executors.newCachedThreadPool();

	//map
	private Map<String, Socket> clientSockets = new ConcurrentHashMap<>();
	private Map<String, ObjectInputStream> clientInput = new ConcurrentHashMap<>();
	private Map<String, ObjectOutputStream> clientOut = new ConcurrentHashMap<>();
	
	private User concurrentUser; //user attached to this handler
	
	//constructor
	public ClientHandler(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
	}
	
	@Override
	public void run() {
		startManager();
	}
	
	public void startManager(){
		try{
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			
			private User loggedInUser = null; //after login becomes non null
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
