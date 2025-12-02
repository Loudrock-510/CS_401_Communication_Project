package testing;

import static org.junit.Assert.*;

import org.junit.Test;

import server.*;

import java.util.*;

public class TestLog {

	@Test
	public void testReceiveMessage() {
		List<Message> x = new ArrayList<Message>();
		Log l = new Log(x);
		Message msg = new Message(null, "msgTest", null, null);
		l.receiveMessage(msg);
		assertEquals(l.getList().get(0), msg);
	}

}
