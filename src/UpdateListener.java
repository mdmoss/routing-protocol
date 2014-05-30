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
        DistanceVector dv = (DistanceVector) new ObjectInputStream(bytes).readObject();

        synchronized (router.neighborDVs) {
          if (!router.neighborDVs.get(dv.id).equals(dv)) {
            /* Updated neighbor DV found */
            router.neighborDVs.put(dv.id, dv);
            router.stabilisationCount = 1;
          } else {
            /* Everything is as it was */
            router.stabilisationCount++;
          }
        }

        if (router.stabilisationCount == 9) {
          router.printShortestRoutes();
        }

      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
}
