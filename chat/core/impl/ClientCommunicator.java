package core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import chatsession.ChatClientService;
import chatsession.ex.ChatServiceException;
import chatsession.impl.ChatClientServiceFactoryImpl;
import chatsession.listener.ChatClientListener;
import chatsession.pdu.ChatAction;
import chatsession.pdu.ChatMessage;
import chatsession.pdu.ChatUserList;

public class ClientCommunicator implements ChatClientListener {
	
	//Attribute//
	private static Log log = LogFactory.getLog(ClientCommunicator.class);
	private ChatClientService chatClientService;
	private Client client;
	
	
	//Ctor
	public ClientCommunicator(Client client, String remoteAdress, int remotePort, String userName) throws ChatServiceException {
		ChatClientServiceFactoryImpl clServFac = new ChatClientServiceFactoryImpl();	
		// Bei jedem Clienten wird erstmal als listenport die 50000 versucht, wenn der Port dann schon belegt ist,
		// regelt die register Methode auf LWTRT-Schicht, ohne dass der User etwas damit zu tun hat.
		chatClientService = clServFac.register(50000);
		// Verbindung zu einem Partner wird aufgebaut.
		chatClientService.create(remoteAdress, remotePort, userName);
		this.client = client;
		try {
			chatClientService.registerChatSessionListener(this);
		} catch (ChatServiceException e) {
			e.printStackTrace();
		}		
	}

	
	//Test via ActionID auf Name-schon-vorhanden
	public void onActionEvent(ChatAction action) {
		client.onAction(action.getOpId(), action.getReserved());
	}


	//Userlistupdate
	public void onUserListEvent(ChatUserList userlist) {
		client.onUserListUpdate(userlist.getUserList());
	}


	//Nachricht wird empfahngen - Weitergabe von Name, Zeit und Nachricht an Client
	public void onMessageEvent(ChatMessage message) {
		client.onMessage(message.getUsername(), message.getMessage(), message.getTime());
	}
	
	
	//Logout-Methode, wenn jemand den "Chat schließen" (aka "sich ausloggen") möchte
	public void logout() {
		try {
			chatClientService.destroy();
		} catch (ChatServiceException e) {
			log.error(e);
		}
	}
	
	
	//Nachricht geschrieben - Weitergabe von Nachricht und Name an Server
	public void tell(String name, String text) {
		ChatMessage msg = new ChatMessage(name, text);
		try {
			chatClientService.sendMessage(msg);
		} catch (ChatServiceException e) {
			log.error(e);
		}
	}

} // ClientCommunicator
