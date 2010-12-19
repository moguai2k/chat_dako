package chatsession.impl;
/*
 * Client und Server erben von dieser Klasse. Der Thread zur Behandlung der ChatPdu´s ist hier implementiert.
 */
import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chatsession.BaseSessionService;
import chatsession.ex.ChatServiceException;
import chatsession.pdu.ChatPdu;

public abstract class BaseServiceImpl implements BaseSessionService {
	
	//Attribute//
	private static Log log = LogFactory.getLog(BaseServiceImpl.class);
	protected Threading thread;
	protected LWTRTConnection connection;
	protected SessionStatus currentStatus = SessionStatus.NO_SESSION;
	protected String userName;

	
	//Session-Enums
	public enum SessionStatus {
		NO_SESSION(1), SESSION_ACTIVE(2), SESSION_DESTROYED(3);
		int code;

		SessionStatus(int code) {
			this.code = code;
		}
	}

	// Wie werden die ChatPdu´s bearbeitet? Unterschiedliche Implementation Server/Client.
	protected abstract void handleChatPdu(ChatPdu pdu);
	
	
	//Thread starten
	public void startThread() throws ChatServiceException {
		if (connection == null) {
			throw new ChatServiceException(
					"Connection ist null ");
		}
		if (thread == null) {
			thread = new Threading(connection, this);
			thread.start();
			this.currentStatus = SessionStatus.SESSION_ACTIVE;
		}
	}
	
	
	//Thread stoppen
	@SuppressWarnings("deprecation")
	public void stopThread() {
		try {
			connection.disconnect();
			this.currentStatus = SessionStatus.SESSION_DESTROYED;
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
		if ((thread !=null) && thread.isAlive())
		{
			thread.stop();
			thread = null;
		}
	}
	
	
	// Thread auf Chatsession-Schicht. Ruft ständig in der Connection receive auf und behandelt es,
	// wenn es sich um ein Objekt der Klasse ChatPdu handelt. Kleiner Sleep nach jedem Durchlauf (performance).
	public class Threading extends Thread {
		LWTRTConnection connection;
		BaseServiceImpl baseService;
		
		public Threading (LWTRTConnection connection, BaseServiceImpl baseService) {
			log.debug("--BaseService RecvThread für connection-Hash: "
					+connection.hashCode()+ " gestartet--");
			this.connection = connection;
			this.baseService = baseService;
		}
		
		@Override
	    public void run() {
			try {
				while (!isInterrupted()) {
					Object pdu = connection.receive();
	                if (pdu instanceof ChatPdu)
	                	baseService.handleChatPdu((ChatPdu) pdu);
	                try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }		
	        } catch (LWTRTException e) {
	            log.error(e);
	        }
		}
	}
	
	
	//Getter & Setter
	public String getUserName() {
		return userName;
	}

	public void setUserName(String username) {
		this.userName = username;
	}

	public LWTRTConnection getConnection() {
		return connection;
	}

	public void setConnection(LWTRTConnection connection) {
		this.connection = connection;
	}
	
}