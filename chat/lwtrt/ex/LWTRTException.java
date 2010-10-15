package lwtrt.ex;

import org.apache.log4j.Logger;

/**
 * The Class LWTRTException.
 * 
 * @author Hochschule München
 * @version 1.0.0
 */
public class LWTRTException extends Exception {

    /** The logger. */
    private static Logger logger = Logger.getLogger(LWTRTException.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -46473630383065913L;

    /**
     * Instantiates a new lWTRT exception.
     * 
     * @param msg the msg
     */
    public LWTRTException(String msg) {
        super(msg);
        log(msg, this);
    }

    /**
     * Instantiates a new lWTRT exception.
     * 
     * @param msg the msg
     * @param e the e
     */
    public LWTRTException(Throwable e) {
        super(e);
        log("", this);
    }

    /**
     * Log.
     * 
     * @param msg the msg
     * @param e the e
     */
    private void log(String msg, Throwable e) {
        logger.error(msg, e);
    }

}
