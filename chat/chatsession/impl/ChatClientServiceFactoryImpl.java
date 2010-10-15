package chatsession.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chatsession.ChatClientService;
import chatsession.ChatClientServiceFactory;
import chatsession.ex.ChatServiceException;

public class ChatClientServiceFactoryImpl implements ChatClientServiceFactory {
	private static Log log = LogFactory.getLog(BaseServiceImpl.class);

	@Override
	public ChatClientService register(int port) throws ChatServiceException {
		return null;
	}

}
