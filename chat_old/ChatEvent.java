import java.io.Serializable;

public class ChatEvent implements Serializable
{
  private static final long serialVersionUID = 1L;
  public final static int UNDEFINED = 0;
  public final static int LIST_UPDATE = 1;
  public final static int MESSAGE = 2;
  public final static int LOGOUT = 3;

  private int cmd;
  private Object arg;

  public ChatEvent() {
    this.cmd = UNDEFINED;
    this.arg = null;   
  }

  public ChatEvent(int cmd, Object arg) {
    this.cmd = cmd;
    this.arg = arg;
  }

  public int getCommand() {
    return cmd;
  }

  public Object getArg() {
    return arg;
  }

} // ChatEvent
