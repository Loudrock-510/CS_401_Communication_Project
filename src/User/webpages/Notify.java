package User.webpages;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;

import server.Client;

class Notify extends JPanel {

    sendNotification(Message receivedMsg) {
        JOptionPane.showMessageDialog(null, "Message Received: " + receivedMsg.toString());
    }
}
