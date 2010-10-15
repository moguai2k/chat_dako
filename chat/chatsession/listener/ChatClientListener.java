package chatsession.listener;

import chatsession.pdu.ChatUserList;

public interface ChatClientListener extends BaseChatListener {
    /**
     * Wird aufgerufen, sobald eine neue ChatUserList empfangen wurde
     * 
     * @param userlist
     */
    public void onUserListEvent(ChatUserList userlist);
}
