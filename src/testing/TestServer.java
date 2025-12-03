package testing;

import static org.junit.Assert.*;

import org.junit.Test;

import server.Server;
import server.User;
import server.Message;

import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;

public class TestServer {

    @Test
    public void testRemoveClient() {
        Server svr = new Server(12345);
        User u = new User("testRemove", "pw", false);
        svr.registeredClient(u, null, null);
        svr.removeClient(u);
    }

    @Test
    public void testVerifyLogin() {
        Server svr = new Server(12345);
        User u = new User("loginUser", "secret", false);
        svr.getUsers().add(u);
        assertTrue(svr.verifyLogin("loginUser", "secret"));
        assertFalse(svr.verifyLogin("loginUser", "wrong"));
        assertFalse(svr.verifyLogin("unknown", "secret"));
    }

    @Test
    public void testSaveLog() {
        Server svr = new Server(12345);
        svr.saveLog();
    }

    @Test
    public void testSaveUser() {
        Server svr = new Server(12345);
        User u = new User("saveUserTest", "pw", false);
        svr.getUsers().add(u);
        svr.saveUsersToFile();
        Server svr2 = new Server(12345);
        boolean found = false;
        for (User usr : svr2.getUsers()) {
            if ("saveUserTest".equals(usr.getUsername()) && "pw".equals(usr.getPassword())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testStringToLog() {
        Server svr = new Server(12345);
        svr.stringToLog();
    }

    @Test
    public void testStringToUser() {
        Server svr = new Server(12345);
        svr.stringToUser();
    }

    @Test
    public void testCreateLog() {
        Server svr = new Server(12345);
        svr.createLog();
    }

    @Test
    public void testGetLog() {
        Server svr = new Server(12345);
        assertNull(svr.getLog());
    }

    @Test
    public void testGetLogs() {
        Server svr = new Server(12345);
        assertNull(svr.getLogs("someUID"));
    }

    @Test
    public void testViewUserLog() {
        Server svr = new Server(12345);
        assertNull(svr.ViewUserLog("someUser"));
    }

    @Test
    public void testGetUsers() {
        Server svr = new Server(12345);
        List<User> users = svr.getUsers();
        assertNotNull(users);
        int initialSize = users.size();
        User u = new User("getUsersTest", "pw", false);
        users.add(u);
        assertEquals(initialSize + 1, users.size());
        assertTrue(users.contains(u));
    }

    @Test
    public void testGetMasterLog() {
        Server svr = new Server(12345);
        List<Message> master = svr.getMasterLog();
        assertNotNull(master);
        int initialSize = master.size();
        List<String> recipients = new ArrayList<>();
        recipients.add("recipient");
        Message msg = new Message(java.time.LocalDateTime.now(), "hello", "sender", recipients);
        master.add(msg);
        assertEquals(initialSize + 1, master.size());
        assertTrue(master.contains(msg));
    }

    @Test
    public void testMsgListToString() {
        Server svr = new Server(12345);
        List<String> recipients1 = new ArrayList<>();
        recipients1.add("userB");
        Message msg1 = new Message(java.time.LocalDateTime.now(), "message one", "userA", recipients1);
        List<String> recipients2 = new ArrayList<>();
        recipients2.add("userC");
        Message msg2 = new Message(java.time.LocalDateTime.now(), "message two", "userB", recipients2);
        List<Message> msgs = new ArrayList<>();
        msgs.add(msg1);
        msgs.add(msg2);
        String s = svr.toString(msgs);
        assertTrue(s.contains("message one"));
        assertTrue(s.contains("message two"));
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
            JOptionPane.showMessageDialog(null, "IO filewriter error");
            return;
        }
        String s = svr.loadData("SERVERTESTING.txt");
        assertEquals("TESTTEXT1\nTESTTEXT2\n", s);
    }
}
