package User.webpages;

import java.awt.*;
import javax.swing.*;
import java.util.regex.Pattern;
import server.Client;

class CreateUser extends JPanel {
    private final TeamChatApp app;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JCheckBox adminCheckbox;

    CreateUser(TeamChatApp app) {
        this.app = app;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(24));

        JLabel title = new JLabel("Create a New User");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);

        add(Box.createVerticalStrut(16));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(400, 40));
        new TextPrompt("Username", usernameField);

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(400, 40));
        new TextPrompt("Password", passwordField);

        adminCheckbox = new JCheckBox("Is Admin");
        adminCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton createButton = new JButton("Create");
        createButton.setMaximumSize(new Dimension(200, 40));

        JButton cancel = new JButton("Cancel");
        cancel.setMaximumSize(new Dimension(200, 40));

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(createButton);
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(cancel);

        form.add(usernameField);
        form.add(Box.createVerticalStrut(12));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(12));
        form.add(adminCheckbox);
        form.add(Box.createVerticalStrut(12));
        form.add(buttons);
        form.add(Box.createVerticalGlue());

        add(form);
        add(Box.createVerticalGlue());

        createButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            boolean isAdmin = adminCheckbox.isSelected();

            //validate username
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidAlphanumeric(username) || username.length() < 6 || username.length() > 20) {
                JOptionPane.showMessageDialog(this, 
                    "Username must be alphanumeric and 6-20 characters long.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            //validate password
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a password.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidAlphanumeric(password) || password.length() < 6 || password.length() > 20) {
                JOptionPane.showMessageDialog(this, 
                    "Password must be alphanumeric and 6-20 characters long.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            //create user via client
            Client client = app.getClient();
            if (client == null || !client.isLoggedIn()) {
                JOptionPane.showMessageDialog(this, 
                    "Not connected to server. Please log in first.", 
                    "Connection Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                client.sendUser(username, password, isAdmin);
                // Show success message verification happens on server
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "User creation request sent: " + username, 
                        "Request Sent", 
                        JOptionPane.INFORMATION_MESSAGE);
                    usernameField.setText("");
                    passwordField.setText("");
                    adminCheckbox.setSelected(false);
                });
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error sending user creation request: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
            adminCheckbox.setSelected(false);
            app.showSearchIT();
        });
    }

    private boolean isValidAlphanumeric(String str) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
        return pattern.matcher(str).matches();
    }
}
