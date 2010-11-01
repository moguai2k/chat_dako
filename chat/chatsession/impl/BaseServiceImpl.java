package chatsession.impl;

import lwtrt.LWTRTConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chatsession.BaseSessionService;

public abstract class BaseServiceImpl implements BaseSessionService {
	private static Log log = LogFactory.getLog(BaseServiceImpl.class);
	protected LWTRTConnection connection;
	protected SessionStatus currentStatus = SessionStatus.NO_SESSION;
	
	public enum SessionStatus {
		NO_SESSION(1), SESSION_ACTIVE(2), SESSION_DESTROYED(3);
		int code;

		SessionStatus(int code) {
			this.code = code;
		}
	}

	public LWTRTConnection getConnection() {
		return connection;
	}

	public void setConnection(LWTRTConnection connection) {
		this.connection = connection;
	}

}
