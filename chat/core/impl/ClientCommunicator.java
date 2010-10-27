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
	private static Integer startPort = 51000;
	private ChatEventListener chatEventListener;
	private ChatClientService chatClientService;
	private int currentPort;

	public ClientCommunicator(ChatEventListener chatEventListener) {
		
		 try {
			 chatClientService.registerChatSessionListener(this);
			} catch (ChatServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		/*int nextPort = startPort;
		boolean connected = false;
		while (!connected) {
			try {
				chatClientService = new ChatClientServiceFactoryImpl().register(nextPort);
				chatClientService.registerChatSessionListener(this);
				registerChatEventListener(chatEventListener);
				currentPort = nextPort;
			} catch (ChatServiceException e) {
				log.error(e.getCause());
				nextPort = nextPort + 1;
				continue;
			}
			connected = true;
		}*/
	}

	public void registerChatEventListener(ChatEventListener listener) {
		this.chatEventListener = listener;
	}

	public void login(String name) {

		boolean connected = false;

		try {

			log.info("Trying to set Listenport to " + currentPort);

			chatClientService.create("127.0.0.1", 50000, name);

		} catch (ChatServiceException e) {
			log.error(e.getCause());

		}

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
		chatEventListener
				.onMessage(message.getUsername(), message.getMessage());
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
