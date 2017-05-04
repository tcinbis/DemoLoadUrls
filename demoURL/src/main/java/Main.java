import java.io.File;
import java.io.IOException;

/**
 * Created by tcinb on 27.04.2017.
 */
public class Main extends Thread {

  public static Thread readingThread;

  public static void main(String[] args) {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    File file = new File("url.db");
    file.delete();

    PositionCreator positionCreator = new PositionCreator();
    positionCreator.getPositions();

    long totalLines = 170000000;
    long linesToReadPerThread = totalLines/17;

    /*for (int i = 0; i <= 17;i++){
      new ReadAndProcessThread(i+"",i*1000000,linesToReadPerThread).start();
    }*/

    //new ReadAndProcessThread("1",0,170000000).start();
  }
}
