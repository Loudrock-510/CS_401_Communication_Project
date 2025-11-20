package server;


import java.util.*;
import java.net.*;
import java.time.LocalDateTime;
import java.io.*;

public class Client {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		
		PacketHandler packetHandler = new PacketHandler();
		User myUser;
		Scanner sc= new Scanner(System.in); //System.in is a standard input stream. 
		
		
		//************************************************************
		//			CONNECT TO SERVER
		// uses port 1776 and grab host from device
		//
		//************************************************************
		//STUB: REMOVED ALL CONSOLE AND MADE STATIC PORT AND GRAB HOST.
		int port = 1776;
		String host = InetAddress.getLocalHost().getHostName();
		// Connect to the ServerSocket at host:port
		Socket socket = new Socket(host, port);
		System.out.println("Connected to " + host + ":" + port);
		
		//************************************************************
		//				OPEN INPUT AND OUTPUT STREAM
		// 				AND LOOPS EXPECTING A PACKAGE
		//	
		//************************************************************
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		//STUB: NEEDS TO BE ITS OWN THREAD
		while (true) {
			Packet packet = (Packet) in.readObject();
			packetHandler.handlePacket(packet);
			
		}
		//************************************************************
		//						SENDING MESSAGE
		//				
		//
		//************************************************************
		List<Message> newMessageList = new ArrayList<>();
		String sender = myUser.getUsername();
		List<String> recipients = new ArrayList<>();
		recipients.add("");
		String textToBeSent;
		Message newMessge = new Message(LocalDateTime.now(),textToBeSent,sender,recipients);
		newMessageList.add(newMessge);
		Packet newMessageRequest = new Packet(Type.MESSAGES,"REQUEST",List.of(newMessageList));
		
		//verify new message and send the same message back to add to chat
		
		//************************************************************
		//						SENDING LOGIN
		//			used for sending server login info to verify
		//
		//************************************************************
		// create list to send
		List<LoginInfo> logData = new ArrayList<>(); 
	    String username;
	    String password;
	    //create create info you want to send
	    LoginInfo myInfo = new LoginInfo(username, password);
	    //add info to list
	    logData.add(myInfo);
	    //create packet
		Packet loginInfo = new Packet(Type.LOGIN, "DEFAULT",List.of(logData)) ;
		//send packet
		out.writeObject(loginInfo);
		//clears
		out.flush(); 
		
		// add server sending a user.
		//************************************************************
		//						SENDING LOGOUT
		//
		//
		//************************************************************
		List<LoginInfo> empty = new ArrayList<>();
		Packet logoutRequest = new Packet(Type.LOGOUT, "REQUEST",List.of(empty)) ;
		
		//************************************************************
		//						SENDING NEW USER
		//						for creating a new user
		//						only accessible by Admin/IT
		//************************************************************
		List<User> users = new ArrayList<>();
		String newUsername;
		String newPassword;
		boolean isAdmin;
		User newUser = new User(newUsername,newPassword,isAdmin);
		users.add(newUser);
		Packet newUserRequest = new Packet(Type.USERS, "REQUEST",List.of(users)) ;
		// ask server to verify if exist or not and create user
		 
		
		
		
		
		
		
		
		
		
		
		
		
		 Packet login = (Packet) in.readObject(); //STUB: casting ok here??
		 if (login.getType() != Type.LOGIN) {
			 System.out.println("Server gave invalid packet type for login...");
			 throw new IOException("Server gave invalid packet type for login...");
		 }
		 if (!login.getStatus().equals("success")) {  //MUST EQUAL "success"!
			 System.out.println("Login unsuccessful");
			 throw new IOException("Login unsuccessful");
		 }
		 Packet newM;
		 String msg;
		 List<Object> messages = new ArrayList<Object>();
		 while(true) {
			 
			 //INSERT CHAT GUI CODE BELOW
			 
		System.out.print("Enter Packet info. Type logout to quit\n");
		msg = sc.next();
		//Packets.add(new Packet(msg, Type.TEXT));
		newM = new Packet(Type.MESSAGES, msg, messages);
		objectOutputStream.writeObject(newM);
		newM = (Packet) in.readObject();
		if (newM.getStatus().equals("LOGOUT"))
			newM = new Packet(Type.LOGOUT, "", null);
		 if (newM.getType() == Type.LOGOUT) {
			 System.out.println("Logging out...");
			 objectOutputStream.writeObject(new Packet(Type.LOGOUT, "logout", null));
			 break;
		 }
		 else if (newM.getType() == Type.MESSAGES) {
			 System.out.println("New Message: " + newM.getcontent().getFirst()); //LATER - SHOW IN GUI
		 }
		 
		}
		System.out.println("Client successfully logged out");
		System.out.println("Closing socket");
		socket.close();
	}
}