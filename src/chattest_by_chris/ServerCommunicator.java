package chattest_by_chris;

//wichtigste imports
import java.net.*; //Socket, etc
import java.io.*; //Ein und Ausgaben, etc
import java.util.*; //Listen, etc


//ServerCommunicator-Klasse soll als eigenständiger Thread laufen, daher runnable
public class ServerCommunicator implements Runnable {
	
	//Variablen
	public static final int PORT = 8205; //unser PORT
    //String[] userList; //selbsterklärend, für Client zum Abruf
	private ServerSocket serverSocket; //Server-Socket (Verbindungslauscher)
	private Vector<connection> connections; //darin werden die Verbindungen gelistet, Vector anstelle ArrayList, da threadsafe -> synchronized
	Thread thread; //speichert den Thread der Klasse
	
	
	//Main-Methode
	public static void main(String args[]) {
		new ServerCommunicator(); //neue Instanz unserer Klasse -> Ctor wird aufgerufen
	}

	//Ctor
	public ServerCommunicator() {
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
		connections = new Vector<connection>();

		//Thread wird erstellt
		thread = new Thread(this);
		//Thread wird gestartet
		thread.start();

	}
	
	//Es wird ständig gecheckt ob Verbindungsanfragen vorhanden sind (endlos Listener)
	//Bei Eingang wird ein Objekt erzeugt. Ein neuer Thread. Muss run heißen wegen runnable.
	public void run()
	{
		try
		{
			while(true)
			{
				Socket client = serverSocket.accept(); //accept() =   Listens for a connection to be made to this socket and accepts it.

				connection ev = new connection(this, client); //Erstellt ein neues Objekt aka Verbindung
				connections.addElement(ev); //Fügt die Verbindung der Liste hinzu.
				//TODO: onLogin()
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
			event = (connection) connections.elementAt(i);
			event.out.println(message);
		}
		
		
	}
	
	//Login-Methode
	public void onLogin(connection conn) { //vllt auch über "String username" arbeiten..
		if (connections.size() != 0) { //min. ein User soll eingeloggt sein 
			
			for (int i = 0; i < connections.size() - 1; i++) { //checken ob username schon vorhanden
				if (conn.getUsername().equals(connections.elementAt(i).getUsername())) {
					try {
						//connections.elementAt(i).disconnect(); //TODO: Methode um denjenigen zu Kicken noch schreiben
					} catch (Exception e) {
						e.printStackTrace();
					}
					connections.removeElement(conn); 
					//nochmal unter anderem nick einloggen plz
				}
			}
		}
		return;
	}
	
	//Logout-Methode
	public void onLogout(String username) {

		for (int i = 0; i < connections.size(); i++) { //Username suchen, Verbindung dazu trennen + löschen aus Vector
			if (username.equals(connections.elementAt(i).getUsername())) {
				try {
					//connections.elementAt(i).disconnect(); //TODO: Methode um denjenigen zu Kicken noch schreiben
				} catch (Exception e) {
					e.printStackTrace();
				}
				connections.removeElementAt(i);
				break;
			}
		}
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
		//bisher noch keine Actions definiert

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
