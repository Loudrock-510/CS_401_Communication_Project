package testing;

import org.junit.Test;
import static org.junit.Assert.*;

import server.Server;
import server.User;
import server.Message;
import server.Group;
import server.DirectMessage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class ServerTest {

    @Test
    public void testVerifyLogin() {
        Server server = new Server(0);
        User u = new User("loginUser", "secret", false);
        server.getUsers().add(u);

        assertTrue(server.verifyLogin("loginUser", "secret"));
        assertFalse(server.verifyLogin("loginUser", "wrong"));
        assertFalse(server.verifyLogin("unknown", "secret"));
    }

    @Test
    public void testFindUserByUsernameAndCredentials() {
        Server server = new Server(0);
        User u = new User("userA", "pwA", true);
        server.getUsers().add(u);

        Optional<User> byName = server.findUserByUsername("userA");
        assertTrue(byName.isPresent());
        assertEquals("userA", byName.get().getUsername());

        Optional<User> byCreds = server.findUserByCredentials("userA", "pwA");
        assertTrue(byCreds.isPresent());
        assertEquals("userA", byCreds.get().getUsername());

        assertFalse(server.findUserByUsername("nope").isPresent());
        assertFalse(server.findUserByCredentials("userA", "wrong").isPresent());
    }

    @Test
    public void testRegisteredAndRemoveClient() {
        Server server = new Server(0);
        User u = new User("clientUser", "pw", false);
        server.registeredClient(u, null, null);
        server.removeClient(u);
    }

    @Test
    public void testGetAllMessagesByUserFromGroupsAndDMs() {
        Server server = new Server(0);

        List<String> recipients1 = Arrays.asList("bob");
        List<String> recipients2 = Arrays.asList("alice");

        Message m1 = new Message(LocalDateTime.now().minusMinutes(5), "hi from alice", "alice", recipients1);
        Message m2 = new Message(LocalDateTime.now().minusMinutes(2), "reply from bob", "bob", recipients2);
        Message m3 = new Message(LocalDateTime.now().minusMinutes(1), "dm from alice", "alice", recipients1);

        List<Message> groupMsgs = new ArrayList<>();
        groupMsgs.add(m1);
        groupMsgs.add(m2);
        Group g = new Group(Arrays.asList("alice", "bob"), groupMsgs);
        server.getGroups().add(g);

        List<Message> dmMsgs = new ArrayList<>();
        dmMsgs.add(m3);
        DirectMessage dm = new DirectMessage(Arrays.asList("alice", "bob"), dmMsgs);
        server.getDirectChats().add(dm);

        List<Message> aliceMessages = server.getAllMessagesByUser("alice");
        assertEquals(2, aliceMessages.size());
        assertEquals("hi from alice", aliceMessages.get(0).getMessage());
        assertEquals("dm from alice", aliceMessages.get(1).getMessage());
    }

    @Test
    public void testFindOrCreateGroupCreatesDirectMessage() {
        Server server = new Server(0);
        User alice = new User("alice", "pw", false);
        User bob = new User("bob", "pw", false);
        server.getUsers().add(alice);
        server.getUsers().add(bob);

        List<String> participants = Arrays.asList("alice", "bob");
        LocalDateTime now = LocalDateTime.now();

        Object result = server.findOrCreateGroup(participants, "alice", "hello", now);
        assertNotNull(result);
        assertTrue(result instanceof DirectMessage);

        List<DirectMessage> dms = server.getDirectChats();
        assertEquals(1, dms.size());
        DirectMessage dm = dms.get(0);
        assertTrue(dm.getGroupUsers().contains("alice"));
        assertTrue(dm.getGroupUsers().contains("bob"));
        assertEquals(1, dm.getMessage().size());
        assertEquals("hello", dm.getMessage().get(0).getMessage());
    }

    @Test
    public void testFindOrCreateGroupCreatesGroup() {
        Server server = new Server(0);
        User alice = new User("alice", "pw", false);
        User bob = new User("bob", "pw", false);
        User carol = new User("carol", "pw", false);
        server.getUsers().add(alice);
        server.getUsers().add(bob);
        server.getUsers().add(carol);

        List<String> participants = Arrays.asList("alice", "bob", "carol");
        LocalDateTime now = LocalDateTime.now();

        Object result = server.findOrCreateGroup(participants, "alice", "group hello", now);
        assertNotNull(result);
        assertTrue(result instanceof Group);

        List<Group> groups = server.getGroups();
        assertEquals(1, groups.size());
        Group g = groups.get(0);
        assertTrue(g.getGroupUsers().contains("alice"));
        assertTrue(g.getGroupUsers().contains("bob"));
        assertTrue(g.getGroupUsers().contains("carol"));
        assertEquals(1, g.getMessages().size());
        assertEquals("group hello", g.getMessages().get(0).getMessage());
    }

    @Test
    public void testGetGroupsForUser() {
        Server server = new Server(0);

        Message m1 = new Message(LocalDateTime.now(), "g-msg", "alice", Arrays.asList("bob"));
        Group g = new Group(Arrays.asList("alice", "bob"), new ArrayList<>(Arrays.asList(m1)));
        server.getGroups().add(g);

        Message m2 = new Message(LocalDateTime.now(), "dm-msg", "bob", Arrays.asList("alice"));
        DirectMessage dm = new DirectMessage(Arrays.asList("alice", "bob"), new ArrayList<>(Arrays.asList(m2)));
        server.getDirectChats().add(dm);

        List<Object> aliceGroups = server.getGroupsForUser("alice");
        assertEquals(2, aliceGroups.size());
        assertTrue(aliceGroups.stream().anyMatch(o -> o instanceof Group));
        assertTrue(aliceGroups.stream().anyMatch(o -> o instanceof DirectMessage));
    }

    @Test
    public void testSortAllMessages() {
        Server server = new Server(0);

        Message g1 = new Message(LocalDateTime.now().plusMinutes(1), "later", "alice", Arrays.asList("bob"));
        Message g2 = new Message(LocalDateTime.now(), "earlier", "alice", Arrays.asList("bob"));
        Group g = new Group(Arrays.asList("alice", "bob"), new ArrayList<>(Arrays.asList(g1, g2)));
        server.getGroups().add(g);

        Message d1 = new Message(LocalDateTime.now().plusMinutes(2), "dm-later", "bob", Arrays.asList("alice"));
        Message d2 = new Message(LocalDateTime.now(), "dm-earlier", "bob", Arrays.asList("alice"));
        DirectMessage dm = new DirectMessage(Arrays.asList("alice", "bob"), new ArrayList<>(Arrays.asList(d1, d2)));
        server.getDirectChats().add(dm);

        server.sortAllMessages();

        List<Message> sortedGroupMsgs = g.getMessages();
        assertEquals("earlier", sortedGroupMsgs.get(0).getMessage());
        assertEquals("later", sortedGroupMsgs.get(1).getMessage());

        List<Message> sortedDmMsgs = dm.getMessage();
        assertEquals("dm-earlier", sortedDmMsgs.get(0).getMessage());
        assertEquals("dm-later", sortedDmMsgs.get(1).getMessage());
    }

    @Test
    public void testAddMessageToGroup() {
        Server server = new Server(0);

        Group g = new Group(new ArrayList<>(Arrays.asList("alice", "bob")), new ArrayList<Message>());
        server.getGroups().add(g);

        Message m = new Message(LocalDateTime.now(), "group msg", "alice", Arrays.asList("bob"));
        server.addMessageToGroup(g, m);

        assertEquals(1, g.getMessages().size());
        assertEquals("group msg", g.getMessages().get(0).getMessage());
    }

    @Test
    public void testAddMessageToDirectMessage() {
        Server server = new Server(0);

        DirectMessage dm = new DirectMessage(new ArrayList<>(Arrays.asList("alice", "bob")), new ArrayList<Message>());
        server.getDirectChats().add(dm);

        Message m = new Message(LocalDateTime.now(), "dm msg", "alice", Arrays.asList("bob"));
        server.addMessageToDirectMessage(dm, m);

        assertEquals(1, dm.getMessage().size());
        assertEquals("dm msg", dm.getMessage().get(0).getMessage());
    }

    @Test
    public void testToStringForMessages() {
        Server server = new Server(0);

        Message m1 = new Message(LocalDateTime.now(), "m1", "alice", Arrays.asList("bob"));
        Message m2 = new Message(LocalDateTime.now(), "m2", "bob", Arrays.asList("alice"));

        List<Message> list = Arrays.asList(m1, m2);
        String s = server.toString(list);

        assertTrue(s.contains("m1"));
        assertTrue(s.contains("m2"));
    }

    @Test
    public void testLoadData() throws IOException {
        Server server = new Server(0);
        File f = new File("SERVERTESTING.txt");
        FileWriter fw = new FileWriter(f);
        fw.write("TESTTEXT1\nTESTTEXT2");
        fw.close();

        String s = server.loadData("SERVERTESTING.txt");
        assertEquals("TESTTEXT1\nTESTTEXT2\n", s);

        f.delete();
    }

    @Test
    public void testSaveAndLoadGroupsToFile() {
        Server server1 = new Server(0);

        Message m1 = new Message(LocalDateTime.now(), "g-1", "alice", Arrays.asList("bob"));
        Group g = new Group(Arrays.asList("alice", "bob"), new ArrayList<>(Arrays.asList(m1)));
        server1.getGroups().add(g);

        Message m2 = new Message(LocalDateTime.now(), "dm-1", "bob", Arrays.asList("alice"));
        DirectMessage dm = new DirectMessage(Arrays.asList("alice", "bob"), new ArrayList<>(Arrays.asList(m2)));
        server1.getDirectChats().add(dm);

        server1.saveGroupsToFile();

        Server server2 = new Server(0);
        assertFalse(server2.getGroups().isEmpty());
        assertFalse(server2.getDirectChats().isEmpty());
    }
}
