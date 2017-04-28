import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by tcinb on 27.04.2017.
 */
public class ReadFromFile {

  private FileReader fileReader;
  private BufferedReader bufferedReader;
  private static final String FILENAME = "urls.csv";

  ReadFromFile() {
    try {
      initReader(FILENAME);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This small method is used to initialize our fileReader to read from the txt file
   *
   * @throws IOException If we can not open our fileReader
   */
  private void initReader(String filename) throws IOException {
    File file = new File(FILENAME);
    if (file.exists()) {
      fileReader = new FileReader(filename);
      bufferedReader = new BufferedReader(fileReader);
    }

  }

  public void readUrl() {
    String url = "start";
    try {
      while ((url = bufferedReader.readLine()) != null) {
        Main.insertInArray(url);
      }
    } catch (IOException e) {
      System.err.println("Error reading from file");
      e.printStackTrace();
    } catch (NumberFormatException e) {
      System.err.println("Couldnt load map from File, because of corrupted init values");
      e.printStackTrace();
    }
    System.out.println("DONE");
    Main.doneReading = true;
  }
}
