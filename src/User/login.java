package User;

import java.awt.*;
import javax.swing.*;

public class Login {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::createLoginPage);
    }

    private static void createLoginPage() {
        JFrame jFrame = new JFrame("Login");
        JPanel inputPanel = new JPanel();

        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLayout(new FlowLayout());
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
        TextPrompt tpEquation = new TextPrompt("Username", userTextField);
        TextPrompt tpAnswer = new TextPrompt("Password", passTextField);

        inputPanel.add(Box.createVerticalStrut(75));
        inputPanel.add(userTextField);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(passTextField);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(loginButton);
        inputPanel.add(Box.createVerticalGlue());
        jFrame.add(inputPanel);
        jFrame.setVisible(true);
        inputPanel.setVisible(true);

        loginButton.addActionListener(e -> {
            //insert login sequence here!
            
        });
    }
}
