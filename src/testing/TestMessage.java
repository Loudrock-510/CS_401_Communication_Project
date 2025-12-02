package testing;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.Test;

import server.Message;

public class TestMessage {

	@Test
	public void testGetTimestamp() {
		LocalDateTime cur = LocalDateTime.now();
		Message m = new Message(cur, null, null, null);
		assertEquals(m.getTimestamp(), cur);
	}

	@Test
	public void testGetMessage() {
		Message m = new Message(null, "This is a test.", null, null);
		assertEquals("This is a test.", m.getMessage());
	}

	@Test
	public void testGetSender() {
		Message m = new Message(null, null, "TestSender", null);
		assertEquals("TestSender", m.getSender());
	}

	@Test
	public void testGetRecipients() {
		List<String> users = new ArrayList<String>();
		users.add("Test1");
		Message m = new Message(null, null, null, users);
		assertEquals("Test1", users.get(0));
	}

	@Test
	public void testToString() {
		LocalDateTime cur = LocalDateTime.now();
		List<String> users = new ArrayList<String>();
		users.add("Test1");
		users.add("Test2");
		Message m = new Message(cur, "This is a test..", "Bob", null);
		String test = "Sent at: " + cur + ", Sender: " + "Bob" + 
				"\nMessage Content: " + "This is a test..\nRecipients: Test1, Test2";
		assertEquals(test, m.toString());
	}

}
