package chattest_by_chris;

import java.net.*;
import java.io.*;

//Da Objekt wieder als eigenständiger Thread laufen soll -> extends
class connection extends Thread
{
	private Socket client; //Chat-Server Socket
	private ServerCommunicator server; //Zugriff auf Chat-Server-Obj
	public PrintStream out; //Ausgangsstream, anstelle ObjectOutputStream, da PrintStream Strings ausgeben kann
	private ObjectInputStream in; //Eingangsstream

	
	//Ctor
	public connection(ServerCommunicator server, Socket client)//Chatserver-Objekt + Socket
	{
		this.server = server;
		this.client = client;

		try
		{	//Versuch Eingangs/Ausgangsstream zu bekommen
			in = new ObjectInputStream(client.getInputStream());
			out = new PrintStream(client.getOutputStream());
		} catch (IOException e)
		{
			try { client.close(); } catch (IOException e2) {} ;
			System.err.println("Fehler beim Erzeugen der Streams: " + e);
			return;
		}

		this.start();
	}


	//Run-Methode empängt ständig alle Nachrichten und schickt sie an alle Verbindungen heraus
	public void run()
	{
		String text;

		try
		{
			while(true)
			{
				text = in.readLine();
				if(text != null)
					server.sendToEveryone(text);
			}
		} catch (IOException e)
		{
			System.out.println("Fehler:" + e);
		}
	}
}

