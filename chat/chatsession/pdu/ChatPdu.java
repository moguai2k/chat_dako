package chatsession.pdu;

import java.io.Serializable;

/**
 * Die PDU der Sessionschicht.
 * 
 */
public class ChatPdu implements Serializable {

    private ChatOpId opId;
    private String name;
    private AbstractChatServiceData data;

    public enum ChatOpId {
        createSession_req_PDU(1),
        destroySession_req_PDU(2),
        sendMessage_req_PDU(3),
        sendList_req_PDU(4),
        sendAction_req_PDU(5);
        int code;

        ChatOpId(int code) {
            this.code = code;
        }
    }

    /**
     * Getter fuer name
     * 
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter fuer name
     * 
     * @param name neuer Wert fuer name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter fuer data
     * 
     * @return data
     */
    public AbstractChatServiceData getData() {
        return this.data;
    }

    /**
     * Setter fuer data
     * 
     * @param data neuer Wert fuer data
     */
    public void setData(AbstractChatServiceData data) {
        this.data = data;
    }

    public ChatOpId getOpId() {
        return opId;
    }

    public void setOpId(ChatOpId opId) {
        this.opId = opId;
    }

}
