import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Created by tcinb on 27.04.2017.
 */
public class ReadFromFile {
  private FileReader fileReader;
  private BufferedReader bufferedReader;
  private static final String FILENAME = "urls-sample.txt";
  private ArrayList<String> list = new ArrayList<>();

  ReadFromFile(){
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
        Main.doneReading = true;
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

  public ArrayList<String> getList() {
    return list;
  }

  public void readAllUrls(){
    Runtime runtime = Runtime.getRuntime();
    int mb = 1024*1024;
    try {
      try (Stream<String> stream = Files.lines(Paths.get(FILENAME))) {
        stream.limit(1000000).forEach(c ->list.add(c));;
        System.out.println("Used Memory:"
            + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        //Print free memory
        System.out.println("Free Memory:"
            + runtime.freeMemory() / mb);
      }
    } catch (IOException e){
      e.printStackTrace();
    }

  }
}
