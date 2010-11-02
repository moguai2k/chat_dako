package chatsession.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import chatsession.impl.Threader;
import chatsession.ChatClientService;
import chatsession.ex.ChatServiceException;
import chatsession.listener.ChatClientListener;
import chatsession.pdu.ChatAction;
import chatsession.pdu.ChatMessage;
import chatsession.pdu.ChatPdu;
import chatsession.pdu.ChatUserList;
import lwtrt.impl.LWTRTServiceImpl;
import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;

/**
 * The Class ClientSessionImpl.
 * 
 * @author Hochschule MÃ¼nchen
 * @version 1.0.0
 */
public class ChatClientServiceImpl extends BaseServiceImpl implements ChatClientService {
	private static Log log = LogFactory.getLog(ChatClientServiceImpl.class);
	protected ChatClientListener listener;
	protected LWTRTConnection lwtrtconnection;
	private Threader thread;


	//überarbeiten!
	@Override
	public void create(String rcvAdd, int port, String name) throws ChatServiceException {
		this.userName = name;
		//Test:
		LWTRTServiceImpl service = new LWTRTServiceImpl();
		try {
			service.connect(rcvAdd,port);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
		
		ChatPdu pdu = new ChatPdu();
		pdu.setOpId(ChatPdu.ChatOpId.createSession_req_PDU);
		pdu.setName(name);
		try {
			lwtrtconnection.send(pdu);
		} catch (LWTRTException er) {
			er.printStackTrace();
		}
		
		//Alt:
/*		if (currentStatus != SessionStatus.NO_SESSION) {
			throw new ChatServiceException(
					"Aufruf nicht moeglich. Falscher Status. Aktueller Status:" + currentStatus.toString());
		}*/
	}

	
	@Override
	public void sendMessage(ChatMessage message) throws ChatServiceException {
		ChatPdu pdu = new ChatPdu();
		pdu.setOpId(ChatPdu.ChatOpId.sendMessage_req_PDU);
		pdu.setName(message.getUsername());
		pdu.setData(message);
		log.debug("<< Name ("+message.getUsername()+") + Nachricht ("+message.getMessage()+") weitergegeben >>");
		try {
			lwtrtconnection.send(pdu);
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
			lwtrtconnection.send(pdu);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void registerChatSessionListener(ChatClientListener listener) throws ChatServiceException {
		this.listener = listener;
		
		if (lwtrtconnection == null) {
			throw new ChatServiceException("Kein Thread gestartet");
		}
		thread = new Threader(lwtrtconnection);
		
/*		if (thread == null)
		{
			thread = new Thread(lwtrtconnection); //k.A. wie man nen thread mit der connection aufmacht...
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}*/

	}

	
	@Override
	public void destroy() throws ChatServiceException {
		ChatPdu pdu = new ChatPdu();
		pdu.setOpId(ChatPdu.ChatOpId.destroySession_req_PDU);
		pdu.setName(userName);
		log.debug("<< Name ("+userName+") loggt sich aus >>");
		try {
			lwtrtconnection.send(pdu);
			lwtrtconnection.disconnect();
		} catch (LWTRTException e) {
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
			lwtrtconnection.send(pdu);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
	}

}
