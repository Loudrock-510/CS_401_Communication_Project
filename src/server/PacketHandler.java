
package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

public class PacketHandler {
    private final Client client;
    private final Server server;
    private final EnumMap<Type, ClientPacketDelegate> clientDelegates;
    private final EnumMap<Type, ServerPacketDelegate> serverDelegates;

    public PacketHandler(Client client) {
        this.client = client;
        this.server = null;
        this.clientDelegates = new EnumMap<>(Type.class);
        this.serverDelegates = new EnumMap<>(Type.class);
        registerClientDelegates();
    }

    public PacketHandler(Server server) {
        this.client = null;
        this.server = server;
        this.clientDelegates = new EnumMap<>(Type.class);
        this.serverDelegates = new EnumMap<>(Type.class);
        registerServerDelegates();
    }

    public PacketHandler() {
        this.client = null;
        this.server = null;
        this.clientDelegates = new EnumMap<>(Type.class);
        this.serverDelegates = new EnumMap<>(Type.class);
    }

    private void registerClientDelegates() {
        clientDelegates.put(Type.USERS, this::handleUsers);
        clientDelegates.put(Type.MESSAGES, this::handleMessages);
        clientDelegates.put(Type.LOGIN, this::handleLogin);
        clientDelegates.put(Type.ERROR, this::handleError);
        clientDelegates.put(Type.LOGOUT, this::handleLogout);
        clientDelegates.put(Type.GROUP, this::handleGroup);
    }

    private void registerServerDelegates() {
        serverDelegates.put(Type.LOGIN, this::handleLoginRequest);
        serverDelegates.put(Type.MESSAGES, this::handleMessageRequest);
        serverDelegates.put(Type.USERS, this::handleUserRequest);
        serverDelegates.put(Type.GROUP, this::handleGroupRequest);
    }

    private interface ClientPacketDelegate {
        Object apply(Packet packet);
    }

    private interface ServerPacketDelegate {
        void accept(Packet packet, ClientHandler handler);
    }

    /*
     * ********************************************************
     * CLIENT-SIDE PACKET HANDLERS
     * ********************************************************
     */
    public Object handlePacket(Packet packet) {
        if (client == null) {
            throw new IllegalStateException("Client handler requires client reference");
        }
        ClientPacketDelegate delegate = clientDelegates.get(packet.getType());
        if (delegate == null) {
            return null;
        }
        return delegate.apply(packet);
    }

    /*
     * ********************************************************
     * SERVER-SIDE PACKET HANDLERS
     * ********************************************************
     */
    public void handle(Packet packet, ClientHandler handler) {
        if (server == null) {
            throw new IllegalStateException("Server handler requires server reference");
        }
        ServerPacketDelegate delegate = serverDelegates.get(packet.getType());
        if (delegate == null) {
            return;
        }
        delegate.accept(packet, handler);
    }

    private void handleLoginRequest(Packet packet, ClientHandler handler) {
        if (!"REQUEST".equalsIgnoreCase(packet.getStatus())) {
            return;
        }

        List<LoginInfo> loginInfos = extractLoginInfos(packet);
        if (loginInfos.isEmpty()) {
            sendError(handler, "LOGIN", "Missing credentials");
            return;
        }

        LoginInfo creds = loginInfos.get(0);
        
        Optional<User> authenticated = server.findUserByCredentials(creds.getUsername(), creds.getPassword());
        if (authenticated.isPresent()) {
            System.out.println("SERVER: Login successful for " + creds.getUsername());
            User user = authenticated.get();
            // Register the client in both ClientHandler and Server's activeClients map
            handler.registerClient(user, handler.getSocket());
            server.registeredClient(user, handler.getSocket(), handler.getOutputStream());
            // Send user object
            Packet userResponse = new Packet(Type.USERS, "SINGLE", List.of(user));
            handler.send(userResponse);
            // Get all groups/direct messages for this user
            List<Object> userGroups = server.getGroupsForUser(user.getUsername());
            // Always send groups packet, even if empty (so client knows login is complete)
            server.sortAllMessages(); // Sort all messages before sending
            Packet groupsPacket = new Packet(Type.GROUP, "ALL", userGroups);
            handler.send(groupsPacket);
        } else {
            sendError(handler, "LOGIN", "Invalid username or password");
        }
    }

    private List<LoginInfo> extractLoginInfos(Packet packet) {
        List<Object> content = packet.getcontent();
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }

        Object payload = content.get(0);
        if (payload instanceof List<?>) {
            List<?> rawList = (List<?>) payload;
            List<LoginInfo> result = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof LoginInfo) {
                    result.add((LoginInfo) item);
                }
            }
            return result;
        }
        if (payload instanceof LoginInfo) {
            return List.of((LoginInfo) payload);
        }
        return Collections.emptyList();
    }

    private void sendError(ClientHandler handler, String status, String message) {
        Packet errorPacket = new Packet(Type.ERROR, status, List.of(message));
        handler.send(errorPacket);
    }

    /*
     * ********************************************************
     * SERVER: MESSAGE PACKET HANDLER
     * Routes messages to groups and sends updated groups to all participants
     * ********************************************************
     */
    private void handleMessageRequest(Packet packet, ClientHandler handler) {
        try {
            if (!"REQUEST".equalsIgnoreCase(packet.getStatus())) {
                return;
            }

            List<Object> content = packet.getcontent();
            if (content == null || content.isEmpty()) {
                sendError(handler, "MESSAGES", "No message content");
                return;
            }

            //extract Message objects from packet
            List<Message> messages = new ArrayList<>();
            for (Object obj : content) {
                if (obj instanceof Message) {
                    messages.add((Message) obj);
                } else if (obj instanceof List<?>) {
                    List<?> msgList = (List<?>) obj;
                    for (Object item : msgList) {
                        if (item instanceof Message) {
                            messages.add((Message) item);
                        }
                    }
                }
            }
            if (messages.isEmpty()) {
                sendError(handler, "MESSAGES", "No valid messages in packet");
                return;
            }

            //get the sender from the handler's logged in user
            User senderUser = handler.getLoggedInUser();
            if (senderUser == null) {
                sendError(handler, "MESSAGES", "User not logged in");
                return;
            }

            // process each message
            for (Message msg : messages) {
                //validate all recipients exist
                List<String> invalidRecipients = new ArrayList<>();
                for (String recipient : msg.getRecipients()) {
                    Optional<User> recipientUser = server.findUserByUsername(recipient);
                    if (!recipientUser.isPresent()) {
                        invalidRecipients.add(recipient);
                    }
                }
                
                if (!invalidRecipients.isEmpty()) {
                    sendError(handler, "MESSAGES", "Invalid recipients: " + String.join(", ", invalidRecipients));
                    continue;
                }
                
                //store message in master log
                server.getMasterLog().add(msg);
                
                //build participant list (sender + recipients)
                List<String> participants = new ArrayList<>();
                participants.add(msg.getSender());
                participants.addAll(msg.getRecipients());
                
                //find or create group
                Object groupObj = server.findOrCreateGroup(participants, msg.getSender(), msg.getMessage(), msg.getTimestamp());
                
                if (groupObj instanceof Group) {
                    Group group = (Group) groupObj;
                    
                    //check if message already exists (new groups are created with the message already included)
                    boolean messageExists = group.getMessages().stream()
                        .anyMatch(m -> m.getSender().equals(msg.getSender()) && 
                                      m.getMessage().equals(msg.getMessage()) &&
                                      m.getTimestamp().equals(msg.getTimestamp()));
                    
                    if (!messageExists) {
                        server.addMessageToGroup(group, msg);
                    }
                    
                    //save to file asynchronously
                    server.saveGroupsToFileAsync();
                    
                    //send to all participants
                    List<String> allParticipants = group.getGroupUsers();
<<<<<<< HEAD
                    sendGroupsToParticipant(allParticipants, senderUser.getUsername(), handler);
=======
                    sendGroupUpdateToParticipants(group, allParticipants, senderUser.getUsername(), handler);
>>>>>>> 794bd1ace3e5063890adc28763198620dc184d44
                    
                } else if (groupObj instanceof DirectMessage) {
                    DirectMessage dm = (DirectMessage) groupObj;
                    
                    //check if message already exists
                    boolean messageExists = dm.getMessage().stream()
                        .anyMatch(m -> m.getSender().equals(msg.getSender()) && 
                                      m.getMessage().equals(msg.getMessage()) &&
                                      m.getTimestamp().equals(msg.getTimestamp()));
                    
                    if (!messageExists) {
                        server.addMessageToDirectMessage(dm, msg);
                    }
                    
                    //save to file asynchronously
                    server.saveGroupsToFileAsync();
                    
                    //send to all participants
                    List<String> allParticipants = dm.getGroupUsers();
<<<<<<< HEAD
                    sendGroupsToParticipant(allParticipants, senderUser.getUsername(), handler);
=======
                    sendGroupUpdateToParticipants(dm, allParticipants, senderUser.getUsername(), handler);
>>>>>>> 794bd1ace3e5063890adc28763198620dc184d44
                }
            }
        } catch (Exception e) {
            sendError(handler, "MESSAGES", "Server error processing message");
        }
    }
<<<<<<< HEAD
=======
  
    private void sendGroupUpdateToParticipants(Object groupObj, List<String> allParticipants, String senderName, ClientHandler handler) {
        if (groupObj == null) {
            return;
        }
        
        // Create a fresh copy of the group/DM with current messages
        Object freshGroup = null;
        if (groupObj instanceof Group) {
            Group g = (Group) groupObj;
            List<String> users = new ArrayList<>(g.getGroupUsers());
            List<Message> msgs = new ArrayList<>(g.getMessages());
            freshGroup = new Group(users, msgs);
            try {
                java.lang.reflect.Field uidField = Group.class.getDeclaredField("groupUID");
                uidField.setAccessible(true);
                uidField.set(freshGroup, g.getGroupUID());
            } catch (Exception e) {
            }
        } else if (groupObj instanceof DirectMessage) {
            DirectMessage dm = (DirectMessage) groupObj;
            List<String> users = new ArrayList<>(dm.getGroupUsers());
            List<Message> msgs = new ArrayList<>(dm.getMessage());
            freshGroup = new DirectMessage(users, msgs);
            try {
                java.lang.reflect.Field uidField = DirectMessage.class.getDeclaredField("chatUID");
                uidField.setAccessible(true);
                uidField.set(freshGroup, dm.getChatUID());
            } catch (Exception e) {
            }
        }
        
        if (freshGroup == null) {
            return;
        }
        
        // Send UPDATE packet with just this group
        Packet updatePacket = new Packet(Type.GROUP, "UPDATE", List.of(freshGroup));
        
        for (String participantName : allParticipants) {
            if (participantName.equals(senderName)) {
                handler.send(updatePacket);
            } else {
                Optional<User> participant = server.findUserByUsername(participantName);
                if (participant.isPresent()) {
                    server.sendToClient(participant.get(), updatePacket);
                }
            }
        }
    }
>>>>>>> 794bd1ace3e5063890adc28763198620dc184d44
    
    private void sendGroupsToParticipant(List<String> allParticipants, String senderName, ClientHandler handler) {
        for (String participantName : allParticipants) {
            List<Object> participantGroups = new ArrayList<>();
            
            // Add all groups the participant is in
            for (Group g : server.getGroups()) {
                if (g.getGroupUsers().contains(participantName)) {
                    List<String> users = new ArrayList<>(g.getGroupUsers());
                    List<Message> msgs = new ArrayList<>(g.getMessages());
                    Group freshGroup = new Group(users, msgs);
                    try {
                        java.lang.reflect.Field uidField = Group.class.getDeclaredField("groupUID");
                        uidField.setAccessible(true);
                        uidField.set(freshGroup, g.getGroupUID());
                    } catch (Exception e) {
                    }
                    participantGroups.add(freshGroup);
                }
            }
            
            //add all direct messages the participant is in
            for (DirectMessage dm : server.getDirectChats()) {
                if (dm.getGroupUsers().contains(participantName)) {
                    List<String> users = new ArrayList<>(dm.getGroupUsers());
                    List<Message> msgs = new ArrayList<>(dm.getMessage());
                    DirectMessage freshDM = new DirectMessage(users, msgs);
                    try {
                        java.lang.reflect.Field uidField = DirectMessage.class.getDeclaredField("chatUID");
                        uidField.setAccessible(true);
                        uidField.set(freshDM, dm.getChatUID());
                    } catch (Exception e) {
                    }
                    participantGroups.add(freshDM);
                }
            }
            
            server.sortAllMessages();
            Packet packet = new Packet(Type.GROUP, "ALL", participantGroups);
            
            // send to  sender using handler, others via server
            if (participantName.equals(senderName)) {
                handler.send(packet);
            } else {
                Optional<User> participant = server.findUserByUsername(participantName);
                if (participant.isPresent()) {
                    server.sendToClient(participant.get(), packet);
                }
            }
        }
    }

    /*
     * ********************************************************
     * SERVER: USER REQUEST PACKET HANDLER
     * Handles user creation requests
     * ********************************************************
     */
    private void handleUserRequest(Packet packet, ClientHandler handler) {
        if (!"REQUEST".equalsIgnoreCase(packet.getStatus())) {
            return;
        }

        List<Object> content = packet.getcontent();
        if (content == null || content.isEmpty()) {
            sendError(handler, "USERS", "No user data provided");
            return;
        }

        Object first = content.get(0);
        if (!(first instanceof User)) {
            sendError(handler, "USERS", "Invalid user data");
            return;
        }

        User newUser = (User) first;
        String username = newUser.getUsername();

        //check if username already exists
        //option is a container that can be empty, just in case so it doesnt throw an error
        Optional<User> existing = server.findUserByUsername(username);
        if (existing.isPresent()) {
            sendError(handler, "USERS", "Username already exists");
            return;
        }
        //aAdd user to server's user list
        server.getUsers().add(newUser);
        //save users to file
        server.saveUsersToFile();
        //send success response
        Packet successPacket = new Packet(Type.USERS, "SUCCESS", List.of(newUser));
        handler.send(successPacket);
    }

    /*
     * *********************************************************
     * CLIENT: USER PACKET HANDLER
     * ********************************************************
     */
    private Object handleUsers(Packet packet) {
        List<Object> content = packet.getcontent();
        if (content == null || content.isEmpty()) {
            return null;
        }

        String status = packet.getStatus();

        //handle user creation success
        // move gui in the future
        if ("SUCCESS".equalsIgnoreCase(status)) {
            Object first = content.get(0);
            if (first instanceof User) {
                User user = (User) first;
                //show success dialog
                javax.swing.SwingUtilities.invokeLater(() -> {
                    javax.swing.JOptionPane.showMessageDialog(null, 
                        "User created successfully: " + user.getUsername(), 
                        "Success", 
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                });
                return user;
            }
        }

        //handle login (SINGLE status)
        Object first = content.get(0);
        if (first instanceof User) {
            User user = (User) first;
            client.setMyUser(user);
            return user;
        }

        List<User> users = new ArrayList<>();
        for (Object obj : content) {
            if (obj instanceof User) {
                users.add((User) obj);
            }
        }

        if (!users.isEmpty()) {
            return users;
        }

        return null;
    }

    /*
     * ********************************************************
     * SERVER: GROUP REQUEST PACKET HANDLER
     * Handles requests for user message history (admin feature)
     * ********************************************************
     */
    private void handleGroupRequest(Packet packet, ClientHandler handler) {
        if (!"REQUEST".equalsIgnoreCase(packet.getStatus())) {
            return;
        }

        User requester = handler.getLoggedInUser();
        if (requester == null || !requester.isAdmin()) {
            sendError(handler, "GROUP", "Access denied. Admin privileges required.");
            return;
        }

        List<Object> content = packet.getcontent();
        if (content == null || content.isEmpty()) {
            sendError(handler, "GROUP", "No username provided");
            return;
        }

        String targetUsername = content.get(0).toString();
        //verify user exists
        Optional<User> targetUser = server.findUserByUsername(targetUsername);
        if (!targetUser.isPresent()) {
            sendError(handler, "GROUP", "User not found");
            return;
        }

        //get all messages by this user
        List<Message> userMessages = server.getAllMessagesByUser(targetUsername);

        // send messages back
        Packet responsePacket = new Packet(Type.GROUP, "MESSAGES", List.of(userMessages));
        handler.send(responsePacket);
    }

    /*
     * *****************************************************
     * CLIENT: MESSAGE PACKET HANDLER
     * *****************************************************
     */
    private Object handleMessages(Packet packet) {
        List<Object> content = packet.getcontent();

        if (content == null || content.isEmpty()) {
            return null;
        }

        String status = packet.getStatus();
        
        //handle received messages
        if ("RECEIVED".equalsIgnoreCase(status)) {
            for (Object obj : content) {
                if (obj instanceof Message) {
                    Message msg = (Message) obj;
                    return msg;
                }
            }
        }
        
        //handle sent confirmation
        if ("SENT".equalsIgnoreCase(status)) {
            for (Object obj : content) {
                if (obj instanceof Message) {
                    Message msg = (Message) obj;
                    return msg;
                }
            }
        }

        //fallback for other message types
        if (content.size() == 1) {
            String message = content.get(0).toString();
            return message;
        } else {
            List<String> messages = new ArrayList<>();
            for (Object obj : content) {
                String msg = obj.toString();
                messages.add(msg);
            }
            return messages;
        }
    }

    /*
     * ******************************************************
     * CLIENT: LOGIN PACKET HANDLER
     * ******************************************************
     */
    private Object handleLogin(Packet packet) {
        return null;
    }

    /*
     * *******************************************************
     * CLIENT: ERROR PACKET HANDLER
     * ******************************************************
     */
    private Object handleError(Packet packet) {
        List<Object> content = packet.getcontent();
        String status = packet.getStatus();
        if (content != null && !content.isEmpty()) {
            String errorMsg = content.get(0).toString();
            
            //show error dialog for user creation, message errors, or group errors (move gui in the future)
            if ("USERS".equalsIgnoreCase(status) || "MESSAGES".equalsIgnoreCase(status) || "GROUP".equalsIgnoreCase(status)) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    javax.swing.JOptionPane.showMessageDialog(null, 
                        "Error: " + errorMsg, 
                        "Error", 
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                });
            }
        }
        return null;
    }

    private Object handleLogout(Packet packet) {
        return null;
    }

    /*
     * *******************************************************
     * CLIENT: GROUP PACKET HANDLER
     * Handles Group and DirectMessage updates from server
     * ******************************************************
     */
    private Object handleGroup(Packet packet) {
        List<Object> content = packet.getcontent();
        if (content == null || content.isEmpty()) {
            return null;
        }

        String status = packet.getStatus();
        
        if ("MESSAGES".equalsIgnoreCase(status)) {
            //admin message history response
            List<Message> messages = new ArrayList<>();
            for (Object obj : content) {
                if (obj instanceof Message) {
                    messages.add((Message) obj);
                } else if (obj instanceof List<?>) {
                    List<?> msgList = (List<?>) obj;
                    for (Object item : msgList) {
                        if (item instanceof Message) {
                            messages.add((Message) item);
                        }
                    }
                }
            }
            //store messages
            if (!messages.isEmpty()) {
                String username = messages.get(0).getSender();
                client.setUserMessages(username, messages);
            }
            return messages;
        }
        
        if ("ALL".equalsIgnoreCase(status)) {
            List<Object> allGroups = new ArrayList<>();
            for (Object obj : content) {
                if (obj instanceof Group || obj instanceof DirectMessage) {
                    allGroups.add(obj);
                }
            }
            // completely clear and repopulate client groups to ensure that all groups are refreshed
            if (client != null) {
                client.setMyGroups(allGroups);
            }
            return allGroups;
        } else if ("UPDATE".equalsIgnoreCase(status)) {
            //single group/direct message update
            Object groupObj = content.get(0);
            if (groupObj instanceof Group) {
                Group group = (Group) groupObj;
                
                //update client's group list
                if (client != null) {
                    client.updateGroup(group);
                }
                return group;
            } else if (groupObj instanceof DirectMessage) {
                DirectMessage dm = (DirectMessage) groupObj;
                //update client's group list
                if (client != null) {
                    client.updateGroup(dm);
                }
                return dm;
            }
        }

        return null;
    }
}
