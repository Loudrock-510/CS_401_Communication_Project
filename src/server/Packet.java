package server;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class Packet implements Serializable {

	    private final Type type; 
	    private final String status;
	    private final List<Object> content;

	    public Packet(Type type, String status, List<Object> content) {
	        this.type = type;
	        this.status = status; 
	        this.content = content;
	    }
	    public Packet(Type type, String status) {
	        this.type = type;
	        this.status = status; 
	        this.content = null;
	    }

	    public Type getType() {
	        return type;
	    }

	    public String getStatus() {
	        return status;
	    }

	    public List <Object> getcontent() {
	        return content;
	    }

}

