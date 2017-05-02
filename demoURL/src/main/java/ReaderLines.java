import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tcinb on 01.05.2017.
 */
public class ReaderLines extends Thread{
  private static final String FILENAME = "urls-sample.txt";
  public static ArrayList<String> urls = new ArrayList<>();
  private UrlDatabase urlDatabase;

  ReaderLines(UrlDatabase database){
    urlDatabase = database;
  }

  public void readLines(){
    File filePath = new File(FILENAME);
    try {
      AtomicInteger counter = new AtomicInteger(0);
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
      reader.lines().forEach(line -> {
        urlDatabase.insert(line);
      });
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    try {
      Thread.sleep(0,50);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void run(){
    readLines();
  }
}
