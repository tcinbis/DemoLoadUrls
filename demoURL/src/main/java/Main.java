/**
 * Created by tcinb on 27.04.2017.
 */
public class Main extends Thread {

  private static final int maxEntry = 1000000;
  public static boolean doneReading = false;
  public static Thread processingThread;
  public static Thread readingThread;

  public static void main(String[] args) {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    processingThread = new ProcessUrls();
    readingThread = new ReadFromFileNIO();
    processingThread.start();
    //readingThread.start();
    /*UrlDatabase urlDatabase = new UrlDatabase();
    ReadFromFile readFromFile = new ReadFromFile();
    System.exit(0);
    readFromFile.readAllUrls();
    urlDatabase.dropTable();
    urlDatabase.disableAutoCommit();

    long timeStart = System.currentTimeMillis();
    /*int i = 0;
    while (!doneReading) {
      urlDatabase.insert(readFromFile.readUrl());
      if (i % 200000 == 0) {
        System.out.println(i);
        urlDatabase.commit();
        System.out.println("Done");
      }
      if (i == 5000000){
        doneReading = true;
        urlDatabase.commit();
      }
      i++;
    }

    int j = 0;
    for (String urlFromList:readFromFile.getList()){
      urlDatabase.insert(urlFromList);
      if (j%200000 == 0){
        System.out.println(j);
        urlDatabase.commit();
        System.out.println("Done");
      }
      if (j == 10000000){
        break;
      }
      j++;
    }
    System.out.println((System.currentTimeMillis()-timeStart)/1000.0 +"s to insert 10gb");
    urlDatabase.commit();
    urlDatabase.close();*/
  }
}
