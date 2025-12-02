package User.webpages;

import java.awt.*;
import javax.swing.*;

public class TeamChatApp extends JFrame {
    public static final String LOGIN = "login";
    public static final String SEARCH_CHAT = "searchChat";
    public static final String CHATROOM = "chatroom";
    public static final String CHATROOM_IT = "chatroomIT";
    public static final String SEARCH_IT = "searchIT";
    public static final String CREATE_USER = "createUser";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private boolean isIT = false; // determines if user is IT
    //private User currentUser;
    //this is to keep track of the logged in user, username, password, isIT, etc.
    //User getCurrentUser() { return currentUser; }

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

        chatroom = new Chatroom(this);
        chatroomIT = new ChatroomIT(this);
        searchChat = new SearchChat(this);
        searchIT = new SearchIT(this);
        createUser = new CreateUser(this);
        login = new Login(this);

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

    void showCard(String name) { cards.show(root, name); }

    void setIT(boolean it) {
        this.isIT = it;
        refreshITVisibility();
    }

    boolean isIT() { return isIT; }

    /** Refreshes visibility of all IT-only UI elements */
    private void refreshITVisibility() {
        if (searchChat != null) searchChat.refreshITVisibility(isIT);
        if (chatroom != null) chatroom.refreshITVisibility(isIT);
        if (chatroomIT != null) chatroomIT.refreshITVisibility(isIT);
    }

    void openUserChat(String otherUser, String[] messages) {
        chatroom.loadConversation(otherUser, messages);
        showCard(CHATROOM);
    }

    void openITChat(String otherUser, String[] messages) {
        chatroomIT.loadConversation(otherUser, messages);
        showCard(CHATROOM_IT);
    }

    void showSearchChat() {
        refreshITVisibility();
        showCard(SEARCH_CHAT);
    }

    void showSearchIT() { showCard(SEARCH_IT); }
    void showCreateUser() { showCard(CREATE_USER); }
    void showLogin() { showCard(LOGIN); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TeamChatApp app = new TeamChatApp();
            app.setVisible(true);
            app.showCard(LOGIN);
            //to make default logout
            //app.setDefaultCloseOperation(Client.sendLogout());
            app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }
}
