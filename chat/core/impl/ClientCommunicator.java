package core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import core.ChatEventListener;
import chatsession.ChatClientService;
import chatsession.ex.ChatServiceException;
import chatsession.impl.ChatClientServiceFactoryImpl;
import chatsession.listener.ChatClientListener;
import chatsession.pdu.ChatAction;
import chatsession.pdu.ChatMessage;
import chatsession.pdu.ChatUserList;

public class ClientCommunicator implements ChatClientListener {
	private static Log log = LogFactory.getLog(ClientCommunicator.class);
	private ChatEventListener chatEventListener;
	private ChatClientService chatClientService;
	private ChatClientServiceFactoryImpl chatclientservicefactoryimpl;
	private Client client;
	private String username;

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

	public void registerChatEventListener(ChatEventListener listener) {
		this.chatEventListener = listener;
	}
	

	@Override
	public void onActionEvent(ChatAction action) {
		chatEventListener.onAction(action.getOpId(), action.getReserved());

	}

	@Override
	public void onUserListEvent(ChatUserList userlist) {
		chatEventListener.onUserListUpdate(userlist.getUserList());
	}

	@Override
	public void onMessageEvent(ChatMessage message) {
		chatEventListener.onMessage(message.getUsername(), message.getMessage());
	}

	public void logout(String name) {
		try {
			chatClientService.destroy();
		} catch (ChatServiceException e) {
			log.error(e);
		}
	}

	public void tell(String name, String text) {
		ChatMessage msg = new ChatMessage(name, text);
		try {
			chatClientService.sendMessage(msg);
		} catch (ChatServiceException e) {
			log.error(e);
		}
	}

} // ClientCommunicator
