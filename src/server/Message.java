package server;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Message implements Serializable {
	private LocalDateTime timestamp;
	private String message;
	private String sender;
	private List <String> recipients;
	
	public Message(LocalDateTime timestamp, String message,String sender,List<String> recipients) {
		this.timestamp = timestamp;
		this.message = message;
		this.sender = sender;
		this.recipients = recipients;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getMessage() {
		return message;
	}

	public String getSender() {
		return sender;
	}

	public List<String> getRecipients() {
		return recipients;
	}
	public String toString() {
		String s = "Sent at: " + timestamp + ", Sender: " + sender + "\nMessage Content: " + message + "\nRecipients: ";
		for (int i = 0; i < recipients.size()-1; i++) {  //used newlines to break up long messages/recipients
			s += recipients.get(i) + ", ";
		}
		s += recipients.get(recipients.size() - 1);
		return s;
	}
}
