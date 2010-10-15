/*
 * $Date$
 * $Revision$
 * $Author$
 * $HeadURL$
 * $Id$
 */

package edu.hm.training.tcp;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import edu.hm.training.bo.Message;

public class client {

/**
 * TCP-client
 * @param args
 * @throws IOException
 */
public static void main(String[] args) throws IOException {	
	System.out.println("client start");
	
	//Wohin wollen wir senden?
	InetAddress add = InetAddress.getByName("127.0.0.1");
	
	//Was wollen wir senden
	Message msg = new Message("hallo welt", 1234);
	
	//Ein Socket zum senden. Diese Überladung connected auch gleich
	//see also: Socketbeschreibung
	Socket client = new Socket(add, 4711,add,4712);
	
	//Ein Objekt serialisieren
	ObjectOutputStream out;
	
	//Den Stream vom Socket holen
	out = new ObjectOutputStream(client.getOutputStream());
	
	//Das Objekt in Stream und damit in Socket schreiben
	out.writeObject(msg);
	//Flush
	out.flush();
	
	//Socket und Stream schliessen
	client.close();
	out.close();
	
	
	System.out.println("client ende");
	}

}
