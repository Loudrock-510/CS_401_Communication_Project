
package server;

import java.util.ArrayList;
import java.util.List;

public class PacketHandler {
    private Client client;
    private Server server;
    public PacketHandler() {
    }
    
    /*
     * Place this code in client
    private PacketHandler handler = new PacketHandler(this);

 	// example code for handling when a packet arrives:
 	handler.handle(packet);
    */
    
    
    /*
     * ********************************************************
     * PACKET HANDLERS
     * 
     * Returns proper return type depending on the type 
     * attribute of the Packet object
     * ********************************************************
     */
    //THIS ALL GOES IN CLIENT AND SERVER
    public void handlePacket(Packet packet) {
        switch (packet.getType()) {
            case USERS -> handleUsers(packet);
            case MESSAGES -> handleMessages(packet);
            case LOGIN -> handleLogin(packet);
            case ERROR -> handleError(packet);
            case LOGOUT -> handleLogout(packet);
            
            //remove later. used for debugging
            default -> System.out.println("Unknown packet type: " + packet.getType());
        }
    }
    
    private Object handleLogout(Packet packet) {
        // close thread
    	//************************
    	//Test console print
    	System.out.println("SERVER: Recevied LOGOUT Packet");
       return null;
    }

    //added handle method public so it can grab handlePacket method since its private
    public void handle(Packet packet, ClientHandler handler) {
    	handlePacket(packet);
    }
    
    /*
     * *********************************************************
     * USER PACKET HANDLER
     * 
     * returns one user or a list of users 
     * based on the content of the packet
     * ********************************************************
     */
    private Object handleUsers(Packet packet) {
        List<Object> content = packet.getcontent();
      //*************************************
    	//Test console print
        System.out.println("SERVER: Received USERS packet");
        if (content.isEmpty()) {
        	//remove later. used for debugging
            System.out.println("No users in packet.");
            return null;
        }

        if (content.size() == 1) {
            User user = (User) content.get(0);
            return user; // return the single user directly
        } else {
            List<User> users = new ArrayList<>();
            for (Object obj : content) {
                User u = (User) obj; // may need to change depending on user implentation
                users.add(u);
            }
            return users; // return the list of users
        }
        
        

    }
    /*
     * *****************************************************
     * MESSAGE PACKET HANDLER
     * 
     * returns one message string or a list of strings 
     * depending on the content of the packet
     * *****************************************************
     */
    private Object handleMessages(Packet packet) {
        List<Object> content = packet.getcontent();

        if (content.isEmpty()) {
            return null;
        }

        if (content.size() == 1) {
            String message = content.get(0).toString();
            System.out.println("Server received message: " + message); //test for if server received packet
            return message; // return one message
        } else {
            List<String> messages = new ArrayList<>();
            for (Object obj : content) {
                String msg = obj.toString();
                messages.add(msg);
            }
            return messages; // return list of messages
        }
    }

    
    /*
     * ******************************************************
     * LOGIN PACKET HANDLER
     * 
     * username is stored in  [0] and password stored in [1] 
     * and is returned as a singly LoginInfo object
     * ******************************************************
     */
    private LoginInfo handleLogin(Packet packet) {
        List<Object> content = packet.getcontent();
        //************************
    	//Test console print
        System.out.println("SERVER: Received LOGIN packet");
        if (content.size() < 2) { // does not contain a password or login
            return null;
        }

        String username = content.get(0).toString();
        String password = content.get(1).toString();

        return new LoginInfo(username, password);
    }
    
    /*
     * *******************************************************
     * ERROR PACKET HANDLER
     * 
     * ******************************************************
     */
    private void handleError(Packet packet) {
        System.out.println("Error: " + packet.getcontent().get(0));
    }
}
