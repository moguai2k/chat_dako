package chatsession;

import chatsession.ex.ChatServiceException;
import chatsession.pdu.ChatUserList;

public interface BaseSessionService {

    /**
     * Beendet die Session und loggt den Client aus
     * @param name 
     */
    void destroy(String name) throws ChatServiceException; //String name

}
