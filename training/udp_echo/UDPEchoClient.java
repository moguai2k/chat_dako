// ============================================================================
// Project:      Datenkommunikation
// Class:        UDPEchoClient
// Version:      1.2
// Copyright:    ---
// Company:      ---
// Description:	 Client for UDP-Test
//				 The program sends a message to a server which sends an echo 
//				 back.
// ----------------------------------------------------------------------------
// History:
// Date      Author      Remark
// ----------------------------------------------------------------------------
// 16.08.02	 Mandl		 created
// ============================================================================
package udp_echo;

import java.net.*;
import java.io.*;

public class UDPEchoClient{
	
  protected DatagramSocket socket; // Socket for UDP communication
  protected DatagramPacket packet; 

 /** 
   * Constructor
   */	
  public UDPEchoClient (String message, String host, int port) throws IOException 
  {
  	/*
  	 * Create a new socket
  	 */
    socket = new DatagramSocket ();
    /* 
     * Build a message
     */
    packet = buildPacket (message, host, port);
    /*
     * Send message and receive the echo
     */
    try {
      sendPacket ();
      receivePacket ();
    } 
    finally {
      socket.close ();
    }
  }

 /**
  * Create a datagram
  */
  protected DatagramPacket buildPacket (String message, String host, int port) 
  		throws IOException 
  {
  	/*
  	 * Serialize the message
  	 */
    ByteArrayOutputStream byteO = new ByteArrayOutputStream ();
    DataOutputStream dataO = new DataOutputStream (byteO);
    dataO.writeUTF (message);
    byte[] data = byteO.toByteArray ();
  	/*
  	 * and create the datagram
  	 */
    return new DatagramPacket (data, data.length, InetAddress.getByName (host), port);
  }
  
 /**
   * Send a datagram
   */
  protected void sendPacket () throws IOException 
  {
    socket.send (packet);
    System.out.println ("Message sent to server");
  }

 /**
   * Receive a datagram
   */
  protected void receivePacket () throws IOException 
  {	
    byte buffer[] = new byte[65535]; // Buffer for received messages
    
    /*
     * Create a datagram
     */
    DatagramPacket packet = new DatagramPacket (buffer, buffer.length);
    
    /*
     * Receive a datagram
     */
    socket.receive (packet);
    
    /*
     * Transform it into a string object
     */
    ByteArrayInputStream byteI = new ByteArrayInputStream (packet.getData (), 0, packet.getLength ());
    DataInputStream dataI = new DataInputStream (byteI);
    String result = dataI.readUTF ();
   
    System.out.println ("Message received:" + result);
  }

 /**
  * Main method
  */ 
  public static void main (String args[]) throws IOException 
  {  
    if (args.length != 3) {
      throw new RuntimeException ("Syntax: EchoClient <host> <port> <message>");
    }
    
    while (true) {
      new UDPEchoClient (args[2], args[0], Integer.parseInt (args[1]));
      System.out.println ("Wait a bit for the echo...");
      try {
        Thread.sleep (2000);
      } catch (InterruptedException ex) {
        System.out.println("Exception in sleep");
      }
    }
  } 
}
