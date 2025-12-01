package server;

import java.io.Serializable;

public class LoginInfo implements Serializable{
    String username;
    String password;

    LoginInfo(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
