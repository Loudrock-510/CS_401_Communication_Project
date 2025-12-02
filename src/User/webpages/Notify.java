package User.webpages;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;

import server.Client;

import server.Message;

class Notify extends JPanel {

    public void sendNotification(Message receivedMsg) {
        JOptionPane.showMessageDialog(null, "Message Received: " + receivedMsg.toString());
    }
}
