/**
 * Created by tcinb on 27.04.2017.
 */
public class Main {

  private static final int maxEntry = 1000000;

  public static void main(String[] args) {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    UrlDatabase urlDatabase = new UrlDatabase();
    ReadFromFile readFromFile = new ReadFromFile();
    urlDatabase.dropTable();
    urlDatabase.disableAutoCommit();

    long timeStart = System.currentTimeMillis();
    for (int i = 0;i < maxEntry;i++){
      urlDatabase.insert(readFromFile.readUrl());
      if (i%200000 ==0){
        System.out.println(i);
        urlDatabase.commit();
        System.out.println("Done");
      }
    }
    System.out.println((System.currentTimeMillis()-timeStart)/1000.0 +"s to insert "+maxEntry);
    urlDatabase.commit();
    urlDatabase.close();
  }
}
