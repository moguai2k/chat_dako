package core.impl;

import java.awt.BorderLayout;
import java.awt.AWTException;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import core.ChatEventListener;
import chatsession.ChatClientService;
import chatsession.ex.ChatServiceException;
import chatsession.impl.ChatClientServiceFactoryImpl;
import chatsession.pdu.ChatAction;

public class Client implements ChatEventListener {
	
	//Attribute//
	private ClientCommunicator clientCommunicator;
	private ChatClientService chatClientService;
	
	private String userName, userIP, userPort;	
    private Frame loginFrame, chatFrame;
    private List userList;
    private TextArea chatArea;
    private TextField nameField, ipField, portField, chatField, errorField;
    private Button submitButton, logoutButton;
	private static Log log = LogFactory.getLog(Client.class);
	
	
	//Erzeugen des Loginframes.
	private void showLoginFrame() {
        loginFrame = new Frame();
        loginFrame.setLayout(new GridLayout(4, 2, 0, 20));    // 4 rows, 2 columns, gaps
        loginFrame.setResizable(false);
        loginFrame.setBounds(100, 100, 1000, 1000);
        loginFrame.setBackground(Color.BLACK);
        loginFrame.setForeground(Color.WHITE);
        
        loginFrame.add(new Label("Name:"));
        nameField = new TextField(30);
        loginFrame.add(nameField);
        nameField.setForeground(Color.BLACK);
	    enter(nameField, true);
        
        loginFrame.add(new Label("IP:"));
        ipField = new TextField(30);
		ipField.setText("127.0.0.1");
        loginFrame.add(ipField);
        ipField.setForeground(Color.BLACK);
	    enter(ipField, true);
        
        loginFrame.add(new Label("Port:"));
        portField = new TextField(30);
		portField.setText("50000");
        loginFrame.add(portField);
        portField.setForeground(Color.BLACK);
	    enter(portField, true);
	    
		Button loginButton = new Button("logMEin");
		loginButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				logMEin();
			}
		});
		
        loginFrame.add(loginButton);
        loginButton.setForeground(Color.BLACK);
        
        loginFrame.addWindowListener(new WindowAdapter(){
	        public void windowClosing(WindowEvent we){
	          System.exit(0);
	        }
	      });
        
        errorField = new TextField(50);
        errorField.enable(false);
        loginFrame.add(errorField);
        errorField.setBackground(Color.PINK);
        errorField.setForeground(Color.BLACK);
        
        loginFrame.pack();
        loginFrame.setVisible(true);
    }
    

	//Erzeugen des Chatframes
	private void showChatFrame() {
        Panel usersPanel = new Panel();
        Color(usersPanel);
        usersPanel.setLayout(new BorderLayout());
        usersPanel.add(new Label("Current users:"), BorderLayout.NORTH);
        userList = new List(5, false);
        userList.setBackground(Color.BLACK);
        userList.setForeground(Color.WHITE);
        usersPanel.add(userList, BorderLayout.SOUTH);
        
        Panel chatPanel = new Panel();
        Color(chatPanel);
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(new Label("Chat:"), BorderLayout.NORTH);
        chatArea = new TextArea(10, 20);
        chatArea.setBackground(Color.BLACK);
        chatArea.setForeground(Color.WHITE);
        chatArea.setEditable(false);
        chatPanel.add(chatArea, BorderLayout.SOUTH);
        
        Panel ownPanel = new Panel();
        Color(ownPanel);
        ownPanel.setLayout(new BorderLayout());
        ownPanel.add(new Label("What I want to say:"), BorderLayout.NORTH);
        chatField = new TextField(30);
	    enter(chatField, false);
	    
        ownPanel.add(chatField, BorderLayout.CENTER);
        ownPanel.setForeground(Color.BLACK);
        Panel buttonsPanel = new Panel();
        submitButton = new Button("submit");
        submitButton.addMouseListener(new ChatListener());
        buttonsPanel.add(submitButton);
        submitButton.setForeground(Color.BLACK);
        logoutButton = new Button("logout");
        logoutButton.addMouseListener(new ChatListener());
        buttonsPanel.add(logoutButton);
        logoutButton.setForeground(Color.BLACK);
        ownPanel.add(buttonsPanel, BorderLayout.SOUTH);

        chatFrame = new Frame();
        chatFrame.setLayout(new BorderLayout());
        chatFrame.add(usersPanel, BorderLayout.NORTH);
        chatFrame.add(chatPanel, BorderLayout.CENTER);
        chatFrame.add(ownPanel, BorderLayout.SOUTH);
	    chatFrame.addWindowListener(new WindowAdapter(){
	        public void windowClosing(WindowEvent we){
	          System.exit(0);
	        }
	      });

        chatFrame.pack();
        chatFrame.setVisible(true);
    }
	
	
	//eigene Hilfsmethode Farbe
	private void Color(Panel ty) {
        ty.setBackground(Color.BLACK);
        ty.setForeground(Color.WHITE);
	}
	
	
	//eigene Hilfsmethode Enter-Taste
    private void enter(TextField field, final boolean login) {
	    field.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                	if(login){
                		logMEin();
                	}
                	else {
                        clientCommunicator.tell(userName, chatField.getText());
                        chatField.setText("");
                	}
                }
            }
        });
	}
    
    
    //Hilfsmethode zum Check der IP-Adresse - IP-Pattern aus http://forums.sun.com/thread.jspa?threadID=584205&start=15&tstart=0
    public final static boolean ValidateIPAddress(String  str)
    {
    	final String IP_PATTERN = "^((\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";
		
    	if (str.matches(IP_PATTERN))
    		return true;
    	else
    		return false;
    }

    
    //Hilfsmethode zum Check des Ports //TODO: Mit der Server-Port-Check-Methode abgleichen, kleine Redundanz
    public boolean ValidatePort(String userPort) {
		try {
			int port = Integer.parseInt(userPort); //String -> int
			if (userPort.length() > 1 && userPort.length() < 6 && port >= 00000 && port < 65536)
				return true;
			else return false;
			} catch (Exception e) {
				return false;
			}
    }
    

    //Nachricht und Name werden empfangen und zusammen mit der Serverzeit im Chat eingetragen
    public void onMessage(String username, String message, String time) {
    	if(message.contains(";)")) { //TODO: Erster Test: Smileys ersetzen und Schimpfwortfilter
    		message = message.replaceAll(";)", ";-)");  //Funktionsweise bei Laufzeit testen und ggf. �ber ENUM-Klassen steuern
    	}
        chatArea.append("(" + time + ")" + " " + username + ": " + message + "\n");
        chatFrame.setVisible(true);
    }


    //Userliste wird an fillUserList weitergegeben
    public void onUserListUpdate(String[] userList) {
        fillUserList(new Vector<String>(Arrays.asList(userList)));
        chatFrame.setVisible(true);
    }

    
    //aktualisiert die Userliste
    private void fillUserList(Vector<String> names) {
        userList.removeAll();
        for (int i = 0; i < names.size(); i++)
            if (names.get(i).equals(userName)) userList.add("<" + names.get(i) + ">");
            else userList.add(names.get(i) + " ");
    }   
    
    
    //Wenn der Name schon vorhanden ist -> Errormeldung im Chat
    public void onAction(int actionId, String reserved) {
        if (actionId == ChatAction.CHATACTION_USERNAME_SCHON_VERGEBEN) {
            chatArea.append("Der Username ist schon vergeben. Bitte loggen Sie sich aus und starten Sie die Anwendung neu!");
        }
    }


    //Ctor, Logdatei einstellen + Loginframe anzeigen
    public Client() {
        PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
        showLoginFrame();
		log.debug("Chat gestartet");
    }

    //MAIN
    public static void main(String[] args) throws AWTException {
        new Client();
    }

    
    //Login-Methode (vorher LoginListener)
    public void logMEin() {
            boolean ok = true;
        	String error = "";
        	userName = nameField.getText();
            userIP = ipField.getText();
            userPort = portField.getText();
        	
        	if (nameField.getText().isEmpty()) {
            error += " Name ";
            ok = false;
            }

        	if (userIP.isEmpty() | !ValidateIPAddress(userIP)) {
            	error += "IP ";
            	ok = false;
	            }

    		if (!ValidatePort(userPort)) {
                	error += "Port ";
                	ok = false;
    			}
            
            if (ok) {
            int port = Integer.parseInt(userPort);
            loginFrame.dispose();
            showChatFrame();
            login(userName,userIP,port);
            }
            else {
            	errorField.setText("Bitte Eingaben kontrollieren:" + error);
            }
        }
    
    
    //Methode Login und Logout zust�ndig
    class ChatListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == submitButton) {
                clientCommunicator.tell(userName, chatField.getText());
                chatField.setText("");
            } else if (e.getSource() == logoutButton) {
                clientCommunicator.logout();
				chatFrame.dispose();
                System.exit(0);
            }
        }
    }
    
    
    //TODO: �berarbeiten !! + Port evtl. dynamisch gestalten?
	public void login(String userName, String remoteAdress, int remotePort) {
		ChatClientServiceFactoryImpl clServFac = new ChatClientServiceFactoryImpl();	
		try {
			chatClientService = clServFac.register(50000); //Registriet mal fest 50000 als Listener
			chatClientService.create(remoteAdress, remotePort, userName); //Baut eine Session mit dem angegeben Partner auf
			clientCommunicator = new ClientCommunicator(this, chatClientService, userName);
			} catch (ChatServiceException e) {
				new ChatServiceException("Verbindungsfehler.");
			}
	}

} // Client
