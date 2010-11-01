package chatsession.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import chatsession.ChatClientService;
import chatsession.ChatClientServiceFactory;
import chatsession.ex.ChatServiceException;
import lwtrt.ex.LWTRTException;
import lwtrt.impl.LWTRTServiceImpl;


public class ChatClientServiceFactoryImpl implements ChatClientServiceFactory {
	private static Log log = LogFactory.getLog(BaseServiceImpl.class);
	private LWTRTServiceImpl service = new LWTRTServiceImpl();

	@Override
	public ChatClientService register(int port) throws ChatServiceException {
        try {
    		service.register(port);
		} catch (LWTRTException e) {
			e.printStackTrace();
		}
		return null;
	}
	

}

