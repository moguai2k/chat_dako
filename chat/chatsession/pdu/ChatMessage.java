package chatsession.pdu;

/**
 * Das Nutzdatenobjekt, dass Nachrichten +bertr+gt
 * 
 */
public class ChatMessage extends AbstractChatServiceData {
    /**
     * Die Nachricht
     */
    private String message;

    /**
     * Der Benutzername des Senders
     */
    private String username;
    
    /**
     * Serverzeit
     */
    private String time;

    public ChatMessage() {

    }

    public ChatMessage(String username, String message) {
        this.message = message;
        this.username = username;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
