package User.webpages;

import java.awt.*;
import javax.swing.*;

/**
 * Updated SearchChat with scrollable list of recent chats inside main panel.
 */
public class SearchChat {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createChatSearchPage(new String[]{ "Alice", "Bob", "Charlie", "Dev Chat", "Project Room"}));
    }

    private static void createChatSearchPage(String[] recentChats) {
        // Frame setup
        JFrame frame = new JFrame("Search");
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
        JLabel title = new JLabel("Search recent texts");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(Box.createVerticalStrut(24));
        main.add(title);

        // Recent panel with scrollable list of buttons
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String chatTitle : recentChats) {
            JButton chatButton = new JButton(chatTitle);
            chatButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            chatButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            chatButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Opening chat: " + chatTitle));
            listPanel.add(chatButton);
            listPanel.add(Box.createVerticalStrut(8));
        }

        JScrollPane recentScroll = new JScrollPane(listPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        recentScroll.setPreferredSize(new Dimension(400, 300));
        recentScroll.setMinimumSize(new Dimension(300, 400));
        recentScroll.setMaximumSize(new Dimension(600,800));
        recentScroll.getVerticalScrollBar().setUnitIncrement(16);
        recentScroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        main.add(Box.createVerticalStrut(16));
        main.add(recentScroll);

        // 50px spacer
        main.add(Box.createVerticalStrut(50));

        // Search field and button section
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(600, 40));
        searchField.setAlignmentX(Component.CENTER_ALIGNMENT);
        new TextPrompt("Type to search", searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchButton.setMaximumSize(new Dimension(200, 40));

        searchPanel.add(searchField);
        searchPanel.add(Box.createVerticalStrut(12));
        searchPanel.add(searchButton);
        searchPanel.add(Box.createVerticalGlue());

        main.add(searchPanel);
        main.add(Box.createVerticalGlue());

        // Add main to root
        root.add(main);
        root.add(Box.createHorizontalGlue());

        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Search action logic placeholder
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter text to search.");
                // No action taken for empty query
            } else {
                JOptionPane.showMessageDialog(frame, "Searching for: " + query);
                // Implement actual search logic here
                //SearchChat searchResult = new SearchChat(query);
            }
        });
    }
}