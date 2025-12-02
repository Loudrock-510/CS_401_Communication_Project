package User.webpages;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;

import server.Client;

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
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(loginButton);
        inputPanel.add(Box.createVerticalGlue());

        add(inputPanel);
        add(Box.createVerticalGlue());

        loginButton.addActionListener(e -> {
            String username = userTextField.getText().trim();
            String password = new String(passTextField.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password.");
            } else {
                try {
                    Client client = app.getClient();
                    if (client != null) {
                        client.sendLogin(username, password);
                        //wait for login response and groups to arrive
                        //the server sends user object first, then groups
                        Thread.sleep(2000); // Increased wait time for groups to arrive
                        
                        //check if login was successful (user object received)
                        if (client.getMyUser() != null) {
                            //show SearchChat - it will refresh when groups arrive via callback
                            //also force a refresh now in case groups already arrived
                            app.showSearchChat();
                        } else {
                            JOptionPane.showMessageDialog(this, 
                                "Login failed. Please check your credentials.",
                                "Login Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Client not connected. Please wait for connection to establish or check if the server is running.",
                            "Connection Error",
                            JOptionPane.WARNING_MESSAGE);
                        return; //don't proceed to SearchChat if not connected
                    }
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(this, 
                        "Error sending login request: " + e1.getMessage(),
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                    return; //don't proceed to SearchChat on error
                } catch (InterruptedException e1) {
                }
            }
        });
    }
}
