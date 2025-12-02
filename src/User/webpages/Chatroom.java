package User.webpages;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.*;

import server.Message;
import server.Group;
import server.DirectMessage;
import server.Client;

class Chatroom extends JPanel {
    private final TeamChatApp app;
    private final JPanel listPanel = new JPanel();
    private final JLabel title = new JLabel();
    private final JTextField sendMessageTf = new JTextField();
    private Object currentGroup = null; //currently open Group or DirectMessage
    
    //gets the currently open group or direct message
    Object getCurrentGroup() {
        return currentGroup;
    }

    Chatroom(TeamChatApp app) {
        this.app = app;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(24));

        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane recentScroll = new JScrollPane(listPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        recentScroll.setPreferredSize(new Dimension(400, 300));
        recentScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(Box.createVerticalStrut(16));
        add(recentScroll);
        add(Box.createVerticalStrut(24));

        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.Y_AXIS));

        sendMessageTf.setMaximumSize(new Dimension(600, 40));
        new TextPrompt("New Messages", sendMessageTf);

        JButton sendButton = new JButton("Send");
        JButton backButton = new JButton("Go to another chat back in Search Chat");

        sendPanel.add(sendMessageTf);
        sendPanel.add(Box.createVerticalStrut(12));
        sendPanel.add(sendButton);
        sendPanel.add(Box.createVerticalStrut(8));
        sendPanel.add(backButton);
        add(sendPanel);
        add(Box.createVerticalGlue());

        sendButton.addActionListener(e -> {
            String text = sendMessageTf.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No message detected.");
            }
            else if (text.length() > 1000) {
                JOptionPane.showMessageDialog(this, 
                    "Message exceeds 1000 character limit. Current length: " + text.length() + " characters.",
                    "Message Too Long",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                Client client = app.getClient();
                if (client == null || client.getMyUser() == null) {
                    JOptionPane.showMessageDialog(this, "Not connected to server.");
                    return;
                }
                
                if (currentGroup == null) {
                    JOptionPane.showMessageDialog(this, "No chat selected.");
                    return;
                }
                
                //get recipients from current group (excluding current user)
                List<String> recipients = new ArrayList<>();
                String currentUsername = client.getMyUser().getUsername();
                
                if (currentGroup instanceof Group) {
                    Group group = (Group) currentGroup;
                    for (String user : group.getGroupUsers()) {
                        if (!user.equals(currentUsername)) {
                            recipients.add(user);
                        }
                    }
                } else if (currentGroup instanceof DirectMessage) {
                    DirectMessage dm = (DirectMessage) currentGroup;
                    for (String user : dm.getGroupUsers()) {
                        if (!user.equals(currentUsername)) {
                            recipients.add(user);
                        }
                    }
                }
                
                if (recipients.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No recipients found.");
                    return;
                }
                
                //send message to server
                try {
                    client.sendMessage(recipients, text);
                    sendMessageTf.setText("");
                    //don't append locally - previous interation
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error sending message: " + ex.getMessage(),
                        "Send Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        backButton.addActionListener(e ->{
            app.showSearchChat();
        });
    }
    

    void loadConversation(String otherUser, String[] recentMessages) {
        title.setText(otherUser + "'s Chat");
        listPanel.removeAll();
        for (String m : recentMessages) appendMessage(": " + m);
        revalidate();
        repaint();
    }
    
    //loads a conversation from a Group or DirectMessage object
    void loadGroupConversation(Object groupObj) {
        if (groupObj == null) {
            return;
        }
        
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(() -> loadGroupConversation(groupObj));
            return;
        }
        
        currentGroup = groupObj;
        Client client = app.getClient();
        if (client == null || client.getMyUser() == null) {
            return;
        }
        
        String currentUsername = client.getMyUser().getUsername();
        List<Message> messages;
        List<String> participants = new ArrayList<>();
        
        if (groupObj instanceof Group) {
            Group group = (Group) groupObj;
            messages = new ArrayList<>(group.getMessages());
            participants = new ArrayList<>(group.getGroupUsers());
        } else if (groupObj instanceof DirectMessage) {
            DirectMessage dm = (DirectMessage) groupObj;
            messages = new ArrayList<>(dm.getMessage());
            participants = new ArrayList<>(dm.getGroupUsers());
        } else {
            return;
        }
        
        //set title to participant names (excluding current user)
        participants.remove(currentUsername);
        String displayName;
        if (participants.isEmpty()) {
            displayName = "You";
        } else if (participants.size() == 1) {
            displayName = participants.get(0) + "'s Chat";
        } else {
            displayName = String.join(", ", participants) + "'s Chat";
        }
        title.setText(displayName);
        
        //clear all existing messages completely
        listPanel.removeAll();
        
        //rebuild all message components
        for (Message msg : messages) {
            String sender = msg.getSender();
            String messageText = msg.getMessage();
            if (sender.equals(currentUsername)) {
                appendMessage("You: " + messageText);
            } else {
                appendMessage(sender + ": " + messageText);
            }
        }
        
        //force complete UI refresh
        listPanel.revalidate();
        listPanel.repaint();
        
        //repaint the scroll pane parent
        Container scrollParent = listPanel.getParent();
        if (scrollParent instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) scrollParent;
            scrollPane.revalidate();
            scrollPane.repaint();
            
            //scroll to bottom to show latest message
            javax.swing.SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                if (vertical != null) {
                    vertical.setValue(vertical.getMaximum());
                }
            });
        }
        
        //repaint this panel and all ancestors
        this.revalidate();
        this.repaint();
        
        //repaint the root CardLayout container
        Container root = this.getTopLevelAncestor();
        if (root != null) {
            root.revalidate();
            root.repaint();
        }
    }
    
    //updates the chatroom if it's currently displaying the updated group
    void updateGroupIfOpen(Object groupObj) {
        if (groupObj == null) {
            return;
        }
        
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(() -> updateGroupIfOpen(groupObj));
            return;
        }
        
        if (currentGroup == null) {
            return;
        }
        
        //get UID from the updated group
        final int updatedUID;
        final boolean isGroup;
        if (groupObj instanceof Group) {
            updatedUID = ((Group) groupObj).getGroupUID();
            isGroup = true;
        } else if (groupObj instanceof DirectMessage) {
            updatedUID = ((DirectMessage) groupObj).getChatUID();
            isGroup = false;
        } else {
            return;
        }
        
        //get UID from current group
        int currentUID = -1;
        boolean currentIsGroup = false;
        if (currentGroup instanceof Group) {
            currentUID = ((Group) currentGroup).getGroupUID();
            currentIsGroup = true;
        } else if (currentGroup instanceof DirectMessage) {
            currentUID = ((DirectMessage) currentGroup).getChatUID();
            currentIsGroup = false;
        }
        
        //check if this is the currently open group (must match type and UID)
        //double checking
        if (isGroup == currentIsGroup && updatedUID == currentUID) {
            //get the latest version from client's stored groups to ensure we have the most recent data
            Client client = app.getClient();
            if (client != null) {
                Object latestGroup = null;
                if (isGroup) {
                    latestGroup = client.getGroupById(updatedUID);
                } else {
                    latestGroup = client.getDirectMessageById(updatedUID);
                }
                
                if (latestGroup != null) {
                    //verify message count as it wasnt updating
                    int currentCount = 0;
                    int latestCount = 0;
                    if (isGroup) {
                        currentCount = ((Group) currentGroup).getMessages().size();
                        latestCount = ((Group) latestGroup).getMessages().size();
                    } else {
                        currentCount = ((DirectMessage) currentGroup).getMessage().size();
                        latestCount = ((DirectMessage) latestGroup).getMessage().size();
                    }
                    
                    if (latestCount >= currentCount) {
                        currentGroup = latestGroup;
                        loadGroupConversation(latestGroup);
                    }
                } else {
                    //fallback to the provided groupObj if not found in client
                    currentGroup = groupObj;
                    loadGroupConversation(groupObj);
                }
            } else {
                currentGroup = groupObj;
                loadGroupConversation(groupObj);
            }
        }
    }

    void refreshAdminVisibility(boolean isAdmin) {
        //chatroom currently has no IT-only controls,
        // but the method exists for consistency    
    }

    private void appendMessage(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        listPanel.add(label);
        listPanel.add(Box.createVerticalStrut(4));
    }
}
