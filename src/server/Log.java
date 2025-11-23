package server;

import java.time.LocalDateTime;
import java.util.*;

public class Log {
	private List<Message> log;
	
	//constructor
	public Log(List<Message> log) {
		this.log = log;
	}
	
	//return timestamp of messsage
	public LocalDateTime getTimeStamp(Message message) {
		return message.getTimestamp();
	}
	
	//return message text
	public String getMessage(Message message) {
		return message.getMessage();
	}
	
	//get sender
	public String getSentBy(Message message) {
		return message.getSender();
	}
	
	//get recipient
	public List<String> getRecipients(Message message){
		return message.getRecipients();
	}
	
	//add messge to log when receiving
	public void receiveMessage(Message message) {
		log.add(message);
	}
	
	
}
