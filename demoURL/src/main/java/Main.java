import java.util.ArrayList;

/**
 * Created by tcinb on 27.04.2017.
 */
public class Main {

  private static final int maxEntry = 1000000;
  private static ArrayList<String> urls = new ArrayList<>();

  public static void addUrls(String urls) {
    Main.urls.add(urls);
  }

  public static void main(String[] args) {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    UrlDatabase urlDatabase = new UrlDatabase();
    new ReadFromFile().start();
    urlDatabase.dropTable();
    urlDatabase.disableAutoCommit();

    long timeStart = System.currentTimeMillis();
    for (int i = 0;i < maxEntry;i++){
      urlDatabase.insert(urls.get(0));
      if (i%200000 ==0){
        System.out.println(i);
        urlDatabase.commit();
        System.out.println("Done");
      }
    }
    urlDatabase.commit();
    urlDatabase.close();
    System.out.println((System.currentTimeMillis()-timeStart)/1000.0 + "s needed for "+ maxEntry +" urls!");
  }
}
