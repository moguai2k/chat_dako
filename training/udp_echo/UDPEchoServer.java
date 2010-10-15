// ============================================================================
// Project:      Datenkommunikation
// Class:        UDPEchoServer
// Version:      1.2
// Copyright:    ---
// Company:      ---
// Description:	 Server for UDP-Test
//				 The servers receives Messages from clients and sends an echo 
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

public class UDPEchoServer {

  protected DatagramSocket socket; // Socket for communiation via UDP
 
 /** 
   * Constructor
   */
  public UDPEchoServer (int port) throws IOException 
  {
    socket = new DatagramSocket (port);
  }

 /** 
   * Method to handle an incoming messages
   */  
  public void execute () throws IOException 
  {
  	/* 
  	 * Listen to messages forever
  	 */
    while (true) {
      /* 
       * Receive a packet from a client
       */
      DatagramPacket packet = receive ();
      /*
       * Send echo back to client
       */
      sendEcho (packet.getAddress (), packet.getPort (), packet.getData (), packet.getLength ());
    } 
  }
  
 /** 
   * Method to receive a message
   */  
  protected DatagramPacket receive () throws IOException 
  {
    byte buffer[] = new byte[65535];
    DatagramPacket packet = new DatagramPacket (buffer, buffer.length);
    socket.receive (packet);
    System.out.println ("Message received with " + packet.getLength () + " bytes.");
    return packet;
  }

 /** 
   * Method to send an echo message
   */  
  protected void sendEcho (InetAddress address, int port, byte data[], int length) throws IOException 
  {
    DatagramPacket packet = new DatagramPacket (data, length, address, port);
    socket.send (packet);
    System.out.println ("Echo sent back to client");
  }

 /** 
   *  Main method
   */  
  public static void main (String args[]) throws IOException 
  {
    if (args.length != 1) {
      throw new RuntimeException ("Syntax: UDPEchoServer <port>");
    }    
    /* 
     * Create an instance of an UDPEchoServer
     */  
    UDPEchoServer echo = new UDPEchoServer (Integer.parseInt (args[0]));
    System.out.println("UDPEchoServer startet...");
    /*
     * Execute the server
     */
    echo.execute ();
  } 
}
