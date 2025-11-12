package User.webpages;

import java.awt.*;
import javax.swing.*;

public class Login {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::createLoginPage);
    }

    private static void createLoginPage() {
        JFrame jFrame = new JFrame("Login");
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.LIGHT_GRAY);

        JLabel title = new JLabel("XYZ teamchat Login");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        jFrame.add(Box.createVerticalStrut(24));
        jFrame.add(title);
        

        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setPreferredSize(new Dimension(700, 450));
        jFrame.setMinimumSize(jFrame.getPreferredSize());
        jFrame.setMaximumSize(new Dimension(1200, 800));
        
        JTextField userTextField = new JTextField(20);
        JTextField passTextField = new JTextField(20);
        JButton loginButton = new JButton("Login");
        
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setPreferredSize(new Dimension(600, 380));
        inputPanel.setMinimumSize(inputPanel.getPreferredSize());
        inputPanel.setMaximumSize(new Dimension(1200, 2000));
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userTextField.setMaximumSize(new Dimension(400, 40));
        passTextField.setMaximumSize(new Dimension(400, 40));
        loginButton.setMaximumSize(new Dimension(200, 40));
        TextPrompt userTF = new TextPrompt("Username", userTextField);
        TextPrompt passTF = new TextPrompt("Password", passTextField);

        inputPanel.add(Box.createVerticalStrut(75));
        inputPanel.add(userTextField);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(passTextField);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(loginButton);
        inputPanel.add(Box.createVerticalGlue());
        jFrame.add(inputPanel);
        jFrame.add(Box.createVerticalGlue());
        jFrame.setVisible(true);
        inputPanel.setVisible(true);

        loginButton.addActionListener(e -> {
            String user = userTextField.getText().trim();
            String password = passTextField.getText().trim();
            if (user.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(jFrame, "Please enter both username and password.");
            } else {
                //to implement login verification
                //Login loginAttempt = new Login(user, password);
                // if (loginAttempt.isSuccessful()) {
                }
            }
        });
    }
}
