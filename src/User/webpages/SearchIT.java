package User.webpages;

import java.awt.*;
import javax.swing.*;
import server.Client;
import server.Message;
import java.util.List;

class SearchIT extends JPanel {
    private final TeamChatApp app;

    SearchIT(TeamChatApp app) {
        this.app = app;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Search for a User");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(24));
        add(title);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(600, 40));
        searchField.setAlignmentX(Component.CENTER_ALIGNMENT);
        new TextPrompt("Type to search", searchField);

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

        JButton backButton = new JButton("Go back to Chats");
        backButton.setMaximumSize(new Dimension(200, 40));

        JButton searchButton = new JButton("Search");
        searchButton.setMaximumSize(new Dimension(200, 40));

        JButton createUser = new JButton("Create a New User");
        createUser.setMaximumSize(new Dimension(250, 40));

        row.add(backButton);
        row.add(Box.createHorizontalStrut(12));
        row.add(searchButton);
        row.add(Box.createHorizontalStrut(12));
        row.add(createUser);

        searchPanel.add(searchField);
        searchPanel.add(Box.createVerticalStrut(12));
        searchPanel.add(row);
        searchPanel.add(Box.createVerticalGlue());

        add(searchPanel);
        add(Box.createVerticalGlue());

        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a username to search.");
                return;
            }
            
            if (!app.isAdmin()) {
                JOptionPane.showMessageDialog(this, 
                    "Access denied. Admin privileges required.", 
                    "Access Denied", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Client client = app.getClient();
            if (client == null || client.getMyUser() == null) {
                JOptionPane.showMessageDialog(this, 
                    "Not connected to server. Please log in first.", 
                    "Connection Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            //request all messages by this user from server
            try {
                client.requestUserMessages(query);
                // Wait for response
                Thread.sleep(1000);
                List<Message> messages = client.getUserMessages(query);
                if (messages == null || messages.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "User not found or has no messages.", 
                        "Not Found", 
                        JOptionPane.ERROR_MESSAGE);
                } else {
                    app.openITChat(query, messages);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error retrieving messages: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> app.showSearchChat());
        createUser.addActionListener(e -> {
            if (app.isAdmin()) {
                app.showCreateUser();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Access denied. Admin privileges required.", 
                    "Access Denied", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
