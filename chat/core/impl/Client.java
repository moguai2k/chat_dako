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
import chatsession.ChatClientServiceFactory;
import chatsession.ex.ChatServiceException;
import chatsession.impl.ChatClientServiceFactoryImpl;
import chatsession.impl.ChatClientServiceImpl;
import chatsession.pdu.ChatAction;

import java.util.Date; 
import java.text.*; 

public class Client implements ChatEventListener {
	
	//Attribute//
	private ClientCommunicator communicator;	
	private String userName, userIP, userPort;	
    private Frame loginFrame, chatFrame;
    private List userList;
    private TextArea chatArea;
    private TextField nameField, ipField, portField, chatField, errorField;
    private Button submitButton, logoutButton;
    ChatClientService chatClientService;
	private static Log log = LogFactory.getLog(Client.class);
	private ServerCommunicator server;
	
	//Erzeugen des Loginframes. Festlegen der Elemente die beim Login benötigt werden, sowie Größe,
	//Farbe usw. Desweiteren wird hier die Funktionalitäten des Loginframes festgelegt.
    @SuppressWarnings("Textfeld diable")
    //TODO: Größe + Farbschema ändern + (Für IP evtl dropdown mit Favoriten) + (Evtl Registrierung am Server mit
    //Password generierung) -- Status: vorrübergehend Fertig ! 
	private void showLoginFrame() {
    	
    	//Erzeugen des Loginframes (Layout,Größe,Form)
        loginFrame = new Frame();
        loginFrame.setLayout(new GridLayout(4, 2, 0, 20));    // 4 rows, 2 columns, gaps
        loginFrame.setResizable(false);
        loginFrame.setBounds(100, 100, 1000, 1000);
        loginFrame.setBackground(Color.BLACK);
        loginFrame.setForeground(Color.WHITE);
        
        //Hinzufügen vom Textfeld mit dem Label "Name"
        loginFrame.add(new Label("Name:"));
        nameField = new TextField(30);
        loginFrame.add(nameField);
        nameField.setForeground(Color.BLACK);
	    enter(nameField, true);
        
	    //Hinzufügen vom Textfeld mit dem Label "IP"
        loginFrame.add(new Label("IP:"));
        ipField = new TextField(30);
		ipField.setText("127.0.0.1");
        loginFrame.add(ipField);
        ipField.setForeground(Color.BLACK);
	    enter(ipField, true);
        
	    //Hinzufügen vom Textfeld mit dem Label "Port"
        loginFrame.add(new Label("Port:"));
        portField = new TextField(30);
		portField.setText("50000");
        loginFrame.add(portField);
        portField.setForeground(Color.BLACK);
	    enter(portField, true);
	    
	    //Erzeugen vom "Einloggen" button ( inkl. Mouselistener )
		Button loginButton = new Button("logMEin");
		loginButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				logMEin();
			}
		});
		
		//Hinzufügen vom "Login Button" 
        loginFrame.add(loginButton);
        loginButton.setForeground(Color.BLACK);
	    
        //Funktion macht es möglich das Fenster über X zu schließen
        loginFrame.addWindowListener(new WindowAdapter(){
	        public void windowClosing(WindowEvent we){
	          System.exit(0);
	        }
	      });
        
        //Hinzufügen des Errorfeldes 
        errorField = new TextField(50);
        loginFrame.add(errorField);
        errorField.setBackground(Color.PINK);
        errorField.setForeground(Color.BLACK);
        errorField.enable(false);

        //Sichtbarkeit des ganzen Frames sowie Aussehen feslegen
        loginFrame.pack();
        loginFrame.setVisible(true);
    }
    
    //Status: Vorrübergehend Fertig !
    //TODO: andere Schriftarten wählen + URL´s schön einbinden + smiley Button
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
	
//eigene Hilfsmethode Uhrzeit (TODO FÜR SERVERCOMMUNICATOR + GETTER)
//	public static String time(){
//	DateFormat shortTime = DateFormat.getTimeInstance(DateFormat.SHORT); 
//	return shortTime.format(new Date()).toString(); 
//	}
	
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
                        communicator.tell(userName, chatField.getText());
                        chatField.setText("");
                	}
                }
            }
        });
	}
    
    //eigene Hilfsmethode zum Check ob Buchstaben oder Zahlen in IP/Port - nicht perfekt
    public boolean containsOnlyNumbers(String str, String kind) {
        
        if (str == null || str.length() == 0)
            return false;
        
        for (int i = 0; i < str.length(); i++) {

        	if(kind == "ip") {
        		if (!Character.isDigit(str.charAt(i)) && str.charAt(i) != '.') 
        			return false;
        	}
        	else {
        		if (!Character.isDigit(str.charAt(i))) 
        			return false;
        	}
        }
        
        return true;
    }

    @Override // Status : grundsätzlich Fertig !
    public void onMessage(String username, String message) {
    	if(message.contains(";)")) { //TODO: Erster Test: Smileys ersetzen und Schimpfwortfilter
    		message.replaceAll(";)", ";-)");
    	}
        chatArea.append("(" + server.getTime() + ")" + " " + username + ": " + message + "\n");
        chatFrame.setVisible(true);
    }
    
    @Override //TODO: Status : ?
    public void onUserListUpdate(String[] userList) {
        fillUserList(new Vector<String>(Arrays.asList(userList)));
        chatFrame.setVisible(true);
    }
    
    //Status: vorerst Fertig !
    private void fillUserList(Vector<String> names) {
        userList.removeAll();
        for (int i = 0; i < names.size(); i++)
            if (names.get(i).equals(userName)) userList.add("<" + names.get(i) + ">");
            else userList.add(names.get(i) + " ");
    }   
    
    
    @Override //Status: vorerst Fertig!
    public void onAction(int actionId, String reserved) {
        if (actionId == ChatAction.CHATACTION_USERNAME_SCHON_VERGEBEN) {
            chatArea.append("Der Username ist schon vergeben. Bitte loggen Sie sich aus und starten Sie die Anwendung neu!");
        }
    }

    // Status: voerst Fertig !
    public Client() {
        PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
        showLoginFrame();
		log.debug("<< Chat gestartet >>");
    }

    //MAIN ( Status: voerst Fertig ! )
    public static void main(String[] args) throws AWTException {
        new Client();
    }

    //TODO: neue Login-Methode (vorher LoginListener) ( Status: überarbeiten - Fehlerhandling bei IP und beim Port )
    public void logMEin() {
            boolean ok = true;
        	String error = "";
        	
        	if (nameField.getText().isEmpty()) {
            error += " Name ";
            ok = false;
            }

            if (ipField.getText().isEmpty() | !containsOnlyNumbers(ipField.getText(),"ip")) {
            	error += "IP ";
            	ok = false;
            }

            if (portField.getText().isEmpty() | !containsOnlyNumbers(portField.getText(),"port")) { //absichern dass es nur zahlen sind usw...
            	error += "Port ";
            	ok = false;
            }

            if (ok) {
        	userName = nameField.getText();
            userIP = ipField.getText();
            userPort = portField.getText();
            int port = Integer.parseInt(userPort); //String -> int

            loginFrame.dispose();
            showChatFrame();
            login(userName, userIP, port);
            }
            else {
            	errorField.setText("Bitte Eingaben kontrollieren:" + error);
            }
        }
    
    //Methode Login und Logout zuständig  Status: Fertig
    class ChatListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == submitButton) {
                communicator.tell(userName, chatField.getText());
                chatField.setText("");
            } else if (e.getSource() == logoutButton) {
                communicator.logout(userName);
                System.exit(0);
            }
        }
        
    }
    
    //TODO: Status: zu überarbeiten !!
	public void login(String name, String ip, int port) {

		ChatClientServiceFactory chatclientservicefactory = new ChatClientServiceFactoryImpl();
		chatClientService = new ChatClientServiceImpl();
		
		try {
			chatclientservicefactory.register(port); //Registriert einen ChatClientService an einem lokalen port und gibt das ChatClientService-Objekt zur+ck
			chatClientService.create(ip, port, name); //TODO: dynamisch. //Baut eine Session mit dem angegeben Partner auf
			communicator = new ClientCommunicator(this, chatClientService, name);
			} catch (ChatServiceException e) {
				new ChatServiceException("Verbindungsfehler.");
			}

	}

} // Client
