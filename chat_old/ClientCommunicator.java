import java.net.*;
import java.io.*;

public class ClientCommunicator {
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;

  public ClientCommunicator() {
    try { 
      socket = new Socket("localhost", 8205);
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());
    } 
    catch (Exception e) { e.printStackTrace(); }
  } 

  private synchronized Object communicate(Query query) {  
    Object answer = null;

    try {
      out.writeObject(query);
      answer = in.readObject();
    }
    catch (Exception e) { e.printStackTrace(); }

    return answer;
  } 

  public void login(String name) {
    communicate(new Query(Query.LOGIN, name, null)); 
  } 

  public void logout(String name) {
    communicate(new Query(Query.LOGOUT, name, null));
  } 

  public void tell(String name, String text) {
    communicate(new Query(Query.MESSAGE, name, text));
  } 

  public ChatEvent poll(String name) {
    return (ChatEvent) communicate(new Query(
                  Query.POLLING, name, null));
  } 

  public void stop() {
    try {
      socket.close();
    } 
    catch (Exception e) { e.printStackTrace(); }
  } 

} // ClientCommunicator