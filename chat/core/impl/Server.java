package core.impl;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.Enumeration;

import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import chatsession.ChatServerService;
import chatsession.ChatServerServiceFactory;
import chatsession.impl.ChatServerServiceFactoryImpl;

import core.impl.app.MyAppender;
import core.impl.app.TextAreaOutputStream;

public class Server { //implements ListAppenderListener 
	
	//Attribute//
	private static Log log = LogFactory.getLog(Server.class);
	static ChatServerServiceFactory factory;
	private JFrame consoleFrame;
	private JTextField port;
	private static JTextArea console;
	private JButton loginButton, logoutButton;
	private JLabel header;
	private JLabel label;

	public void showServerFrame(){
		consoleFrame = new JFrame("Chat-Server-Console");
		logoutButton = new JButton("Server herunterfahren");
		port = new JTextField();
		
		console = new JTextArea();
		JScrollPane consoleNew = new JScrollPane(console);

		loginButton = new JButton("Server starten");
		header = new JLabel("Chat-Server");
		header.setFont(new Font("Impact", Font.BOLD,40));
		label = new JLabel("Bitte Server-Port eingeben:");
		JPanel panel = new JPanel();
		panel.add(header);panel.add(port);panel.add(consoleNew);panel.add(label);
		panel.add(loginButton);panel.add(logoutButton);
		logoutButton.setBounds(70,200,90,30);
		
		logoutButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
            	servLogout();
            }
        });
		
		consoleNew.setPreferredSize(new Dimension(400,400));
		header.setBounds(30,20,280,80);
		label.setBounds(20,445,250,60);
		port.setBounds(180,460,100,30);
		consoleNew.setBounds(50,100,500,330);
		port.setText("50000");
		loginButton.setBounds(70,500,180,30);
		logoutButton.setBounds(250,500,180,30);
		
        loginButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
            	loginButton.setText("Server neustarten");
        		serverStart();
            }
        });
        
	    port.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                	serverStart();
                	}
            }

        });

        TextAreaOutputStream taos = new TextAreaOutputStream( console, 60 );
        PrintStream ps = new PrintStream( taos );
        System.setOut( ps );
        System.setErr( ps );
        
        consoleNew.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        console.enable(false);        
        consoleFrame.pack();
		consoleFrame.add(panel);
		panel.setLayout(null);
		consoleFrame.setSize(600, 600);
	    consoleFrame.setVisible(true);
		consoleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


	
    //Ctor, Logdatei einstellen + Loginframe anzeigen
    public Server() throws InterruptedException {
        PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
        showServerFrame();
        //Thread.sleep( 100 );
    }

    
    //MAIN
    public static void main(String[] args) throws InterruptedException {
    	new Server();
    	
		MyAppender a = new MyAppender(console);
		Logger root = Logger.getRootLogger();
		root.addAppender(a);
		log.debug("<< Server gestartet >>");
    }
    
    
    private void serverStart() {
		int serverPort = 0;
		serverPort = Integer.parseInt(port.getText());;
		
		if (port.getText().isEmpty() || port.getText().length() > 5 || serverPort <= 00000 || serverPort > 65535) {
			log.debug("<< Port falsch >>");
        	port.setBackground(Color.PINK);
        }
		else {
		servLogin(serverPort);
		}
    }
    
    
    private void servLogin(int serverport) {   	
    	try {
    	factory = new ChatServerServiceFactoryImpl();
    	factory.register(serverport);
    	log.debug("<< ServerCommunicator erwartet Clients >>");

    	while (true) {
    		ChatServerService service = factory.getSession();
    		ServerCommunicator communicator = new ServerCommunicator(service);
    	}
    	} catch (Exception e) {
    			e.printStackTrace();
    	}

}
	
    
	//Logout-Methode
	public void servLogout() {
		try {
			consoleFrame.dispose();
            System.exit(0);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
}