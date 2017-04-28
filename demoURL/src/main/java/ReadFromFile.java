import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by tcinb on 27.04.2017.
 */
public class ReadFromFile extends Thread {

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

  /**
   * This method reads the counter from txt file
   *
   * @return Returns a integer, which represents the counter. Returns -1 if there was an error.
   */
  public String readUrl() {
    try {
      if (bufferedReader != null) {
        String url = bufferedReader.readLine();
        //bufferedReader.close();
        return url;
      } else {
        return "null";
      }
    } catch (IOException e) {
      System.err.println("Error reading from file");
      e.printStackTrace();
    } catch (NumberFormatException e) {
      System.err.println("Couldnt load map from File, because of corrupted init values");
      e.printStackTrace();
    }
    return "null";
  }

  public void run() {
    String url = "";
    try {
      while ((url = bufferedReader.readLine()) != null) {
        Main.addUrls(url);
      }
    } catch (IOException e) {
      System.err.println("Error reading from file");
      e.printStackTrace();
    } catch (NumberFormatException e) {
      System.err.println("Couldnt load map from File, because of corrupted init values");
      e.printStackTrace();
    }
  }
}
