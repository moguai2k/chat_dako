package chatsession.impl;


import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chatsession.BaseSessionService;
import chatsession.ex.ChatServiceException;
import chatsession.pdu.ChatPdu;

public abstract class BaseServiceImpl implements BaseSessionService {
	private static Log log = LogFactory.getLog(BaseServiceImpl.class);
	
	protected Threading thread;
	protected LWTRTConnection connection;
	protected SessionStatus currentStatus = SessionStatus.NO_SESSION;
	protected String userName;
	protected ChatPdu chatPdu;

	public enum SessionStatus {
		NO_SESSION(1), SESSION_ACTIVE(2), SESSION_DESTROYED(3);
		int code;

		SessionStatus(int code) {
			this.code = code;
		}
	}

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
	
	protected abstract void handleChatPdu(ChatPdu pdu);
	
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
	
	
	public class Threading extends Thread {
		LWTRTConnection connection;
		BaseServiceImpl baseService;
		
		public Threading (LWTRTConnection connection, BaseServiceImpl baseService) {
			log.debug("--BaseService RecvThread gestartet--");
			this.connection = connection;
			this.baseService = baseService;
		}
		
		@Override
	    public void run() {
			try {
				//synchronized (connection.receive()) {
					while (!isInterrupted()) {
						Object pdu = connection.receive();
		                if (pdu instanceof ChatPdu)
		                	baseService.handleChatPdu((ChatPdu) pdu);
		            }
				//}	
	        } catch (LWTRTException e) {
	            log.error(e);
	        }
		}
	}
	
}