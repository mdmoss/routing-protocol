import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

public class Router {
  char id;
  int port;
  Map<Character, Neighbor> neighbors;
  boolean poisonedReverse;

  final Map<Character, DistanceVector> neighborDVs = new HashMap<Character, DistanceVector>();
  int stabilisationCount = 0;

  Timer update;
  DatagramSocket routeUpdateSock;

  Timer hbCheck;
  final Map<Character, Long> lastMsg = new HashMap<Character, Long>();
  ArrayList<Character> deadlist = new ArrayList<Character>();

  public Router(char id, int port, Map<Character, Neighbor> neighbors, boolean poisonedReverse) throws SocketException {
    this.id = id;
    this.port = port;
    this.neighbors = neighbors;
    this.poisonedReverse = poisonedReverse;

    for (Neighbor n : neighbors.values()) {
      neighborDVs.put(n.id, new DistanceVector(n.id, new HashMap<Character, Float>()));
      lastMsg.put(n.id, System.currentTimeMillis());
    }

    routeUpdateSock = new DatagramSocket(port);
    new Thread(new UpdateListener(this)).start();

    scheduleUpdate();
    scheduleHeartbeatCheck();
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

  public void scheduleUpdate() {
    assert(update == null);

    update = new Timer();
    update.scheduleAtFixedRate(new TimerTask() {
                                 @Override
                                 public void run() {
                                   try {
                                     ByteArrayOutputStream out = new ByteArrayOutputStream();
                                     ObjectOutputStream obj = new ObjectOutputStream(out);
                                     synchronized (neighborDVs) {
                                       HashMap<Character, Float> distances = new HashMap<Character, Float>();
                                       for (Route r : routeSet()) {
                                         distances.put(r.dest, r.cost);
                                       }
                                       DistanceVector dv = new DistanceVector(id, distances);
                                       obj.writeObject(dv);
                                     }

                                     DatagramPacket p = new DatagramPacket(out.toByteArray(), out.size());

                                     for (Neighbor n : neighbors.values()) {
                                       DatagramSocket sock = new DatagramSocket();
                                       p.setAddress(InetAddress.getLocalHost());
                                       p.setPort(n.port);
                                       sock.send(p);
                                     }

                                   } catch (IOException e) {
                                     e.printStackTrace();
                                     System.exit(1);
                                   }
                                 }
                               },
      0,
      5000
    );
  }

  public void scheduleHeartbeatCheck() {
    assert(hbCheck == null);

    hbCheck = new Timer();
    hbCheck.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        ArrayList<Neighbor> neibs = new ArrayList<Neighbor>(neighbors.values());
        for (Neighbor n : neibs) {
          if (System.currentTimeMillis() - lastMsg.get(n.id) > 17000) {
            neighbors.remove(n.id);
            neighborDVs.remove(n.id);
            deadlist.add(n.id);
          }
        }
      }
    },
    0,
    1000);
  }

  private Collection<Route> routeSet() {
    Set<Character> nodeSet = new HashSet<Character>();

    /* Add everything we know about, and remove ourselves and known-down nodes */
    nodeSet.addAll(neighbors.keySet());
    for (DistanceVector dv : neighborDVs.values()) {
      nodeSet.addAll(dv.distances.keySet());
    }
    nodeSet.remove(this.id);
    nodeSet.removeAll(deadlist);

    /* In-place only sort. How anyone can stand this language is beyond me */
    ArrayList<Character> nodes = new ArrayList<Character>(nodeSet);
    Collections.sort(nodes);

    ArrayList<Route> res = new ArrayList<Route>();
    /* Find the shortest distance and via for each node */
    for (Character n : nodes) {
      char via = '?';
      float distance = Float.MAX_VALUE;

      if (neighbors.containsKey(n)) {
        via = n;
        distance = neighbors.get(n).cost;
      }

      for (Character neighbor : neighborDVs.keySet()) {
        if (neighborDVs.get(neighbor).distances.containsKey(n) &&
            neighborDVs.get(neighbor).distances.get(n) + neighbors.get(neighbor).cost < distance) {
          via = neighbor;
          distance = neighborDVs.get(neighbor).distances.get(n) + neighbors.get(neighbor).cost;
        }
      }
      res.add(new Route(n, via, distance));
    }
    return res;
  }

  public void printShortestRoutes() {
    /* We assume nothing will change here, because the assumption is that the system has stabilised */
    for (Route r : routeSet()) {
      debug(String.format("shortest path to node %s: the next hop is %s and the cost is %s", r.dest, r.via, r.cost));
    }
  }

  public void debug(String msg) {
    System.err.println(String.format("%c: %s", id, msg));
  }
}
