package server;

import java.util.*;
import java.net.*;
import java.time.LocalDateTime;
import java.io.*;

public class Client {

	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	private volatile Boolean loggedIn; // true if login is successful
	User myUser;
	private List<Object> myGroups = new ArrayList<>(); // stores Group and DirectMessage objects

	
	public Client(ObjectOutputStream out, ObjectInputStream in) {
		this.out = out;
		this.in = in;
		this.loggedIn = false;
	}
	
	// Static instance for GUI access
		private static Client instance;
		
		// GUI update callback (set by TeamChatApp)
		private Runnable groupUpdateCallback = null;
	
	/**
	 * Creates and initializes a Client instance connected to the server.
	 * This method can be called from GUI applications to get a connected Client.
	 */
	public static Client createAndConnect() {
		try {
			int port = 12345;
			String host = InetAddress.getLocalHost().getHostName();
			Socket socket = new Socket(host, port);
			
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			
			Client client = new Client(out, in);
			instance = client; // set static instance for GUI access
			PacketHandler packetHandler = new PacketHandler(client);
			
			// start listening thread for incoming packets
			client.startListening(packetHandler);
			
			// small delay to ensure listener thread is ready
			Thread.sleep(200);
			return client;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		// ************************************************************
				// CONNECT TO SERVER
				// uses port 12345 and grab host from device
				// ************************************************************
				Client client = createAndConnect();
				if (client == null) {
					return;
				}

				while (true) {
					Thread.sleep(1000);
				}
	}

	// METHODS

	// ************************************************************
	// SENDING NEW USER
	// for creating a new user
	// only accessible by Admin/IT
	// ************************************************************
	public void sendUser(String newUsername, String newPassword, boolean isAdmin) throws IOException {
		List<User> users = new ArrayList<>();
		User newUser = new User(newUsername, newPassword, isAdmin);
		users.add(newUser);
		Packet newUserRequest = new Packet(Type.USERS, "REQUEST", List.of(newUser));
		// send packet
		out.writeObject(newUserRequest);
		// clears
		out.flush();
	}

	// ************************************************************
	// SENDING MESSAGE
	// make recipient list and add before calling
	//
	// ************************************************************
	public void sendMessage(List<String> recipients, String textToBeSent) throws IOException {
		List<Message> newMessageList = new ArrayList<>();
		String sender = myUser.getUsername();
		Message newMessge = new Message(LocalDateTime.now(), textToBeSent, sender, recipients);
		newMessageList.add(newMessge);
		Packet newMessageRequest = new Packet(Type.MESSAGES, "REQUEST", List.of(newMessageList));
		// send packet
		out.writeObject(newMessageRequest);
		// clears
		out.flush();
	}// verify new message and send the same message back to add to chat
	

	// ************************************************************
	// SENDING LOGIN
	// used for sending server login info to verify
	//
	// ************************************************************
	public void sendLogin(String username, String password) throws IOException {
		// create list to send
		List<LoginInfo> logData = new ArrayList<>();
		// create create info you want to send
		LoginInfo myInfo = new LoginInfo(username, password);
		// add info to list
		logData.add(myInfo);
		// create packet
		Packet loginInfo = new Packet(Type.LOGIN, "DEFAULT", List.of(logData));
		// send packet
		out.writeObject(loginInfo);
		// clears
		out.flush();
	}// add server sending a user.
	// ************************************************************
	// SENDING LOGOUT
	// ************************************************************
	public void sendLogout() throws IOException {
		List<LoginInfo> empty = new ArrayList<>();
		Packet logoutRequest = new Packet(Type.LOGOUT, "REQUEST", List.of(empty));
		// send packet
		out.writeObject(logoutRequest);
		// clears
		out.flush();
	}
	
	//request user messages (Admin feature)
		//requests all messages sent by a specific user
		public void requestUserMessages(String username) throws IOException {
			Packet requestPacket = new Packet(Type.GROUP, "REQUEST", List.of(username));
			out.writeObject(requestPacket);
			out.flush();
		}
		
		//store for user messages received from server
		private Map<String, List<Message>> userMessagesCache = new HashMap<>();
		
		public synchronized List<Message> getUserMessages(String username) {
			return userMessagesCache.get(username);
		}

		public synchronized void setUserMessages(String username, List<Message> messages) {
			userMessagesCache.put(username, messages);
		}
		public synchronized void setMyUser(User user) {
			this.myUser = user;
			this.loggedIn = user != null;
		}
		
		public synchronized User getMyUser() {
			return myUser;
		}
		
		public boolean isLoggedIn() {
			return Boolean.TRUE.equals(loggedIn);
		}
		
		 //sets the list of groups/direct messages for this client
		public synchronized void setMyGroups(List<Object> groups) {
			if (groups == null) {
				groups = new ArrayList<>();
			}
			
			//for each incoming group, check if we should replace existing
			//build a map of existing groups by UID for quick lookup
			//quick easy way to find groups messages, mapping them to their UID
			Map<Integer, Object> existingGroupsMap = new HashMap<>();
			Map<Integer, Integer> existingMessageCounts = new HashMap<>();
			Map<Integer, Integer> oldMessageCounts = new HashMap<>();
			
			for (Object existing : myGroups) {
				if (existing instanceof Group) {
					Group g = (Group) existing;
					existingGroupsMap.put(g.getGroupUID(), existing);
					existingMessageCounts.put(g.getGroupUID(), g.getMessages().size());
					oldMessageCounts.put(g.getGroupUID(), g.getMessages().size());
				} else if (existing instanceof DirectMessage) {
					DirectMessage dm = (DirectMessage) existing;
					existingGroupsMap.put(dm.getChatUID(), existing);
					existingMessageCounts.put(dm.getChatUID(), dm.getMessage().size());
					oldMessageCounts.put(dm.getChatUID(), dm.getMessage().size());
				}
			}
			
			//completely clear existing groups
			this.myGroups.clear();
			
			//repopulate, but only use incoming groups if they have same or more messages
			for (Object incoming : groups) {
				boolean shouldAdd = true;
				int incomingUID = -1;
				int incomingCount = 0;
				
				if (incoming instanceof Group) {
					Group g = (Group) incoming;
					incomingUID = g.getGroupUID();
					incomingCount = g.getMessages().size();
				} else if (incoming instanceof DirectMessage) {
					DirectMessage dm = (DirectMessage) incoming;
					incomingUID = dm.getChatUID();
					incomingCount = dm.getMessage().size();
				}
				
				if (existingMessageCounts.containsKey(incomingUID)) {
					int existingCount = existingMessageCounts.get(incomingUID);
					if (incomingCount < existingCount) {
						//incoming is stale - keep existing
						this.myGroups.add(existingGroupsMap.get(incomingUID));
						shouldAdd = false;
					}
				}
				
				if (shouldAdd) {
					this.myGroups.add(incoming);
				}
			}
			
			//set lastUpdatedGroup to first group (or null if empty) 
			if (this.myGroups != null && !this.myGroups.isEmpty()) {
				lastUpdatedGroup = this.myGroups.get(0);
			} else {
				lastUpdatedGroup = null;
			}
			notifyGroupUpdate();
			
			//check for new messages and show notifications
			if (myUser != null) {
				String currentUser = myUser.getUsername();
				for (Object groupObj : this.myGroups) {
					Message lastMessage = null;
					int oldCount = 0;
					int newCount = 0;
					
					if (groupObj instanceof Group) {
						Group g = (Group) groupObj;
						newCount = g.getMessages().size();
						oldCount = oldMessageCounts.getOrDefault(g.getGroupUID(), 0);
						if (newCount > oldCount && newCount > 0) {
							lastMessage = g.getMessages().get(newCount - 1);
						}
					} else if (groupObj instanceof DirectMessage) {
						DirectMessage dm = (DirectMessage) groupObj;
						newCount = dm.getMessage().size();
						oldCount = oldMessageCounts.getOrDefault(dm.getChatUID(), 0);
						if (newCount > oldCount && newCount > 0) {
							lastMessage = dm.getMessage().get(newCount - 1);
						}
					}
					
					//show notification if there's a new message from another user
					if (lastMessage != null && !lastMessage.getSender().equals(currentUser)) {
						try {
							Class<?> notifyClass = Class.forName("User.webpages.Notify");
							java.lang.reflect.Method showMethod = notifyClass.getMethod("showNotification", Message.class);
							showMethod.invoke(null, lastMessage);
						} catch (Exception e) {
						}
					}
				}
			}
		}
		
		//gets the list of groups/direct messages for this client
		public synchronized List<Object> getMyGroups() {
			return new ArrayList<>(myGroups); // Return copy to prevent external modification
		}
		
		//store last updated group for GUI notification
		private Object lastUpdatedGroup = null;
		
		//updates or adds a group/direct message to the client's list
		//only replaces if incoming group has same or more messages 
		public synchronized void updateGroup(Object groupObj) {
			if (groupObj == null) {
				return;
			}
			
			//find and replace existing group, or add new one
			boolean found = false;
			boolean shouldReplace = true;
			Message lastMessage = null;
			int existingCount = 0;
			int newCount = 0;
			
			for (int i = 0; i < myGroups.size(); i++) {
				Object existing = myGroups.get(i);
				if (existing instanceof Group && groupObj instanceof Group) {
					Group existingGroup = (Group) existing;
					Group newGroup = (Group) groupObj;
					if (existingGroup.getGroupUID() == newGroup.getGroupUID()) {
						//check message count 
						existingCount = existingGroup.getMessages().size();
						newCount = newGroup.getMessages().size();
						if (newCount < existingCount) {
							shouldReplace = false;
							return; //don't replace with stale data
						}
						//check if there's a new message (not on initial load)
						if (newCount > existingCount && newCount > 0) {
							lastMessage = newGroup.getMessages().get(newCount - 1);
						}
						myGroups.set(i, newGroup);
						found = true;
						break;
					}
				} else if (existing instanceof DirectMessage && groupObj instanceof DirectMessage) {
					DirectMessage existingDM = (DirectMessage) existing;
					DirectMessage newDM = (DirectMessage) groupObj;
					if (existingDM.getChatUID() == newDM.getChatUID()) {
						//check message count 
						existingCount = existingDM.getMessage().size();
						newCount = newDM.getMessage().size();
						if (newCount < existingCount) {
							shouldReplace = false;
							return; //don't replace with stale data
						}
						//check if there's a new message (not on initial load)
						if (newCount > existingCount && newCount > 0) {
							lastMessage = newDM.getMessage().get(newCount - 1);
						}
						myGroups.set(i, newDM);
						found = true;
						break;
					}
				}
			}
			
			if (!found) {
				myGroups.add(groupObj);
			}
			
			if (shouldReplace) {
				lastUpdatedGroup = groupObj;
				notifyGroupUpdate();
				
				//show notification if there's a new message from another user
				if (lastMessage != null && myUser != null) {
					String sender = lastMessage.getSender();
					String currentUser = myUser.getUsername();
					if (!sender.equals(currentUser)) {
						try {
							Class<?> notifyClass = Class.forName("User.webpages.Notify");
							java.lang.reflect.Method showMethod = notifyClass.getMethod("showNotification", Message.class);
							showMethod.invoke(null, lastMessage);
						} catch (Exception e) {
						}
					}
				}
			}
		}
		
		//gets the last updated group (for GUI notifications)
		public synchronized Object getLastUpdatedGroup() {
			return lastUpdatedGroup;
		}
		
		//gets a Group by its UID
		public synchronized Group getGroupById(int groupUID) {
			for (Object groupObj : myGroups) {
				if (groupObj instanceof Group) {
					Group group = (Group) groupObj;
					if (group.getGroupUID() == groupUID) {
						return group;
					}
				}
			}
			return null;
		}
		
		//gets a DirectMessage by its chat UID
		public synchronized DirectMessage getDirectMessageById(int chatUID) {
			for (Object groupObj : myGroups) {
				if (groupObj instanceof DirectMessage) {
					DirectMessage dm = (DirectMessage) groupObj;
					if (dm.getChatUID() == chatUID) {
						return dm;
					}
				}
			}
			return null;
		}
		
		//gets a group or direct message by participant list (order-independent)
		public synchronized Object getGroupByParticipants(List<String> participants) {
			if (participants == null || participants.isEmpty()) {
				return null;
			}
			
			//sort participants for comparison
			List<String> sortedParticipants = new ArrayList<>(participants);
			Collections.sort(sortedParticipants);
			
			for (Object groupObj : myGroups) {
				List<String> groupParticipants;
				if (groupObj instanceof Group) {
					groupParticipants = new ArrayList<>(((Group) groupObj).getGroupUsers());
				} else if (groupObj instanceof DirectMessage) {
					groupParticipants = new ArrayList<>(((DirectMessage) groupObj).getGroupUsers());
				} else {
					continue;
				}
				
				Collections.sort(groupParticipants);
				if (sortedParticipants.equals(groupParticipants)) {
					return groupObj;
				}
			}
			return null;
		}
		
		public void startListening(PacketHandler packetHandler) {
			Thread listener = new Thread(() -> {
				try {
					while (true) {
						Packet packet = (Packet) in.readObject();
						
						Object result = packetHandler.handlePacket(packet);
						
						//if this was a GROUP packet, trigger immediate GUI refresh
						if (packet.getType().equals(server.Type.GROUP)) {
							String status = packet.getStatus();
							if ("ALL".equalsIgnoreCase(status) || "UPDATE".equalsIgnoreCase(status)) {
								//give a small delay then trigger refresh to ensure GUI is ready
								try {
									Thread.sleep(100);
								} catch (InterruptedException ie) {
									//ignore
								}
								notifyGroupUpdate();
							}
						}
					}
				} catch (java.io.EOFException e) {
				} catch (java.net.SocketException e) {
				} catch (Exception e) {
				}
			});
			listener.setDaemon(true); // closes when main ends 
			listener.start();
		}
		
		//gets the current Client instance for GUI access
		public static Client getInstance() {
			return instance;
		}
		
		//sets a callback to be invoked when groups are updated
		public synchronized void setGroupUpdateCallback(Runnable callback) {
			this.groupUpdateCallback = callback;
		}
		
		//notifies the GUI that groups have been updated
		private synchronized void notifyGroupUpdate() {
			if (groupUpdateCallback != null) {
				try {
					groupUpdateCallback.run();
				} catch (Exception e) {
				}
			}
		}


}