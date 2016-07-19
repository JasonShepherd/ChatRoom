import javax.swing.*;
import java.awt.*;
import java.awt.event.*;




public class ServerGUI extends JFrame implements ActionListener, WindowListener {
	
	
	private JButton stopStart;								// stop/start button for server
	
	private JTextField sPortNumber;

	private Server server;
	
	
	
	ServerGUI(int port) {
		super("Server");
		server = null;
		
		JPanel north = new JPanel();
		north.add(new JLabel("Port number: "));
		sPortNumber = new JTextField("  " + port);
		north.add(sPortNumber);
		
		add(north, BorderLayout.NORTH);
		
		
		JPanel center = new JPanel(new FlowLayout());
		stopStart = new JButton("Start");
		stopStart.addActionListener(this);
		center.add(stopStart);	
		add(center);
		
		
		addWindowListener(this);
		setSize(200, 200);
		setVisible(true);
	}		

	
	
	
	
	public void actionPerformed(ActionEvent e) {
		
		if(server != null) {
			server.stop();
			server = null;
			sPortNumber.setEditable(true);
			stopStart.setText("Start");
			return;
		}
      
		int port;
		try {
			port = Integer.parseInt(sPortNumber.getText().trim());
		}
		catch(Exception er) {
		
			return;
		}
		
		server = new Server(port, this);
		
		new ServerRunning().start();
		stopStart.setText("Stop");
		sPortNumber.setEditable(false);
	}
	
	
	public static void main(String[] arg) {
		new ServerGUI(4990);				
	}

	
	public void windowClosing(WindowEvent e) {
		
		if(server != null) {
			try {
				server.stop();						}
			catch(Exception eClose) {
			}
			server = null;
		}
	
		dispose();
		System.exit(0);
	}
	
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	
	class ServerRunning extends Thread {
		public void run() {
			server.start();     
			stopStart.setText("Start");
			sPortNumber.setEditable(true);
			server = null;
		}
	}

}
