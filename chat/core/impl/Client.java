package core.impl;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
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

public class Client implements ChatEventListener {
    private ClientCommunicator communicator;
    private String userName, userIP, userPort;
    private Frame loginFrame, chatFrame;
    private List userList;
    private TextArea chatArea;
    private TextField nameField, ipField, portField, chatField, errorField;
    private Button submitButton, logoutButton;
    ChatClientService chatClientService;
	private static Log log = LogFactory.getLog(Client.class);
	
    private void showLoginFrame() {
        loginFrame = new Frame();
        loginFrame.setLayout(new GridLayout(4, 2, 0, 20));    // 5 rows, 3 columns, gaps
        loginFrame.setResizable(false);
        loginFrame.setBounds(100, 100, 1000, 1000);
        loginFrame.setBackground(Color.BLACK);
        loginFrame.setForeground(Color.WHITE);

        loginFrame.add(new Label("Name:"));
        nameField = new TextField(30);
        loginFrame.add(nameField);
        nameField.setForeground(Color.BLACK);
        
        loginFrame.add(new Label("IP:"));
        ipField = new TextField(30);
		ipField.setText("127.0.0.1");
        loginFrame.add(ipField);
        ipField.setForeground(Color.BLACK);
        
        loginFrame.add(new Label("Port:"));
        portField = new TextField(30);
		portField.setText("50000");
        loginFrame.add(portField);
        portField.setForeground(Color.BLACK);

        Button loginButton = new Button("logMEin");
        loginButton.addMouseListener(new LoginListener());
        loginFrame.add(loginButton);
        loginButton.setForeground(Color.BLACK);
        
        errorField = new TextField(50);
        loginFrame.add(errorField);
        errorField.setBackground(Color.RED);
        errorField.setForeground(Color.WHITE);

        loginFrame.pack();
        loginFrame.setVisible(true);
        
        //Loginfenster lässt sich über das X schließen
        loginFrame.addWindowListener(new WindowAdapter(){
	        public void windowClosing(WindowEvent we){
	          System.exit(0);
	        }
	      });
    }
   

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
        chatPanel.add(chatArea, BorderLayout.SOUTH);

        Panel ownPanel = new Panel();
        Color(ownPanel);
        ownPanel.setLayout(new BorderLayout());
        ownPanel.add(new Label("What I want to say:"), BorderLayout.NORTH);
        chatField = new TextField(20);
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
        chatFrame.pack();
        chatFrame.setVisible(true);
        
        //Chatfenster lässt sich über das X schließen
        chatFrame.addWindowListener(new WindowAdapter(){
	        public void windowClosing(WindowEvent we){
	          System.exit(0);
	        }
	      });
    }
	
	//Hilfsmethode Farbe
	private void Color(Panel ty) {
        ty.setBackground(Color.BLACK);
        ty.setForeground(Color.WHITE);
	}

    @Override
    public void onMessage(String username, String message) {
        chatArea.append(username + ": " + message + "\n");
        chatFrame.setVisible(true);
    }

    @Override
    public void onUserListUpdate(String[] userList) {
        fillUserList(new Vector<String>(Arrays.asList(userList)));
        chatFrame.setVisible(true);
    }

    @Override
    public void onAction(int actionId, String reserved) {
        if (actionId == ChatAction.CHATACTION_USERNAME_SCHON_VERGEBEN) {

            chatArea
                .append("Der Username ist schon vergeben. Bitte loggen Sie sich aus und starten Sie die Anwendung neu");
        }

    }

    private void fillUserList(Vector<String> names) {
        userList.removeAll();
        for (int i = 0; i < names.size(); i++)
            if (names.get(i).equals(userName)) userList.add("<" + names.get(i) + ">");
            else userList.add(names.get(i) + " ");
    }

    public Client() {
        PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
        showLoginFrame();
		log.debug("<< Chat gestartet >>");
    }

    public static void main(String[] args) {
        new Client();
    }

    class LoginListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
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
            login(userName, port, userIP);
            }
            else {
            	errorField.setText("Bitte Eingaben kontrollieren:" + error);
            	new LoginListener();
            }
        }
    }
    
    //HilfsMethode
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
    
	public void login(String name, int port, String ip) {

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
