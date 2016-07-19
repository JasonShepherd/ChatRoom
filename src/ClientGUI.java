import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;



public class ClientGUI extends JFrame implements ActionListener {

	
																		//variables used for containers
	private JLabel label;
	private JTextField textfield;
	private JTextField tfServer;
	private JTextField tfPort;
	private JTextArea textarea;
		
																		//variables for buttons
	private JButton login;
	private JButton logout;
	private JButton friends;
	private JButton sendfile;
	
	
	
	private boolean connected;											
																				// the Client object
	private Client client;
	
	private int defaultPort;
	private String defaultHost;

																//create gui
	ClientGUI(String host, int port) {

		super("Chat Client");												//gui label
		defaultPort = port;
		defaultHost = host;
		
		
		JPanel southPanel = new JPanel(new GridLayout(3,1));					/* south panel will hold friends online , send file and text field*/
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);											/*also adds each item onto the panel sets orientation and attributes*/						
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
		friends = new JButton("friends online");
		friends.addActionListener(this);
		friends.setEnabled(false);											
		sendfile = new JButton("send file");
		sendfile.addActionListener(this);
		sendfile.setEnabled(false);																			
		label = new JLabel("Enter your name", SwingConstants.CENTER);				
		southPanel.add(label);
		textfield = new JTextField(" ");
		textfield.setBackground(Color.WHITE);
		southPanel.add(textfield);
		add(southPanel, BorderLayout.SOUTH);				
		southPanel.add(friends);
		southPanel.add(sendfile);

																			
		textarea = new JTextArea("login to talk to a friend\n", 80, 80);					//does same thing as south panel this is where messages are displayed
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(textarea));
		textarea.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

																							//login logout button
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);		
		JPanel northPanel = new JPanel();
		northPanel.add(login);
		northPanel.add(logout);
		add(northPanel, BorderLayout.NORTH);

																					/* sets size of application set visibility true allows for rendering */
		setDefaultCloseOperation(EXIT_ON_CLOSE);										/*requests focus to get the input	*/
		setSize(500, 500);
		setVisible(true);
		requestFocusInWindow();

	}

	
	void append(String str) {
		textarea.append(str);
		textarea.setCaretPosition(textarea.getText().length() - 1);
	}
	
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		friends.setEnabled(false);
		sendfile.setEnabled(false);
		label.setText("Enter your name");
		textfield.setText(" ");
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		textfield.removeActionListener(this);
		connected = false;
	}
		
	
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if(o == logout) {
			client.sendMessage(new MessageObject(MessageObject.LOGOUT, ""));
			return;
		}
		
		if(o == friends) {
			client.sendMessage(new MessageObject(MessageObject.FRIENDS, ""));				
			return;
		}
		if(o == sendfile){	
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
			File myFile = new File( chooser.getSelectedFile().getAbsolutePath() );
			byte[] mybytearray = new byte[(int) myFile.length()];
			client.sendMessage(new MessageObject(mybytearray));
			}
			return;
			


		}


		
		if(connected) {
			client.sendMessage(new MessageObject(MessageObject.MESSAGE, textfield.getText()));				
			textfield.setText("");
			return;
		}
		

		if(o == login) {
			
			String username = textfield.getText().trim();
			
			if(username.length() == 0)
				return;
			
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;   
			}

		
			client = new Client(server, port, username, this); 
			
			if(!client.start()) 
				return;
			textfield.setText("");
			label.setText("Type text ->");
			connected = true;
			
			
			login.setEnabled(false);
			logout.setEnabled(true);
			friends.setEnabled(true);
			sendfile.setEnabled(true);
			textfield.addActionListener(this);
			
		}

	}

	
	public static void main(String[] args) {
		new ClientGUI( "192.168.1.110", 4990);			//was localhost
	}

}
