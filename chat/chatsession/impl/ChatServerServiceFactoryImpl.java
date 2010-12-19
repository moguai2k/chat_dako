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
 * Service auf LWTRTSchicht wird erstellt. Zwei Methoden sind verfügbar. register registriert 
 * einen listenPort. getSession wartet dann auf Verbindungsanfragen. Wenn eine Verbindung aufgebaut wurde,
 * wird ein neues ChatServiceImpl-Objekt erstellt, neu erstellt Verbindung gesetzt und Objekt zurückgegeben.
 */
public class ChatServerServiceFactoryImpl implements ChatServerServiceFactory {

	private static Log log = LogFactory.getLog(ChatServerServiceFactoryImpl.class);
	
	LWTRTServiceImpl lwtrtService = new LWTRTServiceImpl();
	ChatServerServiceImpl chatService;

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
		// Synchronisiert mit dem Service! Eine Verbindung nach der anderen!
		synchronized (lwtrtService) {
			try {
				connection = lwtrtService.accept();
			} catch (Exception e) {
				throw new ChatServiceException("Problem beim Verbindungsaufbau:" +e);
			}
			chatService = new ChatServerServiceImpl();
			chatService.setConnection(connection);
			return chatService;
		}		
	}
}
