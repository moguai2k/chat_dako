package chatsession.impl;

import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;
import lwtrt.impl.LWTRTServiceImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chatsession.ChatServerService;
import chatsession.ChatServerServiceFactory;
import chatsession.ex.ChatServiceException;

/**
 * The Enum ServerSessionFactoryImpl.
 * 
 * @author Hochschule München
 * @version 1.0.0
 */
public class ChatServerServiceFactoryImpl implements ChatServerServiceFactory {

	private static Log log = LogFactory.getLog(ChatServerServiceFactoryImpl.class);
	
	LWTRTServiceImpl lwtrtService = new LWTRTServiceImpl();

	public void register(int port) throws ChatServiceException {
		try {
			lwtrtService.register(port);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ChatServerService getSession() throws ChatServiceException {
		
		LWTRTConnection connection;
		try {
			connection = lwtrtService.accept();
		} catch (Exception e) {
			throw new ChatServiceException("Problem beim Verbindungsaufbau:" +e);
		}
		ChatServerServiceImpl chatService = new ChatServerServiceImpl();
		chatService.setConnection(connection);
		return chatService;
	}
}
