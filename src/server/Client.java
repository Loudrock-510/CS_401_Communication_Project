package server;

import java.util.*;
import java.net.*;
import java.time.LocalDateTime;
import java.io.*;

public class Client {

	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	private Boolean loggedIn; // true if login is successful
	User myUser;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		PacketHandler packetHandler = new PacketHandler();
		User myUser;
		// ************************************************************
		// CONNECT TO SERVER
		// uses port 12345 and grab host from device
		// ************************************************************
		// STUB: REMOVED ALL CONSOLE AND MADE STATIC PORT AND GRAB HOST.
		int port = 12345;
		String host = InetAddress.getLocalHost().getHostName();
		// Connect to the ServerSocket at host:port
		Socket socket = new Socket(host, port);
		System.out.println("Connected to " + host + ":" + port);
		// ************************************************************
		// OPEN INPUT AND OUTPUT STREAM
		// AND LOOPS EXPECTING A PACKAGE
		// ************************************************************
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		Thread listener = new Thread(() -> {
			try {
				while (true) {
					Packet packet = (Packet) in.readObject();
					packetHandler.handlePacket(packet);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		listener.setDaemon(true); // closes when main ends (optional)
		listener.start();

		// Main GUI loop 
		while (true) {
			// GUI logic
			// out.writeObject(...); // send user actions
		}
	}

	// METHODS

	// ************************************************************
	// SENDING NEW USER
	// for creating a new user
	// only accessible by Admin/IT
	// ************************************************************
	public void sendUser(String newUsername, String newPassword, boolean isAdmin) throws IOException {
		List<User> users = new ArrayList<>();
		User newUser = new User(newUsername, newPassword, isAdmin);
		users.add(newUser);
		Packet newUserRequest = new Packet(Type.USERS, "REQUEST", List.of(users));
		// send packet
		out.writeObject(newUserRequest);
		// clears
		out.flush();
	}

	// ************************************************************
	// SENDING MESSAGE
	// make recipient list and add before calling
	//
	// ************************************************************
	public void sendMessage(List<String> recipients, String textToBeSent) throws IOException {
		List<Message> newMessageList = new ArrayList<>();
		String sender = myUser.getUsername();
		Message newMessge = new Message(LocalDateTime.now(), textToBeSent, sender, recipients);
		newMessageList.add(newMessge);
		Packet newMessageRequest = new Packet(Type.MESSAGES, "REQUEST", List.of(newMessageList));
		// send packet
		out.writeObject(newMessageRequest);
		// clears
		out.flush();
	}// verify new message and send the same message back to add to chat
	

	// ************************************************************
	// SENDING LOGIN
	// used for sending server login info to verify
	//
	// ************************************************************
	public static void sendLogin(String username, String password) throws IOException {
		// create list to send
		List<LoginInfo> logData = new ArrayList<>();
		// create create info you want to send
		LoginInfo myInfo = new LoginInfo(username, password);
		// add info to list
		logData.add(myInfo);
		// create packet
		Packet loginInfo = new Packet(Type.LOGIN, "DEFAULT", List.of(logData));
		// send packet
		out.writeObject(loginInfo);
		// clears
		out.flush();
	}// add server sending a user.
	// ************************************************************
	// SENDING LOGOUT
	//
	//
	// ************************************************************
	public static void sendLogout() throws IOException {
		List<LoginInfo> empty = new ArrayList<>();
		Packet logoutRequest = new Packet(Type.LOGOUT, "REQUEST", List.of(empty));
			// send packet
		out.writeObject(logoutRequest);
		// clears
		out.flush();
	}
}