package chatsession.pdu;

/**
 * Das Datenobjekt zum Versenden der Userliste
 * 
 */
public class ChatUserList extends AbstractChatServiceData {

    String[] userList;

    public ChatUserList(String[] userList) {
        this.userList = userList;
    }

    /**
     * Getter fuer userList
     * 
     * @return userList
     */
    public String[] getUserList() {
        return this.userList;
    }

    /**
     * Setter fuer userList
     * 
     * @param userList neuer Wert fuer userList
     */
    public void setUserList(String[] userList) {
        this.userList = userList;
    }

}
