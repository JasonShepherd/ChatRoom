import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {									
	private static int uniqueId;						//Id  for connection
	private ArrayList<ClientThread> clientlist;			//array for client list
	private ServerGUI servergui;						// variable to hold instance of server GUI
	private SimpleDateFormat sdf;						// this is used to produce time  and date
	private int port;									// the port number to listen for connection
	private boolean driver;								// driver used to keep program running
	

	public Server(int port) {
		this(port, null);
	}
	
	public Server(int port, ServerGUI servergui) {
		this.servergui = servergui;
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");						// variable to hold time	
		clientlist = new ArrayList<ClientThread>();					//Arraylist of clients
	}
	
	public void start() {
		driver = true;													// set driver to true 
		
		
		try 
		{
			ServerSocket serverSocket = new ServerSocket(port);

			
			while(driver) 													// while loop is set to true
			{
				
				display("Server waiting for Clients on port " + port + ".");
				
				Socket socket = serverSocket.accept();  					// accept connection
				if(!driver)
					break;
				ClientThread t = new ClientThread(socket); 				 // produce thread 
				clientlist.add(t);											// save it in the ArrayList
				t.start();
			}
		
			try {
				serverSocket.close();
				for(int i = 0; i < clientlist.size(); ++i) {
					ClientThread tc = clientlist.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		
   
	protected void stop() {
		driver = false;
		
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			
		}
	}
	
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;

			System.out.println(time);
	}
	
	private synchronized void broadcast(String message) {				//broadcasts to clients
	
		String time = sdf.format(new Date());
		String messageLf = message+"\n"; 
		
		if(servergui == null)
			System.out.print(messageLf);
		else
			appendMessage(messageLf);   							  // append in the room window		
		
														// loops in reverse order in case we would have to remove a Client
																								// because it has disconnected
		for(int i = clientlist.size(); --i >= 0;) {
			ClientThread ct = clientlist.get(i);
			if(!ct.writeMsg(messageLf)) {
				clientlist.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	
												
 public	void appendMessage(String string) {											//appends message to clients in form of a string
		
	}
	
	
	
	
																					// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		for(int i = 0; i <clientlist.size(); ++i) {											//search for id to be removed			
			ClientThread ct = clientlist.get(i);
			// found it
			if(ct.id == id) {
				clientlist.remove(i);
				return;
			}
		}
	}
	
	
	public static void main(String[] args) {
		int portNumber = 4990;									//assigned port 
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}
		
		Server server = new Server(portNumber);
		server.start();
	}

	
	class ClientThread extends Thread {									//creates a thread per client instance
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		int id;
		String username;
		MessageObject messageobject;

	
		ClientThread(Socket socket) {								//thread constructor
			id = ++uniqueId;
			this.socket = socket;
			System.out.println(" creating Input/Output Streams");
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				username = (String) sInput.readObject();
				display(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			
			catch (ClassNotFoundException e) {}
          
		}

		public void run() {
			boolean keepGoing = true;
			while(keepGoing) {
				try {
					messageobject = (MessageObject) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				
				String message = messageobject.getMessage();
				
				switch(messageobject.getType()) {

				case MessageObject.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case MessageObject.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				case MessageObject.FRIENDS:
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					
					for(int i = 0; i < clientlist.size(); ++i) {
						ClientThread ct = clientlist.get(i);
						writeMsg((i+1) + ") " + ct.username +"\n");
						
			/*	case MessageObject.SENDING_FILE:								//trying to mess with this to send file
								byte[] myFile = cm.getFile();
								FileInputStream fileinputstream = null;
								BufferedInputStream bis = new BufferedInputStream(fileinputstream s);
								for(int i = clientlist.size(); --i >= 0;) {
								try {
								bis.read(myFile, 0, myFile.length);
								ct.write(myFile, 0, myFile.length);
								ct.flush();
								ct.close();
								ct.close();
								return;
								} catch (IOException ex) {
									}
								}
							} // End For*/

					}
					break;
					
					
				}
			}
			remove(id);
			close();
		}
		
		
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		
		private boolean writeMsg(String msg) {
		
			if(!socket.isConnected()) {
				close();
				return false;
			}
			
			try {
				sOutput.writeObject(msg);
			}
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}//end class
	
	
	
	
	
	
	
	


	
	
	
	
	
}

