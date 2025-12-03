package User.webpages;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import server.Client;
import server.Group;
import server.DirectMessage;

class SearchChat extends JPanel {
    private final TeamChatApp app;
    private final JButton spectateButton = new JButton("Spectate another viewer's chats");
    private final JPanel listPanel = new JPanel();
    private final JScrollPane scroll;

    SearchChat(TeamChatApp app) {
        this.app = app;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JButton newConversationButton = new JButton("New Conversation");
        newConversationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newConversationButton.setMaximumSize(new Dimension(200, 40));
        newConversationButton.addActionListener(e -> showNewConversationDialog());
        
        JLabel title = new JLabel("Search recent texts");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(24));
        add(newConversationButton);
        add(Box.createVerticalStrut(12));
        add(title);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        scroll = new JScrollPane(listPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(400, 300));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(16));
        add(scroll);
        
        // Will refresh when groups arrive via callback

        JTextField searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(600, 40));
        new TextPrompt("Type to search", searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        spectateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        spectateButton.addActionListener(e -> {
            if (app.isAdmin()) {
                app.showSearchIT();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Access denied. Admin privileges required.", 
                    "Access Denied", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        add(Box.createVerticalStrut(12));
        add(searchField);
        add(Box.createVerticalStrut(12));
        add(searchButton);
        add(Box.createVerticalStrut(12));
        add(spectateButton);
        add(Box.createVerticalGlue());

        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter text to search.");
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
            
            String currentUsername = client.getMyUser().getUsername();
            List<Object> groups = client.getMyGroups();
            
            if (groups == null || groups.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No conversation found with that user.", 
                    "Not Found", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Search through all conversations
            for (Object groupObj : groups) {
                String displayName = getDisplayName(groupObj, currentUsername);
                if (displayName != null && displayName.equalsIgnoreCase(searchText)) {
                    // Match found - open the chatroom
                    app.openGroupChat(groupObj);
                    return;
                }
            }
            
            // No match found
            JOptionPane.showMessageDialog(this, 
                "No conversation found with that user.", 
                "Not Found", 
                JOptionPane.ERROR_MESSAGE);
        });

        refreshAdminVisibility(app.isAdmin());
    }

    void refreshAdminVisibility(boolean isAdmin) {
        spectateButton.setVisible(isAdmin);
    }
    
    //refreshes the list of groups/chats from the client
    void refreshGroups() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            //clear all existing components
            listPanel.removeAll();
            listPanel.revalidate();
            listPanel.repaint();
            
            Client client = app.getClient();
            if (client == null) {
                JLabel noClientLabel = new JLabel("Not connected to server");
                noClientLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                listPanel.add(noClientLabel);
                listPanel.revalidate();
                listPanel.repaint();
                this.revalidate();
                this.repaint();
                return;
            }
            
            if (client.getMyUser() == null) {
                JLabel noLoginLabel = new JLabel("Please log in");
                noLoginLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                listPanel.add(noLoginLabel);
                listPanel.revalidate();
                listPanel.repaint();
                this.revalidate();
                this.repaint();
                return;
            }
            
            String currentUsername = client.getMyUser().getUsername();
            List<Object> groups = client.getMyGroups();
            
            if (groups == null || groups.isEmpty()) {
                JLabel noChatsLabel = new JLabel("No chats yet. Start a conversation!");
                noChatsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                listPanel.add(noChatsLabel);
            } else {
                for (Object groupObj : groups) {
                    String displayName = getDisplayName(groupObj, currentUsername);
                    if (displayName != null && !displayName.isEmpty()) {
                        JButton chatButton = new JButton(displayName);
                        chatButton.addActionListener(e -> app.openGroupChat(groupObj));
                        listPanel.add(chatButton);
                        listPanel.add(Box.createVerticalStrut(8));
                    }
                }
            }
            
            //force complete UI refresh repaint all parent containers
            listPanel.revalidate();
            listPanel.repaint();
            this.revalidate();
            this.repaint();
            
            //repaint the root CardLayout container
            Container root = this.getTopLevelAncestor();
            if (root != null) {
                root.revalidate();
                root.repaint();
            }
        });
    }
    
    //gets the display name for a group (participants excluding current user)
    private String getDisplayName(Object groupObj, String currentUsername) {
        List<String> participants = new ArrayList<>();
        
        if (groupObj instanceof Group) {
            Group group = (Group) groupObj;
            participants = new ArrayList<>(group.getGroupUsers());
        } else if (groupObj instanceof DirectMessage) {
            DirectMessage dm = (DirectMessage) groupObj;
            participants = new ArrayList<>(dm.getGroupUsers());
        } else {
            return null;
        }
        
        //remove current user from list
        participants.remove(currentUsername);
        
        //join remaining participants with commas
        if (participants.isEmpty()) {
            return "You";
        } else if (participants.size() == 1) {
            return participants.get(0);
        } else {
            return String.join(", ", participants);
        }
    }
    
    private void showNewConversationDialog() {
        Client client = app.getClient();
        if (client == null || !client.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, 
                "Not connected to server. Please log in first.", 
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String currentUsername = client.getMyUser().getUsername();
        
        JDialog dialog = new JDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this), "New Conversation", true);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        dialog.setSize(500, 250);
        dialog.setLocationRelativeTo(this);
        
        JLabel recipientsLabel = new JLabel("Recipients (comma-separated usernames):");
        recipientsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField recipientsField = new JTextField();
        recipientsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel messageLabel = new JLabel("Initial Message:");
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField messageField = new JTextField();
        messageField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(createButton);
        buttonPanel.add(Box.createHorizontalStrut(12));
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalGlue());
        
        dialog.add(Box.createVerticalStrut(16));
        dialog.add(recipientsLabel);
        dialog.add(Box.createVerticalStrut(4));
        dialog.add(recipientsField);
        dialog.add(Box.createVerticalStrut(12));
        dialog.add(messageLabel);
        dialog.add(Box.createVerticalStrut(4));
        dialog.add(messageField);
        dialog.add(Box.createVerticalStrut(16));
        dialog.add(buttonPanel);
        dialog.add(Box.createVerticalStrut(16));
        
        createButton.addActionListener(e -> {
            String recipientsText = recipientsField.getText().trim();
            String initialMessage = messageField.getText().trim();
            
            if (recipientsText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter at least one recipient.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (initialMessage.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter an initial message.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            //parse recipients
            String[] recipientArray = recipientsText.split(",");
            List<String> recipients = new ArrayList<>();
            for (String recipient : recipientArray) {
                String trimmed = recipient.trim();
                if (!trimmed.isEmpty() && !trimmed.equals(currentUsername)) {
                    recipients.add(trimmed);
                }
            }
            
            if (recipients.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter at least one valid recipient (excluding yourself).", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            //validate recipients exist (basic check - server will also validate)
            //for now, we'll let the server handle validation and show error if needed
            try {
                client.sendMessage(recipients, initialMessage);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, 
                    "Conversation created successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error creating conversation: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    }
}
