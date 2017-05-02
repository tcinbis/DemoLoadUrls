/**
 * Created by tcinb on 30.04.2017.
 */
public class ProcessUrls extends Thread {

  private volatile boolean stop = false;
  public static boolean startProcessing = false;

  public void run() {
    UrlDatabase urlDatabase = new UrlDatabase("");
    urlDatabase.dropTable();
    urlDatabase.disableAutoCommit();
    Main.readingThread.start();
    while (!stop) {
        System.out.println("starting to wait for Reader!");
        while (!startProcessing){
          //Wait for reader
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        System.out.println("Reader is done reading. Start processing now!");
        int counterForStart = 0;
        int counterForTmp = 0;
        char[] tmp = new char[ReadFromFileNIO.start[counterForStart]];
        for (int i = 0; i < ReadFromFileNIO.BUFFERSIZE; i++) {
          if (i != ReadFromFileNIO.start[counterForStart]) {
            tmp[counterForTmp] = ReadFromFileNIO.urls[i];
            ++counterForTmp;
          } else { //one url read
            urlDatabase.insert(String.valueOf(tmp));
            ++counterForStart;
            tmp = new char[ReadFromFileNIO.start[counterForStart]];
            counterForTmp = 0;
            if (i % 1000 == 0){
              System.out.println("Commiting!");
              urlDatabase.commit();
            }
          }
        }
      System.out.println("Commiting 2!");
        urlDatabase.commit();
        System.out.println("Commited one Buffer!");
        startProcessing = false;
        ReadFromFileNIO.startReading = true;
      //}
    }
  }

  public void requestStop() {
    stop = true;
  }
}
