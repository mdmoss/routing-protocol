/*
 * dv_routing.java
 *
 * Matthew Moss <mdm@cse.unsw.edu.au>
 * comp3331 s1 2014
 */

import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dv_routing_ext {
  public static void main(String[] args) throws IOException {
    if (!(args.length == 3 || args.length == 4)) {
      System.err.println("Usage: dv_routing id port config [-p|-e]");
      System.exit(1);
    }

    char id = args[0].charAt(0);
    if (!(id >= 'A' && id <= 'Z')) {
      System.err.println("Spec violation: invalid node ID");
      System.exit(1);
    }

    int port = Integer.parseInt(args[1]);

    BufferedReader reader = new BufferedReader(new FileReader(args[2]));
    int numNeighbors = Integer.parseInt(reader.readLine());
    HashMap<Character, Neighbor> neighbors = new HashMap<Character, Neighbor>();

    boolean poisoned = (args.length == 4 && args[3].equals("-p"));
    boolean extended = (args.length == 4 && args[3].equals("-e"));

    if (poisoned || extended) {
      Pattern p = Pattern.compile("([A-Z]) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([0-9]+)");
      for (int i = 0; i < numNeighbors; i++) {
        Matcher m = p.matcher(reader.readLine());
        if (m.find()) {
          Neighbor n = new Neighbor(m.group(1).charAt(0), Float.parseFloat(m.group(2)), Integer.parseInt(m.group(4)), Float.parseFloat(m.group(3)));
          neighbors.put(m.group(1).charAt(0), n);
        } else {
          System.err.println("Spec violation: invalid config file format");
          System.exit(1);
        }
      }

    } else {
      Pattern p = Pattern.compile("([A-Z]) ([-+]?[0-9]*\\.?[0-9]+) ([0-9]+)");
      for (int i = 0; i < numNeighbors; i++) {
        Matcher m = p.matcher(reader.readLine());
        if (m.find()) {
          /* Cost == updatedCost */
          Neighbor n = new Neighbor(m.group(1).charAt(0), Float.parseFloat(m.group(2)), Integer.parseInt(m.group(3)), Float.parseFloat(m.group(2)));
          neighbors.put(m.group(1).charAt(0), n);
        } else {
          System.err.println("Spec violation: invalid config file format");
          System.exit(1);
        }
      }
    }


    new Router(id, port, neighbors, poisoned, extended).run();
  }
}
