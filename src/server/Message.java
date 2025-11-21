package server;

import java.time.LocalDateTime;
import java.util.List;

public class Message {
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
		for (int i = 0; i < recipients.length()-1; i++) {
			s += recipients[i] + ", "
		}
		s += recipients[recipients.length() - 1];
		return s;
	}
}
