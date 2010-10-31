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
	private static Log log = LogFactory.getLog(ClientCommunicator.class);
	private ChatClientService chatClientService;
	private Client client;
	private String username;
	
	//TODO: Status: zu überarbeiten !
	public ClientCommunicator(Client client, ChatClientService chatClientService, String username) {
		 this.client = client;
		 this.chatClientService = chatClientService;
		 this.username = username;

		 try {
			 chatClientService.registerChatSessionListener(this);
			} catch (ChatServiceException e) {
				e.printStackTrace();
			}
	}

//	public void registerChatEventListener(ChatEventListener listener) {
//		this.chatEventListener = listener;
//	}
	

	@Override //Status vorerst Fertig !
	public void onActionEvent(ChatAction action) {
		client.onAction(action.getOpId(), action.getReserved());

	}

	@Override //Status Fertig ! ( müsste funktionieren )
	public void onUserListEvent(ChatUserList userlist) {
		client.onUserListUpdate(userlist.getUserList());
	}

	@Override //Status erstmal Fertig !
	public void onMessageEvent(ChatMessage message) {
		client.onMessage(message.getUsername(), message.getMessage());
	}
	
	// Status: zu überarbeiten - destroy festlegen !
	public void logout(String name) {
		try {
			chatClientService.destroy();
		} catch (ChatServiceException e) {
			log.error(e);
		}
	}
	
	// Status: müsste theoretisch funktionieren
	public void tell(String name, String text) {
		ChatMessage msg = new ChatMessage(name, text);
		try {
			chatClientService.sendMessage(msg);
		} catch (ChatServiceException e) {
			log.error(e);
		}
	}

} // ClientCommunicator
