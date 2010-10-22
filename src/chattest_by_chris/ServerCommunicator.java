package chattest_by_chris;

//wichtigste imports
import java.net.*; //Socket, etc
import java.io.*; //Ein und Ausgaben, etc
import java.util.*; //Listen, etc

//alte improts
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

//imports welche inkludiert werden müssen
/*import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import chatsession.ChatServerService;
import chatsession.ChatServerServiceFactory;
import chatsession.ex.ChatServiceException;
import chatsession.impl.ChatServerServiceFactoryImpl;
import chatsession.listener.ChatServerListener;
import chatsession.pdu.ChatAction;
import chatsession.pdu.ChatMessage;
import chatsession.pdu.ChatUserList;*/

//ServerCommunicator-Klasse soll als eigenständiger Thread laufen, daher runnable
public class ServerCommunicator extends Thread implements Runnable {
	//Variablen
	public static final int PORT = 8205; //unser PORT
	private ServerSocket serverSocket; //Server-Socket (Verbindungslauscher)
	private ArrayList connections; //darin werden die Verbindungen gelistet
	Thread thread; //speichert den Thread der Klasse
	
	//unwichtige Variablen?
/*	private static Log log = LogFactory.getLog(ServerCommunicator.class);
	private static ChatServerServiceFactory factory;
	private static ConcurrentHashMap<String, ChatServerService> sessions = new ConcurrentHashMap<String, ChatServerService>();
	private ChatServerService chatServerService;*/

	
	//Main-Methode
	public static void main(String args[]) {
		//alter Code, dieses wurde nun in den Ctor geschrieben
/*		try {
			PropertyConfigurator.configureAndWatch("log4j.properties",60 * 1000);
			if (args != null) PORT = Integer.parseInt((args[0]));
			factory = new ChatServerServiceFactoryImpl();
			factory.register(PORT);
			System.out.println("ServerCommunicator waiting for clients...");

			while (true) {
				ChatServerService service = factory.getSession();

				ServerCommunicator communicator = new ServerCommunicator(
						service);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		new ServerCommunicator(); //neue Instanz unserer Klasse -> Ctor wird aufgerufen
	}

	//Ctor
	public ServerCommunicator() { //vorher paramter ChatServerService chatServerService
		//Socket für PORT wird eröffnet
		try
		{
		    System.out.println("ServerCommunicator wird gestartet...");
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e)
		{
			System.err.println("Fehler beim Erzeugen der Sockets:"+e);
			System.exit(1); //Terminates the currently running Java Virtual Machine.  0=normal exit, 1=abnormal exit
		}

		//Liste für Verbidungen wird erstellt
		connections = new ArrayList(); //ArrayList durch Vector ersetzen, da nicht threadsave !!!!!!!!!!!!!!!

		//Thread wird erstellt
		thread = new Thread(this);
		//Thread wird gestartet
		thread.start();

		
		//alter Ctor
/*		this.chatServerService = chatServerService;
		try {
			onLogin(chatServerService.getUserName());
			chatServerService.registerChatSessionListener(this);
		} catch (ChatServiceException e) {
			log.error(e);
		}*/

	}
	
	//Es wird ständig gecheckt ob Verbindungsanfragen vorhanden sind (endlos)
	//Bei Eingang wird ein Objekt erzeugt. Ein neuer Thread.
	public void listener()
	{
		try
		{
			while(true)
			{
				Socket client = serverSocket.accept(); //accept() =   Listens for a connection to be made to this socket and accepts it.

				connection ev = new connection(this, client); //Erstellt ein neues Objekt aka Verbindung
				connections.add(ev); //Fügt die Verbindung der Liste hinzu.
			}
		} catch (IOException e)
		{
			System.err.println("Fehler beim Warten auf Verbindungen:"+e);
			System.exit(1); //Terminates the currently running Java Virtual Machine. 0=normal exit, 1=abnormal exit
		}
	}

	
	//Senden@Alle-Methode welche alle offnen Verbindungen checkt und die Nachricht an sie schickt.
	public void sendToEveryone(String message) {
		connection event;

		for (int i = 0; i < connections.size(); i++)
		{
			event = (connection) connections.get(i);
			event.out.println(message);
		}
		
		
		//Alter Code
/*		Enumeration<String> keys = sessions.keys();
		for (Entry<String, ChatServerService> entry : sessions.entrySet()) {
			String user = entry.getKey();
			try {
				entry.getValue().sendMessage(message);
			} catch (ChatServiceException e) {
				// Session ist nicht mehr valide --> ausloggen
				log.error("Error sending to " + user + ", logging out " + user);
				onLogout(user);
			}
		}*/
	}



	
	//User-Liste aktualisieren - nicht editiert
/*	public void sendUserlistUpdate() {
		Enumeration<String> keys = sessions.keys();
		ArrayList<String> list = new ArrayList<String>();
		while (keys.hasMoreElements()) {
			list.add(keys.nextElement());
		}
		String[] array = new String[list.size()];
		list.toArray(array);
		ChatUserList userList = new ChatUserList(array);

		for (Entry<String, ChatServerService> entry : sessions.entrySet()) {
			try {
				entry.getValue().sendUserList(userList);
			} catch (ChatServiceException e) {
				// Session ist nicht mehr valide --> ausloggen
				log.error("Error sending to " + entry.getKey()
						+ ", logging out " + entry.getKey());
				onLogout(entry.getKey());
			}
		}
	}*/

/*	@Override
	public void onActionEvent(ChatAction action) {
		// TODO bisher noch keine Actions definiert

	}*/

/*	//Login-Methode - nicht editiert
	public void onLogin(String username) {
		if (!sessions.containsKey(username)) {
			sessions.put(username, chatServerService);
			sendUserlistUpdate();
		} else {
			ChatAction action = new ChatAction(
					ChatAction.CHATACTION_USERNAME_SCHON_VERGEBEN);
			try {
				chatServerService.sendAction(action);
			} catch (ChatServiceException e) {
				log.error(e);

			}
		}

	}*/

/*	//Logout-Methode  - nicht editiert
	@Override
	public void onLogout(String username) {
		log.trace("Logging out " + username);

		if (sessions.containsKey(username)) {
			sessions.remove(username);
			sendUserlistUpdate();
		}

	}*/

/*	@Override
	public void onMessageEvent(ChatMessage message) {

		if (message.getMessage() != null) {
			sendToEveryone(message);
		}
	}*/

} // ServerCommunicator
