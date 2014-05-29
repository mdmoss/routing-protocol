import java.io.Serializable;
import java.util.ArrayList;

public class RoutesMsg implements Serializable {
  char id;
  ArrayList<Route> routes;

  public RoutesMsg(char id, ArrayList<Route> routes) {
    this.id = id;
    this.routes = routes;
  }

  @Override
  public String toString() {
    return "RoutesMsg{" +
      "id=" + id +
      ", routes=" + routes +
      '}';
  }
}
