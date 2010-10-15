/*
 * $Date$
 * $Revision$
 * $Author$
 * $HeadURL$
 * $Id$
 */

package edu.hm.training.udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import edu.hm.training.bo.Message;

public class clientZwei {

	/**
	 * Der Empfänger
	 * @param args
	 */
	public static void main(String[] args) {
		
		//Hier sollen die Daten rein
		byte[] buf = new byte[200];
		InetAddress inetAddr;
		try {
			//Binden des Socket an eine Adresse. Also auf welcher Adresse soll gelauscht werden?
			inetAddr = InetAddress.getByName("127.0.0.1");
			
			//Socket erzeugen und auf port 4712 lauschen
			DatagramSocket sock = new DatagramSocket(4712, inetAddr);
			
			//Paket erzeugen in welcher die Daten landen
			DatagramPacket pck = new DatagramPacket(buf, 200);
			
			while(true){
				//Paket abholen
				sock.receive(pck);
				break;
			}
			
			//Bytes aus Paket lesen
			ByteArrayInputStream bis = new ByteArrayInputStream(pck.getData());
			
			//Deserialsierung
			ObjectInputStream ois = new ObjectInputStream(bis);
			//Hier wird die Nachricht deserialisiert
			Message msg = (Message)ois.readObject();
			//Nachricht an Console schicken
			 System.out.println(msg.toString());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();		
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
