package testing;

import org.junit.Test;
import static org.junit.Assert.*;
import server.User;

public class UserTesting {
	
	@Test
	public void constructorTest() {
		String username = "Username";
		String password = "Password";
		boolean admin = false;
		
		User user = new User(username, password, admin);
		
		assertEquals("Username", user.getUsername());
		assertEquals("Password", user.getPassword());
		assertEquals(false, user.isAdmin());
	}
	
	@Test
	public void testIsStatus() {
		String username = "Username";
		String password = "Password";
		boolean admin = false;
		
		User user = new User(username, password, admin);
		
		user.setStatus(false);
		
		assertEquals(false, user.isStatus());
	}
	
	@Test
	public void testSetStatus() {
		String username = "Username";
		String password = "Password";
		boolean admin = false;
		
		User user = new User(username, password, admin);
		
		user.setStatus(false);
		
		assertEquals(false, user.isStatus());
	}
	
	@Test
	public void testGetUsername() {
		String username = "Username";
		String password = "Password";
		boolean admin = false;
		
		User user = new User(username, password, admin);
		
		assertEquals(username, user.getUsername());
	}
	
	@Test
	public void testGetPassword() {
		String username = "Username";
		String password = "Password";
		boolean admin = false;
		
		User user = new User(username, password, admin);
		
		assertEquals(password, user.getPassword());
	}
	
	@Test
	public void testGetUID() {
		User user1 = new User("u1", "password", false);
		User user2 = new User("u2", "password", false);

		assertEquals(user1.getUID() + 1, user2.getUID());
	}
	
	@Test
	public void testIsAdmin() {
		String username = "Username";
		String password = "Password";
		boolean admin = true;
		
		User user = new User(username, password, admin);
		
		assertEquals(true, user.isAdmin());
	}
}

