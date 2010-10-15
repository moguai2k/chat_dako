package chatsession.ex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChatServiceException extends Exception {
     private static Log log = LogFactory.getLog(ChatServiceException.class);
    private static final long serialVersionUID = -1L;

    public ChatServiceException(String msg) {
        super(msg);
        log(msg, this);
    }

    public ChatServiceException(Throwable e) {
        super(e);
        log("ChatServiceException", this);
    }

    public ChatServiceException(String msg, Throwable e) {
        super(msg, e);
        log(msg, this);
    }

    private void log(String msg, Throwable e) {
        log.error(msg, e);
    }
}
