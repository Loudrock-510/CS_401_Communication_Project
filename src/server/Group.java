package server;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
//import Message
//import User

public class Group implements Serializable {
	private int numUsers;
	private List<String> groupUsers = new ArrayList<>();
	private List<Message> messages = new ArrayList<>();
	static private int count = 0;
	private int groupUID;
	private boolean newMessage;
	
	//constructor for loading existing
	public Group(List<String> groupUsers, List<Message> messages) {
		this.groupUID = count++;
		
		this.groupUsers = groupUsers;
		this.messages = messages;
		
		this.newMessage = false;
	}
	
	//constructor new group , sender and recip and inital message
	
	public Group(String sender, List<String> recipients, String initialMessage, LocalDateTime timestamp) {
		this.groupUID = count++;
		
		//add sender
		groupUsers.add(sender);
		
		//add recipients
		groupUsers.addAll(recipients);
		
		//create initial message
		Message msg = new Message(timestamp, initialMessage, sender, recipients);
		//add it to list
		messages.add(msg);
		
		//size of group
		this.numUsers = groupUsers.size();
		//new so true
		this.newMessage = true;
	}
	
	public Group(String sender, List<String> recipients, String messageText, LocalDateTime timestamp) {
        //TODO Auto-generated constructor stub
    }

    //this is adding a single person to group
	public void addToGroup(String username) {
		groupUsers.add(username);
		numUsers++;
	}
	//if add multiple at once
	public void addMultipleToGroup(List<String> usernames) {
		groupUsers.addAll(usernames); //add all adds entire list to the list
		numUsers =+ usernames.size();
	}
	
	public void sendNotification() {
		newMessage = true;
		//...
	}
	
	public void messageDelivered() {
		newMessage = false;
		//...
	}
	
	//getters
	public List<String> getGroupUsers(){
		return groupUsers;
	}
	
	public List<Message> getMessages(){
		return messages;
	}
	
	public int getGroupUID() {
		return groupUID;
	}
	
}
