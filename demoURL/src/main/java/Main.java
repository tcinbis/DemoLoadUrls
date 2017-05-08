import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by tcinb on 27.04.2017.
 */
public class Main extends Thread {

  public static Thread readingThread;
  private static ArrayList<Long> positions;

  public static void main(String[] args) {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    File file = new File("url.db");
    file.delete();


    PositionCreator positionCreator = new PositionCreator();
    positionCreator.generatePositions();
    positions = positionCreator.getPositions();

    long totalLines = 170000000;
    long linesToReadPerThread = totalLines/17;
    UrlDatabase database = new UrlDatabase(1+"");
    //Start the first Thread
    database.copyFromFile();
    /*new ReadAndProcessThread(0+"",0,positions.get(0)-1,database);

    int nameCounter = 1;
    for (int i = 1; i < positions.size()-1;i++){
      new ReadAndProcessThread(nameCounter+"",positions.get(i),(positions.get(i+1)-positions.get(i)),database).start();
      ++nameCounter;
    }*/

    //new ReadAndProcessThread("1",0,170000000).start();

  }
}
