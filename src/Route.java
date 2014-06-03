/*
 * Route.java
 *
 * Matthew Moss <mdm@cse.unsw.edu.au>
 * comp3331 s1 2014
 */

import java.io.Serializable;

public class Route implements Serializable{
  public char dest;
  public char via;
  public float cost;

  public Route(char dest, char via, float cost) {
    this.dest = dest;
    this.via = via;
    this.cost = cost;
  }

  @Override
  public String toString() {
    return "Route{" +
      "dest=" + dest +
      ", via=" + via +
      ", cost=" + cost +
      '}';
  }
}
