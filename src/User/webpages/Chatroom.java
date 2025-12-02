package User.webpages;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

import javax.swing.*;

import server.Message;
import server.Packet;
import server.Type;

class Chatroom extends JPanel {
    private final TeamChatApp app;
    private final JPanel listPanel = new JPanel();
    private final JLabel title = new JLabel();
    private final JTextField sendMessageTf = new JTextField();

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
            } else {
            	
                //Message log to send to server would be created here
                //Message msg = new Message(app.getCurrentUser(), title.getText(), text);       
                appendMessage("You: " + text);
                sendMessageTf.setText("");
                revalidate();
                repaint();
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

    void refreshITVisibility(boolean isIT) {
        // Chatroom currently has no IT-only controls,
        // but the method exists for consistency
    }

    private void appendMessage(String text) {
        JLabel label = new JLabel(text);
        listPanel.add(label);
        listPanel.add(Box.createVerticalStrut(4));
    }
}
