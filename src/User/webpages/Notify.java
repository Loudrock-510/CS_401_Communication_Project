package User.webpages;

import java.awt.*;
import javax.swing.*;
import server.Message;

class Notify {
    
    //shows a notification popup for a new message
    public static void showNotification(Message message) {
        if (message == null) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame parentFrame = null;
                //try to find an existing frame
                Frame[] frames = Frame.getFrames();
                if (frames.length > 0) {
                    parentFrame = (JFrame) frames[0];
                }
                
                JDialog notificationDialog = new JDialog(parentFrame, "New Message", false);
                notificationDialog.setAlwaysOnTop(true);
                notificationDialog.setUndecorated(false);
                notificationDialog.setResizable(false);
                notificationDialog.setModal(false);
                
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
                
                JLabel senderLabel = new JLabel("From: " + message.getSender());
                senderLabel.setFont(senderLabel.getFont().deriveFont(Font.BOLD, 14f));
                senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                String messageText = message.getMessage();
                if (messageText.length() > 100) {
                    messageText = messageText.substring(0, 97) + "...";
                }
                JLabel messageLabel = new JLabel("<html><body style='width: 300px'>" + messageText + "</body></html>");
                messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JButton closeButton = new JButton("Close");
                closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                closeButton.addActionListener(e -> notificationDialog.dispose());
                
                panel.add(senderLabel);
                panel.add(Box.createVerticalStrut(8));
                panel.add(messageLabel);
                panel.add(Box.createVerticalStrut(12));
                panel.add(closeButton);
                
                notificationDialog.getContentPane().add(panel);
                notificationDialog.pack();
                
                //position in top-right corner
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
                int x = (int) (rect.getMaxX() - notificationDialog.getWidth() - 20);
                int y = 20;
                notificationDialog.setLocation(x, y);
                
                notificationDialog.setVisible(true);
                
                //auto-close after 5 seconds
                Timer timer = new Timer(5000, e -> {
                    notificationDialog.dispose();
                });
                timer.setRepeats(false);
                timer.start();
            } catch (Exception e) {
            }
        });
    }
}
