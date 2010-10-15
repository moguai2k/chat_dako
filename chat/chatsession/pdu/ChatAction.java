package chatsession.pdu;

/**
 * Das Nutzdatenobjekt, dass Nachrichten +bertr+gt
 * 
 */
public class ChatAction extends AbstractChatServiceData {
    public final static int CHATACTION_USERNAME_SCHON_VERGEBEN = 1;
    /**
     * Die Nachricht
     */
    private int opId;

    private String reserved;

    public ChatAction() {

    }

    public ChatAction(int opId) {
        this.opId = opId;
    }

    public int getOpId() {
        return opId;
    }

    public void setOpId(int opId) {
        this.opId = opId;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

}
