import java.io.Serializable;
import java.util.HashMap;

public class DistanceVector implements Serializable {
  /* I am node `id` */
  char id;
  /* My cost to node `k` is `v` */
  HashMap<Character, Float> distances;

  public DistanceVector(char id, HashMap<Character, Float> distances) {
    this.id = id;
    this.distances = distances;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DistanceVector that = (DistanceVector) o;

    if (id != that.id) return false;
    for (Character k : that.distances.keySet()) {
      if (!this.distances.containsKey(k) || !this.distances.get(k).equals(that.distances.get(k))) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "DistanceVector{" +
      "id=" + id +
      ", distances=" + distances +
      '}';
  }
}