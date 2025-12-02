package server;

import java.util.*;
//import User
//import Message

public class DirectMessage {
   private List<String> groupUsers = new ArrayList<>();
   private List<Message> messages = new ArrayList<>();
   static private int count = 0;
   private int chatUID;
   private boolean newMessage;
   
   //constructor
   
   public DirectMessage(User sender, User recipient, String initialMessage) {
	   this.chatUID = count++;
	   
	   //add the users
	   groupUsers.add(sender.getUsername());
	   groupUsers.add(recipient.getUsername());
	   
	   //initial message create it
	   Message msg = new Message(java.time.LocalDateTime.now(),
			   sender.getUsername(), 
			   initialMessage,
			   java.util.List.of(recipient.getUsername()));
	   //add message to list
	   messages.add(msg);
   }
   
   //contructor for loading existing chat
   public DirectMessage(List<String> groupUsers, List<Message> messages) {
	   this.chatUID = count++;
	   
	   this.groupUsers = groupUsers;
	   this.messages = messages;
	 
	   //false since existing chat
	   this.newMessage = false;
   }
   
   public void sendNotifcation() {
	   newMessage = true;
	   //...
	   
   }
   
   public void messageDelivered() {
	   newMessage = false;
	   //...
   }
   
   //getters
   
   public int getChatUID() {
	   return this.chatUID;
   }
   
   public List<String> getGroupUsers(){
	   return this.groupUsers;
   }
   
   public List<Message> getMessage() {
	   return this.messages;
   }
   
   public boolean isNewMessage() {
	   return newMessage;
   }
   
   
   
}
