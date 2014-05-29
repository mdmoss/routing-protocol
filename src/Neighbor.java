public class Neighbor {
  public char id;
  public float cost;
  public int port;

  public Neighbor(char id, float cost, int port) {
    this.id = id;
    this.cost = cost;
    this.port = port;
  }

  @Override
  public String toString() {
    return "Neighbor{" +
      "id=" + id +
      ", cost=" + cost +
      ", port=" + port +
      '}';
  }
}
