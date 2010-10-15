/*
 * $Date$
 * $Revision$
 * $Author$
 * $HeadURL$
 * $Id$
 */

package edu.hm.training.udp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import edu.hm.training.bo.Message;

/**
 * Der Sender
 * @author christoph
 *
 */
public class clientEins {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Message msg = new Message("Hallo Welt",123456);
		
		//Paket und Socket
		DatagramPacket pck;
		DatagramSocket sock;
		
		//Der Datenhalter
		byte[] data;
		
		//Neuen Bytestream erzeugen
		ByteArrayOutputStream bArrayOutStream = new ByteArrayOutputStream(100);
		try {
			
			//Serialsierung an Bytestream hängen
			ObjectOutputStream ooStream = new ObjectOutputStream(bArrayOutStream);
			//Daten schreiben
			ooStream.writeObject(msg);		
			ooStream.flush();
			
			//Bytearray aus Strom holen
			data = bArrayOutStream.toByteArray();
			
			//Empfängeradresse angeben
			InetAddress inetAddr = InetAddress.getByName("127.0.0.1");
			//Socket erzeugen
			sock = new DatagramSocket();
			
			//Paket zum versenden erzeugen Es wird an port 4712 versendet
			pck = new DatagramPacket(data, data.length,inetAddr, 4712);
			
			//Paket versenden
			sock.send(pck);
			
			//Socket und Stream schliessen
			ooStream.close();
			sock.close();


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

}
