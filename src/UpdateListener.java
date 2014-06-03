/*
 * UpdateListener.java
 *
 * Matthew Moss <mdm@cse.unsw.edu.au>
 * comp3331 s1 2014
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;

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
        DistanceVector dv = (DistanceVector) new ObjectInputStream(bytes).readObject();

        synchronized (router.neighborDVs) {
          if (!router.neighborDVs.get(dv.id).equals(dv)) {
            /* Updated neighbor DV found */
            router.neighborDVs.put(dv.id, dv);
            router.neighbourStability.put(dv.id, 1);
            router.stable = false;
          } else {
            /* Everything is as it was */
            router.neighbourStability.put(dv.id, router.neighbourStability.get(dv.id) + 1);
          }
        }

        boolean stable = true;
        for (Integer i : router.neighbourStability.values()) {
          if (i < 3) {
            stable = false;
          }
        }
        if (stable) {
          router.stabilise();
        }

        router.lastMsg.put(dv.id, System.currentTimeMillis());

      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
}
