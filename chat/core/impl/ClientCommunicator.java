package core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import chatsession.ChatClientService;
import chatsession.ex.ChatServiceException;
import chatsession.listener.ChatClientListener;
import chatsession.pdu.ChatAction;
import chatsession.pdu.ChatMessage;
import chatsession.pdu.ChatUserList;

public class ClientCommunicator implements ChatClientListener {
	
	//Attribute//
	private static Log log = LogFactory.getLog(ClientCommunicator.class);
	private ChatClientService chatClientService;
	private Client client;
	private String username;
	
	
	//Ctor - TODO: �berarbeiten !
	public ClientCommunicator(Client client, ChatClientService chatClientService, String username) {
		 this.client = client;
		 this.chatClientService = chatClientService;
		 this.username = username;

		try {
			chatClientService.registerChatSessionListener(this);
		} catch (ChatServiceException e) {
			// TODO Auto-generated catch block
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


	//Nachricht wird empfahngen - Weitergabe von Name und Nachricht an Client
	public void onMessageEvent(ChatMessage message) {
		client.onMessage(message.getUsername(), message.getMessage(), message.getTime());
	}
	
	
	//Logout-Methode
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
