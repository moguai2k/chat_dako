// Nicht verwirren lassen. Alle .awt-Klassen haben eigentlich nur mit der GUI zu tun.
// Es sind vordefinierte Klassen um Textfelder, Rahmen, etc. f�r eine GUI bereitzustellen.
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
// Implementierte Klasse f�r eine Liste. (einfache denk ich mal, so mit den Standardmethoden)
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
// .event sind dann f�r die KLICKS auf die BUttons usw. Allgemein sind das Listener (f�hren Code bei Klick, etc. aus.)
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
// Hier wird irgendwo die Vector-Klasse verwendet... Kann man zur Speicherung von Objekten nutzen.
import java.util.Vector;

// Client
public class Client
{
// Hier die Klassenvariablen f�r den Client.
  // Objekt der Klasse ClientCommunicator! Klasse ist im Sockets-Ordner.
  private ClientCommunicator communicator;
  // String f�r den Benutzernamen
  private String userName;
  // 2 Fenster f�r Login und Chat
  private Frame loginFrame, chatFrame;
  // Liste f�r die Benutzer.
  private List userList;
  // Textbereich f�r den Chat.
  private TextArea chatArea;
  // 2 Textfelder f�r das Namensfeld und das Chatfeld.
  private TextField nameField, chatField;
  // 2 einfache Kn�pfe zum abschicken und ausloggen.
  private Button submitButton, logoutButton;

// Der Login-Screen
  private void showLoginFrame() {
	// Erstellung des Frames
    loginFrame = new Frame();
	// Layout setzen. Flowlayout (Constructs a new FlowLayout object with a centered alignment and horizontal and vertical
	 							// gaps with the default size of 5 pixels.)
    loginFrame.setLayout(new FlowLayout());
	// Dem Fenster wird ein neues Label hinzugef�gt, als Parameter kann man gleich den String mitgeben (quasi: was da halt stehen soll).
    loginFrame.add(new Label("Name:"));

	// Textfeld, Gr��e 20
    nameField = new TextField(20);
	// Dem Fenster wird auch noch das Textfeld hinzugef�gt.
    loginFrame.add(nameField);

	// Ein Knopf f�r den Login
    Button loginButton = new Button("login");
	// hier wird dann ein Listener hinzugef�gt. Das Schema ist immer das Selbe.
    loginButton.addMouseListener(new LoginListener());
    loginFrame.add(loginButton);

	// Das Fenster der Gr��e anpassen und sichtbar machen.
    loginFrame.pack();
    loginFrame.setVisible(true);
  }


  private void showChatFrame() {
    Panel usersPanel = new Panel();
    usersPanel.setLayout(new BorderLayout());
    usersPanel.add(new Label("Current users:"), BorderLayout.NORTH);
    userList = new List(5, false);
    usersPanel.add(userList, BorderLayout.SOUTH);

    Panel chatPanel = new Panel();
    chatPanel.setLayout(new BorderLayout());
    chatPanel.add(new Label("Chat:"), BorderLayout.NORTH);
    chatArea = new TextArea(10, 20);
    chatPanel.add(chatArea, BorderLayout.SOUTH);

    Panel ownPanel = new Panel();
    ownPanel.setLayout(new BorderLayout());
    ownPanel.add(new Label("What I want to say:"), BorderLayout.NORTH);
    chatField = new TextField(20);
    ownPanel.add(chatField, BorderLayout.CENTER);
    Panel buttonsPanel = new Panel();
    submitButton = new Button("submit");
    submitButton.addMouseListener(new ChatListener());
    buttonsPanel.add(submitButton);
    logoutButton = new Button("logout");
    logoutButton.addMouseListener(new ChatListener());
    buttonsPanel.add(logoutButton);
    ownPanel.add(buttonsPanel, BorderLayout.SOUTH);

    chatFrame = new Frame();
    chatFrame.setLayout(new BorderLayout());
    chatFrame.add(usersPanel, BorderLayout.NORTH);
    chatFrame.add(chatPanel, BorderLayout.CENTER);
    chatFrame.add(ownPanel, BorderLayout.SOUTH);
    chatFrame.pack();
    chatFrame.setVisible(true);
  }


  private void fillUserList(Vector names) {
    userList.removeAll();
    for ( int i = 0; i < names.size(); i++ )
      if (names.get(i).equals(userName) )
        userList.add("<" + names.get(i) + ">");
      else
        userList.add(names.get(i) + " ");
  }


  public Client() {
    communicator = new ClientCommunicator();
    showLoginFrame();
  }

  public static void main(String[] args) {
   new Client();
  }

  class LoginListener extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      userName = nameField.getText();
      communicator.login(userName);
      loginFrame.dispose();
      showChatFrame();
      Poller thread = new Poller();
      thread.start();
    }
  }

  class ChatListener extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      if ( e.getSource() == submitButton ) {
        communicator.tell(userName, chatField.getText());
        chatField.setText("");
      }
      else if ( e.getSource() == logoutButton ) {
        communicator.logout(userName);
      }
    }
  }


  class Poller extends Thread
  {
    public void run() {
      boolean finished = false;
      ChatEvent evt = null;

      while (!finished)
      {
      	try {
      		Thread.sleep(100); // Verz�gern wegen CPU-Belastung
		}
      	catch (InterruptedException e)
		{ }

        evt = communicator.poll(userName);

        if ( evt != null ) {
          switch (evt.getCommand()) {
          case ChatEvent.LIST_UPDATE:
            fillUserList((Vector) evt.getArg());
            break;
          case ChatEvent.MESSAGE:
            chatArea.append((String) evt.getArg() + "\n");
            break;
          case ChatEvent.LOGOUT:
            finished = true;
            break;
          }
        }
      }
      communicator.stop();
      System.exit(0);
    } // run

  } // Poller

} // Client