package chatsession;

import chatsession.ex.ChatServiceException;

/**
 * A factory for creating Session objects.
 */
public interface ChatClientServiceFactory {

    /**
     * Registriert einen ChatClientService an einem lokalen port und gibt das ChatClientService-Objekt zur+ck
     * 
     * @param port der lokale Port f+r diesen Client 
     * @return
     * @throws ChatServiceException
     */
    public ChatClientService register(int port) throws ChatServiceException;

}
