package core.impl;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import chatsession.ChatServerService;
import chatsession.ChatServerServiceFactory;
import chatsession.ex.ChatServiceException;
import chatsession.impl.ChatServerServiceFactoryImpl;
import core.MyAppender;

public class Server {
	
	//Attribute//
	private static Log log = LogFactory.getLog(Server.class);
	static ChatServerServiceFactory factory;
	private ServerCommunicator communicator;
	private JFrame consoleFrame;
	private JTextField port;
	private JButton loginButton, logoutButton;// sendUserlist;
	private JLabel header;
	private JLabel label;
	private Thread thread;
	private static JList jList;
	private static DefaultListModel defaultListModel;
	private JScrollPane jScrollPane;
	private boolean runs = false; //rennt der Server?

	
	//Server-Frame
	public void showServerFrame(){
		// Tore eingefügt
		//sendUserlist = new JButton("Liste aktualis.");
		//sendUserlist.addActionListener(new ActionListener() {
			
			//@Override
			//public void actionPerformed(ActionEvent event) {
			//	communicator.sendUserlistUpdate();
			//	
			//}
		// });
		// bis hier. Testweise, da Userlists manchmal bisschen verkacken. Also neu senden.
		
		consoleFrame = new JFrame("Chat-Server");
		consoleFrame.setResizable(false);
		logoutButton = new JButton("Server herunterfahren");
		port = new JTextField();
		
	    defaultListModel = new DefaultListModel();
	    jList = new JList();
	    jList.setModel(defaultListModel);
	      
	    jScrollPane = new JScrollPane(jList);
	    jScrollPane.setViewportView(jList);
		
		loginButton = new JButton("Server starten");
		header = new JLabel("Chat-Server");
		header.setFont(new Font("Impact", Font.BOLD,40));
		label = new JLabel("Bitte Server-Port eingeben:");
		JPanel panel = new JPanel();
		panel.add(header);panel.add(port);panel.add(label);panel.add(jScrollPane);
		panel.add(loginButton);panel.add(logoutButton); 
		// Tore
		// panel.add(sendUserlist);
		//
		logoutButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
            	servLogout();
            }
        });

		header.setBounds(30,20,280,80); //(int x, int y, int width, int height) 
		label.setBounds(120,445,250,60);
		port.setBounds(280,460,100,30);
		jScrollPane.setBounds(20,100,690,330);
		port.setText("50000");
		loginButton.setBounds(170,500,180,30);
		logoutButton.setBounds(350,500,180,30);
		// auch Tore
		// sendUserlist.setBounds(530,500,120,30);
		//
        loginButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
            	if(!runs) {
                	loginButton.setText("Server läuft!");
                	loginButton.disable();
            		serverStart();
            	}
            	runs = true;
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

        consoleFrame.pack(); //1.
		consoleFrame.setSize(740, 600); //1.
	    consoleFrame.setLocationRelativeTo(null); //2. Zentrierung
		consoleFrame.add(panel);
		panel.setLayout(null);
	    consoleFrame.setVisible(true); //3.
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
    	
        // Log4J mit eigenem Appender starten, intialisieren, layouten
        PatternLayout pl = new PatternLayout();
        pl.setConversionPattern( "%d{HH:mm:ss}  |  %F:%L  |  %m%n" );        
        MyAppender cca = new MyAppender(jList,defaultListModel);
        cca.setLayout( pl );
        Logger rl = Logger.getRootLogger(); 
        rl.addAppender( cca ); 
        
		log.debug("Server gestartet");
    }
    
    
    //Server starten
    private void serverStart() {
		int serverPort = 0;
		
		if (port.getText().length() > 1 && port.getText().length() < 6 && containsOnlyNumbers(port.getText())) {
			serverPort = Integer.parseInt(port.getText());;
			if (serverPort >= 00000 && serverPort < 65536) {
	        	port.setBackground(Color.WHITE);
				servLogin(serverPort);
			}
        }
		else {
			log.debug("Port falsch");
        	port.setBackground(Color.PINK);
		}
    }
    
    
    //Server-Login
    private void servLogin(int serverport) {   	
    	try {
        	factory = new ChatServerServiceFactoryImpl();
			factory.register(serverport);
	    	log.debug("ServerCommunicator erwartet Clients");
		} catch (ChatServiceException e) {
			e.printStackTrace();
		}

    	StartStopThread();

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


	//Hilfsmethode
    public boolean containsOnlyNumbers(String str) {
        if (str == null || str.length() == 0)
            return false;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        return true;
    }
    
    
    //Threading ServerGui
	public void StartStopThread()
	{
	if (thread == null)
	{
		thread = new Thread(new Runnable()
	{

			
			//Threading RUNit
			public void run()
			{
				try {
					while (true) {
						ChatServerService service = factory.getSession();
						if (service != null) {communicator = new ServerCommunicator(service);}
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	thread.start();

	} else
	{
		thread = null;
		thread.interrupt();
	}
	}
} //Server