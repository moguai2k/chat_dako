package chatsession;

import chatsession.ex.ChatServiceException;

public interface BaseSessionService {

    /**
     * Beendet die Session und loggt den Client aus
     */
    void destroy() throws ChatServiceException;

}
