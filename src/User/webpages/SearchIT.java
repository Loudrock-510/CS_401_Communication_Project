package User.webpages;

import java.awt.*;
import javax.swing.*;

/**
 * Updated SearchChat with scrollable list of recent chats inside main panel.
 */
public class SearchIT {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createITSearch());
    }

    private static void createITSearch() {
        // Frame setup
        JFrame frame = new JFrame("IT Search for User");
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
        JLabel title = new JLabel("Search for a User");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(Box.createVerticalStrut(24));
        main.add(title);


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
                // searchResult = new SearchChat(query);
            }
        });
    }
}