package User.webpages;

import java.awt.*;
import javax.swing.*;

import server.Client;

class PortRequest extends JPanel {
    private final TeamChatApp app;

    PortRequest(TeamChatApp app) {
        this.app = app;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("XYZ teamchat Server Port");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(24));
        add(title);

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.LIGHT_GRAY);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setPreferredSize(new Dimension(600, 380));
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField IPAddressField = new JTextField(20);
        JButton connectButton = new JButton("Connect");

        IPAddressField.setMaximumSize(new Dimension(400, 40));
        connectButton.setMaximumSize(new Dimension(200, 40));

        new TextPrompt("IP Address (default: localhost)", IPAddressField);

        inputPanel.add(Box.createVerticalStrut(75));
        inputPanel.add(IPAddressField);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(connectButton);
        inputPanel.add(Box.createVerticalGlue());

        add(inputPanel);
        add(Box.createVerticalGlue());

        connectButton.addActionListener(e -> {
            String ipAddress = IPAddressField.getText().trim();
            if (ipAddress.isEmpty()) {
                ipAddress = "localhost";
            }

            app.initializeClient(ipAddress);

            connectButton.setEnabled(false);

            Thread waitForClient = new Thread(() -> {
                Client client = null;

                for (int i = 0; i < 10; i++) {
                    client = app.getClient();
                    if (client != null) {
                        break;
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignored) {}
                }

                Client finalClient = client;
                SwingUtilities.invokeLater(() -> {
                    connectButton.setEnabled(true);

                    if (finalClient != null) {
                        JOptionPane.showMessageDialog(this,
                                "Successful connection to server on port " + port + "!",
                                "Connection Successful",
                                JOptionPane.INFORMATION_MESSAGE);

                        app.showLogin();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Failed to connect to server.\n" +
                                "Please ensure the server is running on port " + port + ".",
                                "Connection Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            });

            waitForClient.setDaemon(true);
            waitForClient.start();
        });
    }
}
