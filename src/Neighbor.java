/*
 * Neighbour.java
 *
 * Matthew Moss <mdm@cse.unsw.edu.au>
 * comp3331 s1 2014
 */

public class Neighbor {
  public char id;
  public float cost;
  public int port;
  public float updatedCost;

  public Neighbor(char id, float cost, int port, float updatedCost) {
    this.id = id;
    this.cost = cost;
    this.port = port;
    this.updatedCost = updatedCost;
  }

  @Override
  public String toString() {
    return "Neighbor{" +
      "id=" + id +
      ", cost=" + cost +
      ", port=" + port +
      ", updatedCost=" + updatedCost +
      '}';
  }
}
