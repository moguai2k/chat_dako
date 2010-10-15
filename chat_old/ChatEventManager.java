import java.util.*;

public class ChatEventManager
{
  private Vector<String> clients;
  private Hashtable<String, Vector<ChatEvent>> events;

  public ChatEventManager() {
    clients = new Vector<String>();
    events = new Hashtable<String, Vector<ChatEvent>>();
  }

  private void queueEventToAll(ChatEvent evt) {
    for ( int i = 0; i < clients.size(); i++ )
      queueEvent((String) clients.get(i), evt);
  }

  private void queueEvent(String name, ChatEvent evt) {
	  Vector<ChatEvent> v = (Vector<ChatEvent>) events.get(name);
    if ( v == null )
      v = new Vector<ChatEvent>();

    v.addElement(evt);
    events.put(name, v);
  }

  public void login(String name) {
    if ( clients.indexOf(name) == -1 ) {
      clients.add(name);
      queueEventToAll(new ChatEvent(
               ChatEvent.LIST_UPDATE, clients));
    }
  }

  public void tell(String name, String message) {
    queueEventToAll(new ChatEvent(
           ChatEvent.MESSAGE, name + ": " + message));
  }

  public ChatEvent poll(String str) {
    ChatEvent evt = null;

    if ( !events.containsKey(str) )
      return null;

    Vector<ChatEvent> v = (Vector<ChatEvent>) events.get(str);
    evt = (ChatEvent) v.get(0);
    v.remove(0);

    if ( v.size() > 0 )
      events.put(str, v);
    else
      events.remove(str);

    return evt;
  }

  public void logout(String name) {
    events.remove(name);

    if ( clients.indexOf(name) != -1 ) {
      clients.remove(clients.indexOf(name));
      queueEvent(name, new ChatEvent(
                    ChatEvent.LOGOUT, null));
      queueEventToAll(new ChatEvent(
                    ChatEvent.LIST_UPDATE, clients));
    }
  }

} // ChatEventManager

