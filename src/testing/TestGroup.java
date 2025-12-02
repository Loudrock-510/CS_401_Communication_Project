package testing;

import static org.junit.Assert.*;

import org.junit.Test;

import server.*;

import java.util.*;

public class TestGroup {

	@Test
	public void testAddToGroup() {
		List<String> users = new ArrayList<String>();
		users.add("Test1");
		Group g = new Group(users, null);
		g.addToGroup("Test1");
		assertEquals(users, g.getGroupUsers());
	}

	@Test
	public void testAddMultipleToGroup() {
		List<String> users = new ArrayList<String>();
		Group g = new Group(users, null);
		users.add("Test1");
		users.add("Test2");
		g.addMultipleToGroup(users);
		assertEquals(users, g.getGroupUsers());
	}

	@Test
	public void testSendNotification() {
		Group g = new Group(null, null);
		g.sendNotification();
		assertTrue(g.getNewMessage());
	}

	@Test
	public void testMessageDelivered() {
		Group g = new Group(null, null);
		g.messageDelivered();
		assertFalse(g.getNewMessage());
	}

	@Test
	public void testGetGroupUsers() {
		List<String> users = new ArrayList<String>();
		users.add("Test1");
		Group g = new Group(users, null);
		assertEquals(users, g.getGroupUsers());
	}

	@Test
	public void testGetMessages() {
		List<Message> msgs = new ArrayList<Message>();
		msgs.add(new Message(null, "Test", null, null));
		Group g = new Group(null, msgs);
		assertEquals(msgs, g.getMessages());
	}

	@Test
	public void testGetGroupUID() {
		Group g = new Group(null, null);
		Group g1 = new Group(null, null);
		assertEquals(1, g1.getGroupUID() - g.getGroupUID());
	}

}
