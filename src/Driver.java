import java.util.*;
import java.io.*;
import java.net.*;
import server.*;

public class Driver {
	public static void main(String[] args) {
		Output
		ObjectOutputStream oos = new ObjectOutputStream();
		Server server = new Server(59090);
		server.startServer();
		Client client = new Client();
	}
}
