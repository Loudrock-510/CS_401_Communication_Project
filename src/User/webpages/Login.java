package User.webpages;

import java.awt.*;
import javax.swing.*;

class Login extends JPanel {
    private final TeamChatApp app;

    Login(TeamChatApp app) {
        this.app = app;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("XYZ teamchat Login");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(24));
        add(title);

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.LIGHT_GRAY);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setPreferredSize(new Dimension(600, 380));
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField userTextField = new JTextField(20);
        JPasswordField passTextField = new JPasswordField(20);
        JCheckBox itCheck = new JCheckBox("Log in as IT"); // marks user as IT
        JButton loginButton = new JButton("Login");

        userTextField.setMaximumSize(new Dimension(400, 40));
        passTextField.setMaximumSize(new Dimension(400, 40));
        loginButton.setMaximumSize(new Dimension(200, 40));
        new TextPrompt("Username", userTextField);
        new TextPrompt("Password", passTextField);

        inputPanel.add(Box.createVerticalStrut(75));
        inputPanel.add(userTextField);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(passTextField);
        inputPanel.add(Box.createVerticalStrut(12));
        inputPanel.add(itCheck);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(loginButton);
        inputPanel.add(Box.createVerticalGlue());

        add(inputPanel);
        add(Box.createVerticalGlue());

        loginButton.addActionListener(e -> {
            String user = userTextField.getText().trim();
            String password = new String(passTextField.getPassword()).trim();
            if (user.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password.");
            } else {
                app.setIT(itCheck.isSelected());
                app.showSearchChat(); // always go to SearchChat
            }
        });
    }
}
