package core.impl;

import java.awt.BorderLayout;
import java.awt.AWTException;
import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

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
	private static Log log = LogFactory.getLog(Client.class);
	//Chat
    private Frame chatFrame;
    private List userList;
    private TextArea chatArea;
    private TextField chatField;
    private Button submitButton, logoutButton;
	//Login
	private JFrame clientLoginFrame;
	private JTextField name, ip, port;
	private JButton loginButton, downButton;
	private JLabel header;
	private JLabel labelName, labelIP, labelPORT, labelError;

	
	//Erzeugen des Loginframes.
	public void showLoginFrame(){
		clientLoginFrame = new JFrame("Chat-Client-Login");
		clientLoginFrame.setResizable(false);
		downButton = new JButton("Chat schließen");
		name = new JTextField();
		ip = new JTextField();
		port = new JTextField();
		
		loginButton = new JButton("Chat starten");
		header = new JLabel("Chat-Login");
		header.setFont(new Font("Impact", Font.BOLD,40));
		labelName = new JLabel("Bitte Nicknamen eingeben:");
		labelIP = new JLabel("Bitte IP eingeben:");
		labelPORT = new JLabel("Bitte Server-Port eingeben:");
		labelError = new JLabel("");
		JPanel panel = new JPanel();
		panel.add(header);panel.add(name);panel.add(ip);panel.add(port);panel.add(labelName);
		panel.add(labelIP);panel.add(labelPORT);panel.add(labelError);
		panel.add(loginButton);panel.add(downButton);
		
		downButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
            	clientLoginFrame.dispose();
                System.exit(0);
            }
        });
		
		header.setBounds(30,20,280,80); //(int x, int y, int width, int height) 
		
		labelName.setBounds(120,80,250,60);
		name.setBounds(280,95,100,30);
		name.setText("");
		
		labelIP.setBounds(120,120,250,60);
		ip.setBounds(280,135,100,30);
		ip.setText("127.0.0.1");
		
		labelPORT.setBounds(120,160,250,60);
		port.setBounds(280,175,100,30);
		port.setText("50000");
		
		loginButton.setBounds(170,220,180,30);
		downButton.setBounds(350,220,180,30);
		
        loginButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
				logMEin();
            }
        });
		enter(name, true);
		enter(ip, true);
		enter(port, true);

        clientLoginFrame.pack();
		clientLoginFrame.add(panel);
		panel.setLayout(null);
		clientLoginFrame.setSize(590, 325);
	    clientLoginFrame.setVisible(true);
		clientLoginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
	    //enter(chatField, false);
	    
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
        chatFrame.setResizable(false);
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
	
	
	//eigene Hilfsmethode Farbe für Chat
	private void Color(Panel ty) {
        ty.setBackground(Color.BLACK);
        ty.setForeground(Color.WHITE);
	}
	

	//eigene Hilfsmethode Enter-Taste
    private void enter(JTextField label, final boolean login) {
	    label.addKeyListener(new KeyAdapter() {
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

    
    //Hilfsmethode zum Check des Ports
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
    	if(message.contains("lol")) { //TODO: Erster Test: Smileys ersetzen und Schimpfwortfilter
    		message = message.replaceAll("lol", ":D");  //Funktionsweise bei Laufzeit testen und ggf. ï¿½ber ENUM-Klassen steuern
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
	    Border thickBorder = new LineBorder(Color.WHITE, 2);
	    name.setBorder(thickBorder);
	    ip.setBorder(thickBorder);
	    port.setBorder(thickBorder);
	    thickBorder = new LineBorder(Color.RED, 2);
	    
            boolean ok = true;
        	String error = "";
        	userName = name.getText();
            userIP = ip.getText();
            userPort = port.getText();
        	
        	if (name.getText().isEmpty()) {
        		error += " Name ";
        		ok = false;
        		name.setBorder(thickBorder);
            }
        	if (userIP.isEmpty() | !ValidateIPAddress(userIP)) {
            	error += "IP ";
            	ok = false;
        	    ip.setBorder(thickBorder);
	            }
    		if (!ValidatePort(userPort)) {
                	error += "Port ";
                	ok = false;
            	    port.setBorder(thickBorder);
    			}
            
            if (ok) {
            int portNumber = Integer.parseInt(userPort);
            clientLoginFrame.dispose();
            showChatFrame();
            login(userName,userIP,portNumber);
            }
        }
    
    
    //Methode Login und Logout zustï¿½ndig
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
    
    
	public void login(String userName, String remoteAdress, int remotePort) {
		System.out.println(userName + remoteAdress + remotePort);
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
