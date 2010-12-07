package chatsession.impl;

import lwtrt.ex.LWTRTException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chatsession.ChatServerService;
import chatsession.ex.ChatServiceException;
import chatsession.listener.ChatServerListener;
import chatsession.pdu.ChatAction;
import chatsession.pdu.ChatMessage;
import chatsession.pdu.ChatPdu;
import chatsession.pdu.ChatUserList;
import chatsession.pdu.ChatPdu.ChatOpId;

/**
 * The Class ServerSessionImpl.
 * 
 * @author Hochschule München
 * @version 1.0.0
 */
public class ChatServerServiceImpl extends BaseServiceImpl implements
		ChatServerService {
	
	private static Log log = LogFactory.getLog(ChatServerServiceImpl.class);
	ChatServerListener listener;

	@Override
	public void sendMessage(ChatMessage message) throws ChatServiceException {
		ChatPdu pdu = new ChatPdu();
		pdu.setOpId(ChatPdu.ChatOpId.sendMessage_req_PDU);
		pdu.setName(message.getUsername());
		pdu.setData(message);
		try {
			this.connection.send(pdu);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendAction(ChatAction action) throws ChatServiceException {
		ChatPdu pdu = new ChatPdu();
		pdu.setOpId(ChatPdu.ChatOpId.sendAction_req_PDU);
		pdu.setData(action);
		try {
			this.connection.send(pdu);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendUserList(ChatUserList userlist) throws ChatServiceException {
		ChatPdu pdu = new ChatPdu();
		pdu.setOpId(ChatPdu.ChatOpId.sendList_req_PDU);
		pdu.setData(userlist);
		try {
			this.connection.send(pdu);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerChatSessionListener(ChatServerListener listener)
			throws ChatServiceException {
		this.listener = listener;
		log.debug("ChatSessionListener registriert.");
		super.startThread();
	}

	public String getUserName() {
		return super.getUserName();
	}
	

	@Override
	public void destroy() throws ChatServiceException {
		try {
			this.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		log.debug("Session von " +super.userName+ " beendet");	
	}

	@Override
	protected void handleChatPdu(ChatPdu pdu) {
		//log.debug("Handle Chat-PDU: " +pdu.getOpId());
		switch (pdu.getOpId()) {
			case createSession_req_PDU:
				super.setUserName(pdu.getName()); break;
			case destroySession_req_PDU:
				listener.onLogout(pdu.getName()); break;
			case sendMessage_req_PDU:
				ChatMessage msg = (ChatMessage) pdu.getData();
				log.debug(msg.getMessage());
				if (msg == null || msg.getMessage().equals(""))
					System.out.println("Leere Nachricht, nichts passiert!"); 
				listener.onMessageEvent(msg); break;
		}	
	}
}
