package chatsession.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chatsession.ChatClientService;
import chatsession.ex.ChatServiceException;
import chatsession.listener.ChatClientListener;
import chatsession.pdu.ChatAction;
import chatsession.pdu.ChatMessage;
import chatsession.pdu.ChatPdu;
import lwtrt.LWTRTConnection;
import lwtrt.ex.LWTRTException;
import lwtrt.impl.LWTRTServiceImpl;

/**
 * The Class ClientSessionImpl.
 * 
 * @author Hochschule München
 * @version 1.0.0
 */
public class ChatClientServiceImpl extends BaseServiceImpl implements ChatClientService {
	private static Log log = LogFactory.getLog(ChatClientServiceImpl.class);
	protected ChatClientListener listener;
	protected LWTRTConnection lwtrtconnection;

	@Override
	public void create(String rcvAdd, int port, String name) throws ChatServiceException {
		
		if (currentStatus != SessionStatus.NO_SESSION) {
			throw new ChatServiceException(
					"Aufruf nicht m+glich. Falscher Status. Aktueller Status:"
							+ currentStatus.toString());
		}

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

	}

	@Override
	public void registerChatSessionListener(ChatClientListener listener) {
		this.listener = listener;

	}

	@Override
	public void destroy() throws ChatServiceException {

	}

}
