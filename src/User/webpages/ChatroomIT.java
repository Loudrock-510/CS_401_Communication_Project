package User.webpages;

import java.awt.*;
import javax.swing.*;
import java.util.List;
import server.Message;

class ChatroomIT extends JPanel {
    private final TeamChatApp app;
    private final JPanel listPanel = new JPanel();
    private final JLabel title = new JLabel();
    private final JButton toSearch = new JButton("Go to another chat in Search");

    ChatroomIT(TeamChatApp app) {
        this.app = app;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(24));

        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(listPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(400, 300));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(Box.createVerticalStrut(16));
        add(scroll);
        add(Box.createVerticalStrut(24));
        toSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(toSearch);
        add(Box.createVerticalGlue());

        toSearch.addActionListener(e -> app.showSearchChat());
    }

    void loadConversation(String otherUser, List<Message> messages) {
        title.setText(otherUser + "'s Messages (Admin View)");
        listPanel.removeAll();
        
        if (messages == null || messages.isEmpty()) {
            JLabel noMessagesLabel = new JLabel("No messages found.");
            noMessagesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(noMessagesLabel);
        } else {
            for (Message msg : messages) {
                String displayText = "[" + msg.getTimestamp() + "] " + msg.getSender() + ": " + msg.getMessage();
                JLabel lbl = new JLabel(displayText);
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                listPanel.add(lbl);
                listPanel.add(Box.createVerticalStrut(4));
            }
        }
        revalidate();
        repaint();
    }

    void refreshAdminVisibility(boolean isAdmin) {
        toSearch.setVisible(isAdmin);
    }
}
