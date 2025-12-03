package server;

import java.io.Serializable;


public class User implements Serializable{
	private String username;
	

	private String password;
	private int UID;
	private boolean status;
	private boolean admin;
	
	public User(String username, String password, boolean admin) {
		this.username = username;
		this.password = password;
		this.admin = admin;
	}
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getUID() {
		return UID;
	}

	public boolean isAdmin() {
		return admin;
	}
}