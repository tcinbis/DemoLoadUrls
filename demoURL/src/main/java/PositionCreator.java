import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author Max
 */
public class PositionCreator {

  private static final String FILENAME = "urls-sample.txt";
  private static final int FACTOR = 10_000_000;
  private ArrayList <Long> positions = new ArrayList<>();
  private static InputStream is;

  PositionCreator() {

  }

  void generatePositions() {
    //read through the file and enter the positions of "\n" into an Array
    try {
      is = new BufferedInputStream(new FileInputStream(FILENAME));
      byte[] c = new byte[1024];
      int count = 0;
      int readChars = 0;
      long positionCounter = 0;
      int counterInPositionsArray = 0;
      boolean empty = true;
      while ((readChars = is.read(c)) != -1) {
        empty = false;
        for (int i = 0; i < readChars; ++i) {
          if (c[i] == '\n') {

            ++count;
            if (count % FACTOR == 0 && count > 0) {
              positions.add(positionCounter + i);
              ++counterInPositionsArray;
              count = 0;
              System.out.println(counterInPositionsArray);
            }
          }
        }
        positionCounter += readChars;
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public ArrayList<Long> getPositions(){
    return positions;
  }
}
