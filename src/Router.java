import java.util.Map;

public class Router {
  char id;
  int port;
  Map<Character, Neighbor> neighbors;
  boolean poisonedReverse;

  public Router(char id, int port, Map<Character, Neighbor> neighbors, boolean poisonedReverse) {
    this.id = id;
    this.port = port;
    this.neighbors = neighbors;
    this.poisonedReverse = poisonedReverse;
  }

  @Override
  public String toString() {
    return "Router{" +
      "id=" + id +
      ", port=" + port +
      ", neighbors=" + neighbors +
      ", poisenedReverse=" + poisonedReverse +
      '}';
  }

  public void run() {
    System.out.println(this.toString());
  }
}
