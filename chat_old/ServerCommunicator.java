import java.net.*;
import java.io.*;

public class ServerCommunicator extends Thread {

  private static ServerSocket serverSocket;
  private static ChatEventManager manager;

  private Socket incoming;
  private ObjectOutputStream out;
  private ObjectInputStream in;


  public static void main (String args[])
  {
    try {
      serverSocket = new ServerSocket(8205);
      System.out.println("ServerCommunicator waiting for clients...");
      
      manager = new ChatEventManager();

      while (true) {
        Socket incoming = serverSocket.accept();
        ServerCommunicator communicator = 
            new ServerCommunicator(incoming);
        communicator.start(); 
      }
    } catch ( Exception e) { e.printStackTrace(); } 
  } 

  public ServerCommunicator(Socket incoming)
    {
      this.incoming = incoming;

      try {
        out = new ObjectOutputStream(incoming.getOutputStream());
        in = new ObjectInputStream(incoming.getInputStream());
      }  
      catch (Exception e) { e.printStackTrace(); }
    } 

    public void run()
    {
      boolean finished = false;

      try {
        while (!finished) {
          Query query = (Query) in.readObject();

          switch(query.getQuery()) {
          case Query.LOGIN:
            manager.login(query.getName());
            out.writeObject(new Query(
                       Query.CONFIRM, null, null));
            break;
          case Query.MESSAGE:
            manager.tell(query.getName(), query.getMessage());
            out.writeObject(new Query(
                       Query.CONFIRM, null, null)); 
            break;
          case Query.LOGOUT:
            manager.logout(query.getName());
            out.writeObject(new Query(
                       Query.CONFIRM, null, null)); 
            break;
          case Query.POLLING:
            ChatEvent evt = manager.poll(query.getName());
            if ( evt != null && 
                 evt.getCommand() == ChatEvent.LOGOUT ) 
              finished = true;
            out.writeObject(evt);
            out.reset();
            break;
          }
        } 
      } 
      catch (Exception e) { e.printStackTrace(); }

      try {
        out.flush();
        incoming.close();
      }
      catch (Exception e) { e.printStackTrace(); }

    } // run

} // ServerCommunicator