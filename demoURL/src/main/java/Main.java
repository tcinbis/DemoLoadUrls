/**
 * Created by tcinb on 27.04.2017.
 */
public class Main {

  private static final int maxEntry = 500000;

  public static void main(String[] args) {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    UrlDatabase urlDatabase = new UrlDatabase();
    ReadFromFile readFromFile = new ReadFromFile();
    for (int i = 0;i <= maxEntry;i++){
      System.out.println(i);
      urlDatabase.insert(readFromFile.readUrl());
      if (i%1000 ==0){
        urlDatabase.commit();
      }
    }
    urlDatabase.commit();
    urlDatabase.close();
  }
}
