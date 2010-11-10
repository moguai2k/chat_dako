package chatsession.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import chatsession.ChatClientService;
import chatsession.ChatClientServiceFactory;
import chatsession.ex.ChatServiceException;
import lwtrt.ex.LWTRTException;
import lwtrt.impl.LWTRTServiceImpl;


public class ChatClientServiceFactoryImpl implements ChatClientServiceFactory {
	
	private static Log log = LogFactory.getLog(BaseServiceImpl.class);
	protected LWTRTServiceImpl lwtrtService = new LWTRTServiceImpl();
	
	@Override
	public ChatClientService register(int port) throws ChatServiceException {
		try {
    		lwtrtService.register(port);
		} catch (LWTRTException e) {
			log.debug("Fehler beim registrieren des Ports im LWTRTService: " +e);
		}
		return new ChatClientServiceImpl(lwtrtService);
	}
	

}

