package User.webpages;

import java.awt.*;
import javax.swing.*;

public class Chatroom {
    public static void main(String[] args) {
        String[] exampleMessages = {"hello", "bye", "ok", "bye", "ok", "bye", "ok", "bye", "ok", "bye", "ok", "bye", "ok", "bye", "ok", "bye", "ok"};
        String exampleName = "jimmy";
        SwingUtilities.invokeLater(() -> createChatroom(exampleMessages, exampleName, true));
    }

    private static void createChatroom(String[] recentMessages, String otherUser, boolean isIT) {
        JFrame frame = new JFrame(otherUser + "'s Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(900, 600));
        frame.setMinimumSize(new Dimension(720, 480));

        // Root (X-axis BoxLayout)
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.X_AXIS));
        root.add(Box.createHorizontalStrut(50));

        // Main panel (Y-axis)
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setAlignmentY(Component.TOP_ALIGNMENT);

        // Title
        JLabel title = new JLabel(otherUser + "'s Chat");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(Box.createVerticalStrut(24));
        main.add(title);

        // Messages list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String nextMessage : recentMessages) {
            JLabel chatText = new JLabel(": " + nextMessage);
            chatText.setAlignmentX(Component.LEFT_ALIGNMENT);
            chatText.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            listPanel.add(chatText);
            listPanel.add(Box.createVerticalStrut(4));
        }

        JScrollPane recentScroll = new JScrollPane(
                listPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        recentScroll.setPreferredSize(new Dimension(400, 300));
        recentScroll.setMinimumSize(new Dimension(300, 400));
        recentScroll.setMaximumSize(new Dimension(600, 800));
        recentScroll.getVerticalScrollBar().setUnitIncrement(16);
        recentScroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        main.add(Box.createVerticalStrut(16));
        main.add(recentScroll);

        // Spacer
        main.add(Box.createVerticalStrut(50));
        
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.Y_AXIS));
        sendPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField sendMessageTf = new JTextField();
        sendMessageTf.setMaximumSize(new Dimension(600, 40));
        sendMessageTf.setAlignmentX(Component.CENTER_ALIGNMENT);
        new TextPrompt("New Messages", sendMessageTf);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));


        JButton sendButton = new JButton("Send");
        sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sendButton.setMaximumSize(new Dimension(200, 40));

        buttonPanel.add(sendButton);
        if (isIT){
            JButton ITsearch = new JButton("Spectate another viewer's chats");
            ITsearch.setAlignmentX(Component.CENTER_ALIGNMENT);
            ITsearch.setMaximumSize(new Dimension(200, 40));
            buttonPanel.add(ITsearch);
            ITsearch.addActionListener(e -> {
                //switch to IT search
            });
        }
        
        sendPanel.add(sendMessageTf);
        sendPanel.add(Box.createVerticalStrut(12));
        sendPanel.add(buttonPanel);
        sendPanel.add(Box.createVerticalGlue());

        main.add(sendPanel);

        // Action: only present when !isIT
        sendButton.addActionListener(e -> {
            String query = sendMessageTf.getText().trim();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No message detected.");
            } else {
                JLabel chatText = new JLabel("You: " + query);
                chatText.setAlignmentX(Component.LEFT_ALIGNMENT);
                chatText.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                listPanel.add(chatText);
                listPanel.add(Box.createVerticalStrut(4));
                listPanel.revalidate();
                listPanel.repaint();
                sendMessageTf.setText("");
            }
        });
    

        main.add(Box.createVerticalGlue());

        // Add main to root
        root.add(main);
        root.add(Box.createHorizontalGlue());

        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
