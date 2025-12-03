package User.webpages;

import java.awt.*;
import javax.swing.*;

import server.Client;
import server.Group;
import server.DirectMessage;
import server.Message;
import java.util.List;

public class TeamChatApp extends JFrame {
    public static final String PortReuqest = "portRequest";
    public static final String LOGIN = "login";
    public static final String SEARCH_CHAT = "searchChat";
    public static final String CHATROOM = "chatroom";
    public static final String CHATROOM_IT = "chatroomIT";
    public static final String SEARCH_IT = "searchIT";
    public static final String CREATE_USER = "createUser";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private Client client;
    private PortRequest portRequest;
    private Login login;
    private SearchChat searchChat;
    private Chatroom chatroom;
    private ChatroomIT chatroomIT;
    private SearchIT searchIT;
    private CreateUser createUser;

    public TeamChatApp() {
        super("XYZ TeamChat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(900, 600));
        setMinimumSize(new Dimension(720, 480));

        //initialize Client connection in background thread
        //DECISION: Keep default port initialization for backward compatibility
        //PortRequest allows users to specify custom port if needed
        initializeClient(12345);

        chatroom = new Chatroom(this);
        chatroomIT = new ChatroomIT(this);
        searchChat = new SearchChat(this);
        searchIT = new SearchIT(this);
        createUser = new CreateUser(this);
        login = new Login(this);
        portRequest = new PortRequest(this);

        root.add(portRequest, PortReuqest);
        root.add(login, LOGIN);
        root.add(searchChat, SEARCH_CHAT);
        root.add(chatroom, CHATROOM);
        root.add(chatroomIT, CHATROOM_IT);
        root.add(searchIT, SEARCH_IT);
        root.add(createUser, CREATE_USER);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    public void initializeClient(int port) {
        Thread clientThread = new Thread(() -> {
            client = Client.createAndConnect(port);

            if (client == null) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Failed to connect to server.\n" +
                        "Please ensure the server is running on port " + port + ".",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            } else {
                //set up callback for group updates IMMEDIATELY after connection
                client.setGroupUpdateCallback(() -> {
                    //ensure callback runs on EDT
                    SwingUtilities.invokeLater(() -> {
                        //always refresh SearchChat when groups are updated
                        if (searchChat != null) {
                            searchChat.refreshGroups();
                        }
                        
                        //if Chatroom is open, update it with the latest group from reseeded list
                        if (chatroom != null && chatroom.getCurrentGroup() != null) {
                            Object currentGroup = chatroom.getCurrentGroup();
                            int currentUID = -1;
                            boolean isGroup = false;
                            
                            if (currentGroup instanceof Group) {
                                currentUID = ((Group) currentGroup).getGroupUID();
                                isGroup = true;
                            } else if (currentGroup instanceof DirectMessage) {
                                currentUID = ((DirectMessage) currentGroup).getChatUID();
                                isGroup = false;
                            }
                            
                            if (currentUID >= 0) {
                                //get latest version from reseeded client groups
                                Object latestGroup = null;
                                if (isGroup) {
                                    latestGroup = client.getGroupById(currentUID);
                                } else {
                                    latestGroup = client.getDirectMessageById(currentUID);
                                }
                                
                                if (latestGroup != null) {
                                    chatroom.updateGroupIfOpen(latestGroup);
                                }
                            }
                        }
                        
                        //NOTIFICATION !!! i dont think it works rn as is
                        //also notify about last updated group if available
                        Object updatedGroup = client.getLastUpdatedGroup();
                        if (updatedGroup != null) {
                            notifyGroupUpdated(updatedGroup);
                        }
                    });
                });
                
                //also set up a periodic check to refresh SearchChat if groups arrive
                //this is a backup in case callback doesn't work
                Thread refreshCheck = new Thread(() -> {
                    try {
                        Thread.sleep(3000); //wait 3 seconds after connection
                        SwingUtilities.invokeLater(() -> {
                            if (client != null && client.getMyUser() != null && searchChat != null) {
                                searchChat.refreshGroups();
                            }
                        });
                    } catch (InterruptedException e) {
                        //ignore
                    }
                });
                refreshCheck.setDaemon(true);
                refreshCheck.start();
            }
        });
        clientThread.setDaemon(true);
        clientThread.start();
    }

    //gets the Client instance for server communication.
    public Client getClient() {
        return client;
    }

    void showCard(String name) { 
        cards.show(root, name);
        root.revalidate();
        root.repaint();
    }

    //refreshes visibility of all admin-only UI elements
    private void refreshAdminVisibility() {
        boolean isAdmin = (client != null && client.getMyUser() != null && client.getMyUser().isAdmin());
        if (searchChat != null) searchChat.refreshAdminVisibility(isAdmin);
        if (chatroom != null) chatroom.refreshAdminVisibility(isAdmin);
        if (chatroomIT != null) chatroomIT.refreshAdminVisibility(isAdmin);
    }
    
    boolean isAdmin() {
        return (client != null && client.getMyUser() != null && client.getMyUser().isAdmin());
    }

    void openUserChat(String otherUser, String[] messages) {
        chatroom.loadConversation(otherUser, messages);
        showCard(CHATROOM);
    }
    
    //opens a chatroom with a Group or DirectMessage object
    void openGroupChat(Object groupObj) {
        if (groupObj != null) {
            chatroom.loadGroupConversation(groupObj);
            showCard(CHATROOM);
        }
    }

    void openITChat(String otherUser, List<Message> messages) {
        chatroomIT.loadConversation(otherUser, messages);
        showCard(CHATROOM_IT);
    }

    void showSearchChat() {
        refreshAdminVisibility();
        if (searchChat != null) {
            searchChat.refreshGroups();
        }
        showCard(SEARCH_CHAT);
    }
    
    
     //notifies GUI components when a group is updated
    void notifyGroupUpdated(Object groupObj) {
        if (groupObj == null) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> notifyGroupUpdated(groupObj));
            return;
        }
        //update SearchChat
        if (searchChat != null) {
            searchChat.refreshGroups();
        }
        //update Chatroom if it's displaying this group
        if (chatroom != null) {
            chatroom.updateGroupIfOpen(groupObj);
        }
    }

    void showSearchIT() { showCard(SEARCH_IT); }
    void showCreateUser() { showCard(CREATE_USER); }
    void showLogin() { showCard(LOGIN); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TeamChatApp app = new TeamChatApp();
            app.setVisible(true);
            app.showCard(PortReuqest);
        });
    }
}
