package testing;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import server.DirectMessage;
import server.User;
import server.Message;

public class DirectMessageTest {
	
	@Test
	public void testConstrutor1() {
		User user1 = new User("u1", "password", false);
		User user2 = new User("u2", "password", false);

		DirectMessage dm = new DirectMessage(user1, user2, "test message" );
		
	}
	
	@Test
	public void testConstructor2() {
		//create users
		String user1 = "user1";
		String user2 = "user2";
		List<String> groupList = new ArrayList<>();
		groupList.add(user1);
		groupList.add(user2);
		
		User userOne= new User("u1", "password", false);
		User userTwo = new User("u2", "password", false);
		
		//create msg
		List<String> recipients = new ArrayList<>();
		recipients.add(user2);
		
		Message msg = new Message(java.time.LocalDateTime.now(),
				   user1, 
				   "test",
				   recipients);
		
		List<Message> messageList = new ArrayList<>();
		messageList.add(msg);
		
		//direct msg
		DirectMessage dm = new DirectMessage(groupList, messageList);
	
		assertEquals(0,dm.getChatUID());
		assertEquals(groupList, dm.getGroupUsers());
		assertEquals(false, dm.isNewMessage());
		}
	
	@Test
	public void testSendNotification() {
		//create users
		String user1 = "user1";
		String user2 = "user2";
		List<String> groupList = new ArrayList<>();
		groupList.add(user1);
		groupList.add(user2);
				
		User userOne= new User("u1", "password", false);
		User userTwo = new User("u2", "password", false);
				
		//create msg
		List<String> recipients = new ArrayList<>();
		recipients.add(user2);
				
		Message msg = new Message(java.time.LocalDateTime.now(),
			 user1, 
			 "test",
			recipients);
				
		List<Message> messageList = new ArrayList<>();
		messageList.add(msg);
				
		//direct msg
		DirectMessage dm = new DirectMessage(groupList, messageList);
				
		 // initially false
		assertEquals(false, dm.isNewMessage());
			    
		// send notification
		dm.sendNotification();
		assertEquals(true, dm.isNewMessage());
	}
	
	@Test
	public void testGetChatUID() {
		//create users
		String user1 = "user1";
		String user2 = "user2";
		List<String> groupList = new ArrayList<>();
		groupList.add(user1);
		groupList.add(user2);
		
		User userOne= new User("u1", "password", false);
		User userTwo = new User("u2", "password", false);
		
		//create msg
		List<String> recipients = new ArrayList<>();
		recipients.add(user2);
		
		Message msg = new Message(java.time.LocalDateTime.now(),
				   user1, 
				   "test",
				   recipients);
		
		List<Message> messageList = new ArrayList<>();
		messageList.add(msg);
		
		//direct msg
		DirectMessage dm = new DirectMessage(groupList, messageList);
		
		assertEquals(0,dm.getChatUID());
	}
	
	@Test
	public void testGetGroupUsers() {
		//create users
		String user1 = "user1";
		String user2 = "user2";
		List<String> groupList = new ArrayList<>();
		groupList.add(user1);
		groupList.add(user2);
		
		User userOne= new User("u1", "password", false);
		User userTwo = new User("u2", "password", false);
		
		//create msg
		List<String> recipients = new ArrayList<>();
		recipients.add(user2);
		
		Message msg = new Message(java.time.LocalDateTime.now(),
				   user1, 
				   "test",
				   recipients);
		
		List<Message> messageList = new ArrayList<>();
		messageList.add(msg);
		
		//direct msg
		DirectMessage dm = new DirectMessage(groupList, messageList);
		
		assertEquals(recipients,dm.getGroupUsers());
	}
	
	@Test
	public void testGetMessage() {
		//create users
		String user1 = "user1";
		String user2 = "user2";
		List<String> groupList = new ArrayList<>();
		groupList.add(user1);
		groupList.add(user2);
		
		User userOne= new User("u1", "password", false);
		User userTwo = new User("u2", "password", false);
		
		//create msg
		List<String> recipients = new ArrayList<>();
		recipients.add(user2);
		
		Message msg = new Message(java.time.LocalDateTime.now(),
				   user1, 
				   "test",
				   recipients);
		
		List<Message> messageList = new ArrayList<>();
		messageList.add(msg);
		
		//direct msg
		DirectMessage dm = new DirectMessage(groupList, messageList);
		
		assertEquals(msg, dm.getMessage());
	}
	
}
