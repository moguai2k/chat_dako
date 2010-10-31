package chatsession;

import chatsession.ex.ChatServiceException;
import chatsession.pdu.ChatUserList;
import chatsession.listener.ChatClientListener;
import chatsession.pdu.ChatAction;
import chatsession.pdu.ChatMessage;

public interface ChatClientService extends BaseSessionService {

    /**
     * Baut eine Session mit dem angegeben Partner auf
     * 
     * @param rcvAdd IP oder Hostname des Partners
     * @param port Port des Partners
     * @param name Login-Name des Users
     */
    void create(String rcvAdd, int port, String name) throws ChatServiceException;

    /**
     * Wird aufgerufen um eine Nachricht (Message) zu versenden
     * 
     * @param ChatMessage the message
     */
    void sendMessage(ChatMessage message) throws ChatServiceException;

    /**
     * Wird aufgerufen um eine Action (ChatAction) zu versenden
     * 
     * @param ChatAction the action
     */
    void sendAction(ChatAction action) throws ChatServiceException;

    /**
     * Hier wird ein Listenerobjekt registriert, das die ankommenden Nachrichten bearbeitet
     * 
     * @param listener
     */
    void registerChatSessionListener(ChatClientListener listener) throws ChatServiceException;

	void sendUserList(ChatUserList userlist) throws ChatServiceException; //neu

}
