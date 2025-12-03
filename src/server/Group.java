package server;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import server.User;
//import Message
//import User

public class Group implements Serializable {
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
	
	public Group(String sender, List<String> recipients, String messageText, LocalDateTime timestamp) {
		this.groupUID = count++;
		
		//add sender
		groupUsers.add(sender);
		
		//add recipients
		groupUsers.addAll(recipients);
		
		//create initial message
		Message msg = new Message(timestamp, messageText, sender, recipients);
		//add it to list
		messages.add(msg);
		
		//new so true
		this.newMessage = true;
	}

    //this is adding a single person to group
	public void addToGroup(String username) {
		groupUsers.add(username);
	}
	//if add multiple at once
	public void addMultipleToGroup(List<String> usernames) {
		groupUsers.addAll(usernames); //add all adds entire list to the list
	}
	
	public void sendNotification() {
		newMessage = true;
		//...
	}
	
	public void messageDelivered() {
		newMessage = false;
		//...
	}
	
	public Boolean getNewMessage() {
		return newMessage;
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
	public String toString() {
		String id = "" + groupUID, users = "", msgs = "";
		for (int i = 0; i < groupUsers.size(); i++) {
			users += groupUsers.get(i) + ',';
		}
		for (int i = 0; i < messages.size(); i++) {
			msgs += messages.get(i).getMessage() + "~sent at~" + messages.get(i).getTimestamp();
		}
		return id + '~' + users + '~' + msgs;
	}
	
}
