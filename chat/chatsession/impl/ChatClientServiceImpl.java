package chatsession.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import chatsession.ChatClientService;
import chatsession.ex.ChatServiceException;
import chatsession.listener.ChatClientListener;
import chatsession.pdu.ChatAction;
import chatsession.pdu.ChatMessage;
import chatsession.pdu.ChatPdu;
import chatsession.pdu.ChatUserList;
import lwtrt.impl.LWTRTServiceImpl;
import lwtrt.ex.LWTRTException;

/**
 * The Class ClientSessionImpl.
 * 
 * @author Hochschule München
 * @version 1.0.0
 */
public class ChatClientServiceImpl extends BaseServiceImpl implements ChatClientService {
	//Attribute//
	private static Log log = LogFactory.getLog(ChatClientServiceImpl.class);
	protected ChatClientListener listener;
	protected LWTRTServiceImpl lwtrtService;
	
	
	//Ctor
	public ChatClientServiceImpl(LWTRTServiceImpl lwtrtService) {
		this.lwtrtService = lwtrtService;
	}
	
	
	@Override
	public void create(String serverAdress, int serverPort, String name) throws ChatServiceException {
		super.userName = name;
		try {
			super.setConnection(lwtrtService.connect(serverAdress, serverPort));
			log.debug("Connection erstellt zu: " +serverAdress+ ", Remoteport: " +serverPort);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void sendMessage(ChatMessage message) throws ChatServiceException {
		ChatPdu pdu = new ChatPdu(); //neue PDU 
		pdu.setOpId(ChatPdu.ChatOpId.sendMessage_req_PDU); //passende ID zugeordnet
		pdu.setName(message.getUsername()); //Username gesetzt
		pdu.setData(message); //Nachricht dazugepackt
		log.debug("<< Name ("+message.getUsername()+") + Nachricht ("+message.getMessage()+") weitergegeben >>"); //Daten an Server-Console gesandt
		try {
			super.connection.send(pdu); //Päckchen senden
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void sendAction(ChatAction action) throws ChatServiceException {
		ChatPdu pdu = new ChatPdu();
		pdu.setOpId(ChatPdu.ChatOpId.sendAction_req_PDU);
		pdu.setData(action);
		log.debug("<< 'Action' zum Chat versandt >>");
		try {
			connection.send(pdu);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void registerChatSessionListener(ChatClientListener listener) throws ChatServiceException {
		log.debug ("-- ChatSessionListner registriert -- ");
		this.listener = listener;
		try {
			super.startThread();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Send-Session Request
		ChatPdu pdu = new ChatPdu();
		pdu.setOpId(ChatPdu.ChatOpId.createSession_req_PDU);
		pdu.setName(super.userName);
		try {
			super.connection.send(pdu);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void destroy() throws ChatServiceException {
		ChatPdu pdu = new ChatPdu();
		pdu.setOpId(ChatPdu.ChatOpId.destroySession_req_PDU);
		pdu.setName(super.userName);
		log.debug("<< Name ("+super.userName+") loggt sich aus >>");
		try {
			connection.send(pdu);
			connection.disconnect();
		} catch (LWTRTException e) {
			e.printStackTrace();
		} catch (Throwable e) {
				e.printStackTrace();
		}
	}
	
	
	//neue Methode, da noch pdu-id-sendList_req_PDU vorhanden war ^^
	@Override
	public void sendUserList(ChatUserList userlist) throws ChatServiceException {
		ChatPdu pdu = new ChatPdu();
		pdu.setOpId(ChatPdu.ChatOpId.sendList_req_PDU);
		pdu.setData(userlist);
		log.debug("<< Userlist generiert >>");
		try {
			connection.send(pdu);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	protected void handleChatPdu(ChatPdu pdu) {
		log.debug("Behandle gerade folgende pduID: " +pdu.getOpId());
		switch(pdu.getOpId()) {
			case sendMessage_req_PDU:
				listener.onMessageEvent((ChatMessage) pdu.getData()); break;
			case sendAction_req_PDU:
				listener.onActionEvent((ChatAction) pdu.getData()); break;
			case sendList_req_PDU:
				listener.onUserListEvent((ChatUserList) pdu.getData()); break;
		}
	}

}
