package chatsession;

import chatsession.ex.ChatServiceException;

/**
 * A factory for creating Session objects.
 */
public interface ChatServerServiceFactory {

    /**
     * Registriert einen lokalen port f+r den Server
     * 
     * @param port
     */
    public void register(int port) throws ChatServiceException;

    /**
     * Wartet auf ankommende Sessionanfragen und gibt bei Erfolg ein Sessionobjekt zur+ck.
     * Damit ist der User eingeloggt. Der Username kann über die Methode getUserName() im ChatServerService
     * abgefragt werden.
     * 
     * @return the session
     */
    public ChatServerService getSession() throws ChatServiceException;

}
