package server;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
//import User
//import Message


public class DirectMessage implements Serializable {
   private List<String> groupUsers = new ArrayList<>();
   private List<Message> messages = new ArrayList<>();
   static private int count = 0;
   private int chatUID;
   private boolean newMessage;
   
   //constructor
   
   public DirectMessage(String sender, String recipient, String initialMessage, LocalDateTime timestamp) {
      this.chatUID = count++;
     
      //add the users
      groupUsers.add(sender);
      groupUsers.add(recipient);
     
      //initial message create it
      List<String> recipients = new ArrayList<>();
      recipients.add(recipient);
      Message msg = new Message(timestamp, initialMessage, sender, recipients);
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
   
   
}







