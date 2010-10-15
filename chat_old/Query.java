import java.io.Serializable;

public class Query implements Serializable
{
  private static final long serialVersionUID = 1L;
  public final static int UNDEFINED = 0;
  public final static int LOGIN = 1;
  public final static int MESSAGE = 2;
  public final static int LOGOUT = 3;
  public final static int POLLING = 4;
  public final static int CONFIRM = 5;

  private int query;
  private String name;
  private String message;

  public Query() {
    this.query = UNDEFINED;
    this.name = null;
    this.message = null;
  }

  public Query(int query, String name, String message) {
    this.query = query;
    this.name = name;
    this.message = message;
  }

  public int getQuery() {
    return query;
  }

  public String getName() {
    return name;
  }

  public String getMessage() {
    return message;
  }

} // Query
