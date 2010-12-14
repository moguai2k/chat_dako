package core.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.log4j.PropertyConfigurator;

import chatsession.ChatServerService;
//import chatsession.ChatServerServiceFactory;
import chatsession.ex.ChatServiceException;
//import chatsession.impl.ChatServerServiceFactoryImpl;
import chatsession.listener.ChatServerListener;
import chatsession.pdu.ChatAction;
import chatsession.pdu.ChatMessage;
import chatsession.pdu.ChatUserList;

public class ServerCommunicator extends Thread implements ChatServerListener {
	
	private static Log log = LogFactory.getLog(ServerCommunicator.class);
	private static ConcurrentHashMap<String, ChatServerService> sessions = new ConcurrentHashMap<String, ChatServerService>();
	private ChatServerService chatServerService;


	public ServerCommunicator(ChatServerService chatServerService) {
		this.chatServerService = chatServerService;
		try {
			chatServerService.registerChatSessionListener(this);		
		} catch (ChatServiceException e) {
			log.error(e);
		}
		while (true) {
			if (chatServerService.getUserName() != null) {
				onLogin(chatServerService.getUserName());
				break;
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendToEveryone(ChatMessage message) {
		Enumeration<String> keys = sessions.keys();
		for (Entry<String, ChatServerService> entry : sessions.entrySet()) {
			String user = entry.getKey();
			try {
				entry.getValue().sendMessage(message);
			} catch (ChatServiceException e) {
				// Session ist nicht mehr valide --> ausloggen
				log.error("Error sending to " + user + ", logging out " + user);
				onLogout(user);
			}
		}
	}

	public void sendUserlistUpdate() {
		Enumeration<String> keys = sessions.keys();
		ArrayList<String> list = new ArrayList<String>();
		while (keys.hasMoreElements()) {
			list.add(keys.nextElement());
		}
		String[] array = new String[list.size()];
		list.toArray(array);
		ChatUserList userList = new ChatUserList(array);

		for (Entry<String, ChatServerService> entry : sessions.entrySet()) {
			try {
				entry.getValue().sendUserList(userList);
			} catch (ChatServiceException e) {
				// Session ist nicht mehr valide --> ausloggen
				log.error("Error sending to " + entry.getKey()
						+ ", logging out " + entry.getKey());
				onLogout(entry.getKey());
			}
		}
	}

	@Override
	public void onActionEvent(ChatAction action) {
	}

	public void onLogin(String username) {
		if (!sessions.containsKey(username)) {
			sessions.put(username, chatServerService);
			sendUserlistUpdate();
			ChatMessage msg = new ChatMessage();
			msg.setUsername("SERVER");
			msg.setTime(getTime());
			msg.setMessage(username.toUpperCase()+" IST DEM CHAT BEIGETRETEN");
			sendToEveryone(msg);
		} else {
			ChatAction action = new ChatAction(
					ChatAction.CHATACTION_USERNAME_SCHON_VERGEBEN);
			try {
				chatServerService.sendAction(action);
			} catch (ChatServiceException e) {
				log.error(e);
			}
		}
	}

	@Override
	public void onLogout(String username) {
		log.trace("Logging out " + username);
		if (sessions.containsKey(username)) {
			sessions.remove(username);
			ChatMessage msg = new ChatMessage();
			msg.setUsername("SERVER");
			msg.setTime(getTime());
			msg.setMessage(username.toUpperCase()+" VERLÄSST DEN CHAT");
			sendToEveryone(msg);
			sendUserlistUpdate();
		}
	}

	@Override
	public void onMessageEvent(ChatMessage message) {
		if (message.getMessage() != null) {
			message.setTime(getTime());
			sendToEveryone(message);
		}
	}
	
	// Gibt die Zeit zurück HH:mm:ss
	private static String getTime() {
		Date now = Calendar.getInstance().getTime();
		SimpleDateFormat format = new SimpleDateFormat ("HH:mm");
		return format.format(now);
	}

} // ServerCommunicator
