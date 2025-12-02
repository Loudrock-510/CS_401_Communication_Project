package User.webpages;

import java.awt.*;
import javax.swing.*;

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
                JOptionPane.showMessageDialog(this, "Enter text to search.");
            } else {
                app.openITChat(query, new String[]{ "(IT view)", "msg 1", "msg 2" });
            }
        });

        backButton.addActionListener(e -> app.showSearchChat());
        createUser.addActionListener(e -> app.showCreateUser());
    }
}
