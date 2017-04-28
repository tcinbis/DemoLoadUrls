import java.util.ArrayList;

/**
 * Created by tcinb on 27.04.2017.
 */
public class Main {

  private static final int maxEntry = 1000000;
  private static String[] urls = new String[1000000];
  private static int placeToInsert = 0;
  public static boolean doneReading = false;

  public static void insertInArray(String url){
    urls[placeToInsert] = url;
    ++placeToInsert;
  }

  public static void main(String[] args) {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    UrlDatabase urlDatabase = new UrlDatabase();
    ReadFromFile readFromFile = new ReadFromFile();
    readFromFile.readUrl();
    try {
      Thread.sleep(0, 600);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //urlDatabase.dropTable();
    urlDatabase.disableAutoCommit();

    long timeStart = System.currentTimeMillis();
    int i = 0;
    while (i < 1000000) {

      urlDatabase.insert(urls[i]);

      if (i % 200000 == 0 && i > 0) {
        System.out.println(i);
        urlDatabase.commit();
        System.out.println("Done");
      }
      i++;
    }

    urlDatabase.commit();
    urlDatabase.close();
    System.out.println(
        (System.currentTimeMillis() - timeStart) / 1000.0 + "s needed for " + maxEntry + " urls!");
  }
}
