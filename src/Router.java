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

  final Map<Character, Route> routingTable = new HashMap<Character, Route>();
  int tableAge = 0;

  Timer update;
  DatagramSocket routeUpdateSock;

  public Router(char id, int port, Map<Character, Neighbor> neighbors, boolean poisonedReverse) throws SocketException {
    this.id = id;
    this.port = port;
    this.neighbors = neighbors;
    this.poisonedReverse = poisonedReverse;

    for (Neighbor n : neighbors.values()) {
      routingTable.put(n.id, new Route(n.id, n.id, n.cost));
    }

    routeUpdateSock = new DatagramSocket(port);
    new Thread(new UpdateListener(this)).start();

    scheduleUpdate();
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
          synchronized (routingTable) {
            obj.writeObject(new RoutesMsg(id, new ArrayList<Route>(routingTable.values())));
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
    5000);
  }

  public void debug(String msg) {
    System.err.println(String.format("%c: %s", id, msg));
  }
}
