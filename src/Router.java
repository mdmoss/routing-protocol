/*
 * Router.java
 *
 * Matthew Moss <mdm@cse.unsw.edu.au>
 * comp3331 s1 2014
 */

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
  boolean extended;

  final Map<Character, DistanceVector> neighborDVs = new HashMap<Character, DistanceVector>();
  HashMap<Character, Integer> neighbourStability = new HashMap<Character, Integer>();

  Timer update;
  DatagramSocket routeUpdateSock;

  Timer hbCheck;
  final Map<Character, Long> lastMsg = new HashMap<Character, Long>();
  ArrayList<Character> deadList = new ArrayList<Character>();

  boolean stable = false;
  boolean hasStabilised = false;

  public Router(char id, int port, Map<Character, Neighbor> neighbors, boolean poisonedReverse, boolean extended) throws SocketException {
    this.id = id;
    this.port = port;
    this.neighbors = neighbors;
    this.poisonedReverse = poisonedReverse;
    this.extended = extended;

    for (Neighbor n : neighbors.values()) {
      neighborDVs.put(n.id, new DistanceVector(n.id, new HashMap<Character, Float>(), new HashMap<Character, String>()));
      lastMsg.put(n.id, System.currentTimeMillis());
      neighbourStability.put(n.id, 0);
    }

    routeUpdateSock = new DatagramSocket(port);
    new Thread(new UpdateListener(this)).start();

    if (poisonedReverse) {
      schedulePoisonedUpdate();
    } else if (extended) {
      scheduleExtendedUpdate();
    } else {
      scheduleUpdate();
    }
    scheduleHeartbeatCheck();
  }

  @Override
  public String toString() {
    return "Router{" +
      "extended=" + extended +
      ", poisonedReverse=" + poisonedReverse +
      ", neighbors=" + neighbors +
      ", port=" + port +
      ", id=" + id +
      '}';
  }

  public void run() {
    /* There's no spec provisions for clean shutdown, so this is close enough */
    while (true);
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
                                       DistanceVector dv = new DistanceVector(id, distances, null);
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

  public void schedulePoisonedUpdate() {
    assert(update == null);

    update = new Timer();
    update.scheduleAtFixedRate(new TimerTask() {
                                 @Override
                                 public void run() {

                                   for (Neighbor n : neighbors.values()) {

                                     try {
                                       ByteArrayOutputStream out = new ByteArrayOutputStream();
                                       ObjectOutputStream obj = new ObjectOutputStream(out);
                                       synchronized (neighborDVs) {
                                         HashMap<Character, Float> distances = new HashMap<Character, Float>();
                                         for (Route r : routeSet()) {
                                           if (r.via == n.id) {
                                             distances.put(r.dest, Float.MAX_VALUE);
                                           } else {
                                             distances.put(r.dest, r.cost);
                                           }
                                         }
                                         DistanceVector dv = new DistanceVector(id, distances, null);
                                         obj.writeObject(dv);
                                       }

                                       DatagramPacket p = new DatagramPacket(out.toByteArray(), out.size());
                                       DatagramSocket sock = new DatagramSocket();
                                       p.setAddress(InetAddress.getLocalHost());
                                       p.setPort(n.port);
                                       sock.send(p);

                                     } catch (IOException e) {
                                       e.printStackTrace();
                                     }

                                   }
                                 }
                               },
      0,
      5000
    );
  }

  public void scheduleExtendedUpdate() {
    assert(update == null);

    update = new Timer();
    update.scheduleAtFixedRate(new TimerTask() {
                                 @Override
                                 public void run() {

                                   for (Neighbor n : neighbors.values()) {

                                     try {
                                       ByteArrayOutputStream out = new ByteArrayOutputStream();
                                       ObjectOutputStream obj = new ObjectOutputStream(out);
                                       synchronized (neighborDVs) {
                                         HashMap<Character, Float> distances = new HashMap<Character, Float>();
                                         HashMap<Character, String> paths = new HashMap<Character, String>();
                                         for (PathedRoute r : pathedRouteSet(n.id)) {
                                           distances.put(r.dest, r.cost);
                                           paths.put(r.dest, r.path);
                                         }
                                         DistanceVector dv = new DistanceVector(id, distances, paths);
                                         obj.writeObject(dv);
                                       }

                                       DatagramPacket p = new DatagramPacket(out.toByteArray(), out.size());
                                       DatagramSocket sock = new DatagramSocket();
                                       p.setAddress(InetAddress.getLocalHost());
                                       p.setPort(n.port);
                                       sock.send(p);

                                     } catch (IOException e) {
                                       e.printStackTrace();
                                     }

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
            deadList.add(n.id);
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
    nodeSet.removeAll(deadList);

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
        distance = neighborCost(n);
      }

      for (Character neighbor : neighborDVs.keySet()) {
        if (neighborDVs.get(neighbor).distances.containsKey(n) &&
            neighborDVs.get(neighbor).distances.get(n) + neighborCost(neighbor) < distance) {
          via = neighbor;
          distance = neighborDVs.get(neighbor).distances.get(n) + neighborCost(neighbor);
        }
      }
      res.add(new Route(n, via, distance));
    }
    return res;
  }

  private Collection<PathedRoute> pathedRouteSet(Character whomFor) {
    Set<Character> nodeSet = new HashSet<Character>();

    /* Add everything we know about, and remove ourselves and known-down nodes */
    nodeSet.addAll(neighbors.keySet());
    for (DistanceVector dv : neighborDVs.values()) {
      nodeSet.addAll(dv.distances.keySet());
    }
    nodeSet.remove(this.id);
    nodeSet.removeAll(deadList);

    /* In-place only sort. How anyone can stand this language is beyond me */
    ArrayList<Character> nodes = new ArrayList<Character>(nodeSet);
    Collections.sort(nodes);

    ArrayList<PathedRoute> res = new ArrayList<PathedRoute>();
    /* Find the shortest distance and via for each node */
    for (Character n : nodes) {
      char via = '?';
      float distance = Float.MAX_VALUE;
      String path = "";

      if (neighbors.containsKey(n)) {
        via = n;
        distance = neighborCost(n);
        path = "";
      }

      for (Character neighbor : neighborDVs.keySet()) {
        if (neighborDVs.get(neighbor).distances.containsKey(n) &&
          neighborDVs.get(neighbor).distances.get(n) + neighborCost(neighbor) < distance &&
          neighborDVs.get(neighbor).paths.get(n).indexOf(this.id) == -1) {
          via = neighbor;
          distance = neighborDVs.get(neighbor).distances.get(n) + neighborCost(neighbor);
          path = neighborDVs.get(neighbor).paths.get(n);
        }
      }
      res.add(new PathedRoute(n, distance, String.format("%c%s", via, path)));
    }
    return res;
  }

  public void printShortestRoutes() {
    for (Route r : routeSet()) {
      System.out.println(String.format("shortest path to node %s: the next hop is %s and the cost is %s", r.dest, r.via, r.cost));
    }
  }

  public void debug(String msg) {
    /* noop */
  }

  public void stabilise() {
    if (!stable) {
      printShortestRoutes();
    }
    hasStabilised = true;
    stable = true;
  }

  private float neighborCost(Character n) {
    return hasStabilised? neighbors.get(n).updatedCost : neighbors.get(n).cost;
  }
}
