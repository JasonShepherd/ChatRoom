
import java.net.*;
import java.io.*;
import java.util.*;


public class Client  {

	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;

	
	private ClientGUI clientgui;
	
	
	private String server;
	private String username;
	private int port;

	
		Client(String server, int port, String username) {
		this(server, port, username, null);
	}


	Client(String server, int port, String username, ClientGUI clientgui) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.clientgui = clientgui;
	}
	

	public boolean start() {
		try {
			socket = new Socket(server, port);
		} 
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);

		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		 
		new ListenFromServer().start();
		
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		
		return true;
	}

	
	private void display(String msg) {
		if(clientgui == null)					
			System.out.println(msg);      
		else
			clientgui.append(msg + "\n");		// append to the ClientGUI JTextArea 
	}
	
  public void sendMessage(MessageObject msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} 
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} 
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} 
		
		if(clientgui != null)
			clientgui.connectionFailed();
			
	}
	
	
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					
					if(clientgui == null) {
						System.out.println(msg);
					}
					else {
						clientgui.append(msg);
					}
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					if(clientgui != null) 
						clientgui.connectionFailed();
					break;
				}
				
				catch(ClassNotFoundException e2) {}
			}
		}
	}
}
