package testing;

import static org.junit.Assert.*;

import org.junit.Test;

import server.Server;

import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;

import java.net.*;

public class TestServer {

	@Test
	public void testRemoveClient() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
		//...
	}

	@Test
	public void testVerifyLogin() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveLog() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
	
	}

	@Test
	public void testSaveUser() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
	}

	@Test
	public void testStringToLog() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
	}

	@Test
	public void testStringToUser() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
	}

	@Test
	public void testCreateLog() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
	}
	

	@Test
	public void testGetLog() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
	}

	@Test
	public void testGetLogs() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
	}

	@Test
	public void testViewUserLog() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
	}

	@Test
	public void testGetUsers() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMasterLog() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
	}

	@Test
	public void testMsgListToString() {
		fail("Not yet implemented");
		Server svr = new Server(12345);
	}

	@Test
	public void testLoadData() {
		Server svr = new Server(12345);
		File f = new File("SERVERTESTING.txt");
		try {
			FileWriter fw = new FileWriter(f);
			fw.write("TESTTEXT1\nTESTTEXT2");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//System.out.println("IO filewriter error");
			JOptionPane.showMessageDialog(null, "IO filewriter error");
			//e.printStackTrace();
			return;
		}
		String s = svr.loadData("SERVERTESTING.txt");
		assertEquals(s, "TESTTEXT1\nTESTTEXT2");
	}
}
