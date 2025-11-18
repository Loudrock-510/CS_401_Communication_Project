package User.webpages;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class LoginRequest {
    public static void main(String[] args) {
        try {
            String username = "sampleUser";
            String password = "samplePass";

            Socket socket = new Socket("127.0.0.1", 5000);
            OutputStream output = socket.getOutputStream();

            String loginPacket = "{\"action\":\"login\",\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
            byte[] data = loginPacket.getBytes(StandardCharsets.UTF_8);

            output.write(data);
            output.flush();

            System.out.println("Login packet sent: " + loginPacket);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
