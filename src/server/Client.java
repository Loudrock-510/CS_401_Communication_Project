package server;


import java.util.*;
import java.net.*;
import java.io.*;

public class Client {
	private ObjectOutputStream objectOutputStream;
	private ObjectInputStream objectInputStream;
	private Boolean loggedIn; //true if login is successful
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Scanner sc= new Scanner(System.in); //System.in is a standard input stream. STUB: REPLACE W/ GUI
		System.out.print("Enter the port number to connect to: ");
		int port = sc.nextInt();
		System.out.print("Enter the host address to connect to: <localhost> ");
		String host = sc.next();
		// Connect to the ServerSocket at host:port
		Socket socket = new Socket(host, port);
		System.out.println("Connected to " + host + ":" + port);
		List<Object> creds = new ArrayList<Object>(); //creds for credentials
		Login.main(null);
		creds.add("username"); //later, extract from gui
		creds.add("password");
		Packet p = new Packet(Type.LOGIN, "pending", creds); //pending; server must validate credentials
		// Output stream socket
		OutputStream outputStream = socket.getOutputStream();
		// Create object output stream from the output stream to send an object through it
		objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(p);
		
		//verify login from server
		 InputStream is = socket.getInputStream();
		 objectInputStream = new ObjectInputStream(is);
		 Packet login = (Packet) objectInputStream.readObject(); //STUB: casting ok here??
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
		newM = (Packet) objectInputStream.readObject();
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
	
	public void signIn(Packet pack) { //STUB: plug into while loop
		if (pack.getType != Type.LOGIN) {
			throw new Exception("signIn() had invalid type!");
			return;
		}
		objectOutputStream.writeObject(pack); //send login request
		
		Packet validation = (Packet) objectInputStream.readObject();
		if (validation.getType() != Type.LOGIN) {
			throw new Exception("Server sent invalid type!");
			return;
		}
		if (!validation.getStatus().equals("success")) {
			throw new Exception("Login unsuccessful");
			return;
		}
		loggedIn = true;
	}
}
