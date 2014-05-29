import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.util.HashMap;

public class UpdateListener implements Runnable {

  Router router;

  public UpdateListener(Router router) {
    this.router = router;
  }

  byte[] buf = new byte[2048];
  DatagramPacket p = new DatagramPacket(buf, 2048);

  @Override
  public void run() {
    while (true) {
      try {
        router.routeUpdateSock.receive(p);
        ByteArrayInputStream bytes = new ByteArrayInputStream(buf);
        RoutesMsg rec = (RoutesMsg) new ObjectInputStream(bytes).readObject();

        boolean updated = false;
        synchronized (router.routingTable) {
          for (Route r : rec.routes) {
            if (r.dest == router.id) continue; /* That's me! */

            if (!router.routingTable.containsKey(r.dest)) {
            /* It's the only way we know of to get there. Go for it! */
              float costToNode = router.routingTable.get(rec.id).cost + r.cost;
              router.routingTable.put(r.dest, new Route(r.dest, rec.id, costToNode));
              router.debug(String.format("found a new route to %c", r.dest));
              updated = true;

            } else if (r.cost + router.routingTable.get(rec.id).cost < router.routingTable.get(r.dest).cost) {
            /* We've found a shorter route. Update the table. */
              float costToNode = router.routingTable.get(rec.id).cost;
              router.routingTable.put(r.dest, new Route(r.dest, rec.id, costToNode + r.cost));
              router.debug(String.format("found a shorter route to %c", r.dest));
              updated = true;

            } /* Otherwise, it's just more expensive. Let it go. */
          }
          if (!updated) {
            router.tableAge++;
          } else {
            router.tableAge = 1;
          }

          if (router.tableAge == 9) {
            for (Route r : router.routingTable.values()) {
              router.debug(String.format("Shortest path to node %c: the next hop is %c and the cost is %s", r.dest, r.via, r.cost));
            }
          }
        }

      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
}
