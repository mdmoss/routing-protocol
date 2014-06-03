import java.io.Serializable;
import java.util.HashMap;

public class DistanceVector implements Serializable {
  /* I am node `id` */
  char id;
  /* My cost to node `k` is `v` */
  HashMap<Character, Float> distances;
  /* I use this path to get there */
  HashMap<Character, String> paths;

  public DistanceVector(char id, HashMap<Character, Float> distances, HashMap<Character, String> paths) {
    this.id = id;
    this.distances = distances;
    this.paths = paths;
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
    for (Character k : this.distances.keySet()) {
      if (!that.distances.containsKey(k) || !that.distances.get(k).equals(this.distances.get(k))) return false;
    }

    if (this.paths != null && that.paths != null) {
      for (Character k : that.paths.keySet()) {
        if (!this.paths.containsKey(k) || !this.paths.get(k).equals(that.paths.get(k))) return false;
      }
      for (Character k : this.paths.keySet()) {
        if (!that.paths.containsKey(k) || !that.paths.get(k).equals(this.paths.get(k))) return false;
      }
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