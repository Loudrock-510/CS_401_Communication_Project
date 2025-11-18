package User.webpages;

import java.awt.*;
import javax.swing.*;

class SearchChat extends JPanel {
    private final TeamChatApp app;
    private final JButton spectateButton = new JButton("Spectate another viewer's chats");

    SearchChat(TeamChatApp app) {
        this.app = app;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Search recent texts");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(24));
        add(title);

        String[] recentChats = { "Alice", "Bob", "Charlie", "Dev Chat", "Project Room" };
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        for (String chatTitle : recentChats) {
            JButton chatButton = new JButton(chatTitle);
            chatButton.addActionListener(e -> app.openUserChat(chatTitle, demoMessages()));
            listPanel.add(chatButton);
            listPanel.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(listPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(400, 300));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(16));
        add(scroll);

        JTextField searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(600, 40));
        new TextPrompt("Type to search", searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        spectateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        spectateButton.addActionListener(e -> app.showSearchIT());

        add(Box.createVerticalStrut(12));
        add(searchField);
        add(Box.createVerticalStrut(12));
        add(searchButton);
        add(Box.createVerticalStrut(12));
        add(spectateButton);
        add(Box.createVerticalGlue());

        searchButton.addActionListener(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter text to search.");
            } else {
                app.openUserChat(q, demoMessages());
            }
        });

        refreshITVisibility(app.isIT());
    }

    void refreshITVisibility(boolean isIT) {
        spectateButton.setVisible(isIT);
    }

    private static String[] demoMessages() {
        return new String[]{ "hello", "bye", "ok", "see you", "done" };
    }
}
