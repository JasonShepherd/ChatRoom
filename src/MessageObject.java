
import java.io.*;

public class MessageObject implements Serializable {


	
	static final int FRIENDS = 0;
	static final int MESSAGE = 1;
	static final int LOGOUT = 2;
	static final int SENDING_FILE = 3; 
	private int type;
	private String message;
	private byte[] file;   
	

	MessageObject(int type, String message) {				//constructor to send message
		this.type = type;
		this.message = message;
	}
	
	
	MessageObject(byte[] buffer) {							// constructor to send file
		file = buffer; 
		type = SENDING_FILE;
		
		        }

	
														
	int getType() {
		return type;
	}
	
	
	byte[] getFile() {									
		return file;
		
		    }

	String getMessage() {
		return message;
	}
}
