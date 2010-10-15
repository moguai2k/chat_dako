/*
 * $Date$
 * $Revision$
 * $Author$
 * $HeadURL$
 * $Id$
 */

package edu.hm.training.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import edu.hm.training.bo.Message;

public class serv {

	/**
	 * Ein TCP-server
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Server start");

		// Socket öffnen und auf port 4711 lauschen, man könnte uach noch die IP
		// angeben

		ServerSocket serverSocket = new ServerSocket(4711);
		while (true) {

			// Verbindung akzeptieren
			Socket clientSocket = serverSocket.accept();

			// Deserialisieren
			ObjectInputStream ois;
			// Stream holen
			ois = new ObjectInputStream(clientSocket.getInputStream());
			Message msg;
			try {
				// Message aus Stream lesen
				msg = (Message) ois.readObject();
				System.out.println(msg.toString());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Socket schliessen
			clientSocket.close();
			System.out.println("Server ende");
		}
	}

	private static String zeileInput() {
		BufferedReader console = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.print("Geben Sie etwas ein: ");
		String zeile = null;
		try {
			zeile = console.readLine();
		} catch (IOException e) {
			// Sollte eigentlich nie passieren
			e.printStackTrace();
		}
		return zeile;
	}

}
