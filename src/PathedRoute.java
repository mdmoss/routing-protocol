import java.io.Serializable;
import java.util.ArrayList;

public class PathedRoute extends Route {
  public String path;

  @Override
  public String toString() {
    return "PathedRoute{" +
      "dest=" + dest +
      ", via=" + via +
      ", cost=" + cost +
      ", path=" + path +
      '}';
  }

  public PathedRoute(char dest, float cost, String path) {
    super(dest, path.charAt(0), cost);
    this.path = path;
  }
}
