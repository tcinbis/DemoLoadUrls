import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Created by tcinb on 02.05.2017.
 */
public class ReadAndProcessThread extends Thread {

  private int startLine = 0;
  private static final String FILENAME = "urls-sample.txt";
  private static final long BUFFERSIZE = Integer.MAX_VALUE-10;
  private static final int COMMITLIMIT = 500000;
  private long linesToRead;
  private volatile boolean stop = false;

  private UrlDatabase database; //The database where all data is stored
  private FileChannel fileChannel;
  private CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
  private MappedByteBuffer buffer;
  private CharBuffer charBuffer;

  ReadAndProcessThread(String name, int startLine,long linesToRead) {
    this.setName(name);
    this.setStartLine(startLine);
    this.linesToRead = linesToRead;
  }

  public void run() {
    try {
      init();
    } catch (IOException e) {
      e.printStackTrace();
    }
    while (!stop) {
      System.out.println("Start to process");
      //TODO Implement a fast search algorithem to find \n (10 in ascii code)in charBuffer!
      int startIndexOfChar = 0;
      int counterOfUrls = 0;
      int commitCounter = 0;
      int i = 0;
      while (commitCounter*COMMITLIMIT <= linesToRead){
        if (buffer.get(i) == 10) {
          int diff = i - startIndexOfChar;
          char[] tmp = new char[diff];
          for (int j = 0; j < diff; j++) {
            tmp[j] = (char) buffer.get(startIndexOfChar + j);
          }
          database.insert(String.valueOf(tmp));
          startIndexOfChar = i + 1;
          ++counterOfUrls;
        }
        if (counterOfUrls % 100000 == 0 && counterOfUrls > 0) {
          database.commit();
          counterOfUrls = 0;
          ++commitCounter;
          System.out.println("Commit Counter: "+commitCounter * COMMITLIMIT);
        }
        ++i;
      }
      database.commit();
      setStop();
      break;
    }
  }

  private void init() throws IOException {
    database = new UrlDatabase(this.getName());
    fileChannel = new RandomAccessFile(FILENAME, "r").getChannel();
    //TODO FIND A BETTER WAY TO DETERMINE WHERE TO START READING FROM THE FILE. WE HAVE TO START AT DIFFERENT LINES, NOT AT DIFFERENT POSITIONS IN THE BUFFER
    buffer = fileChannel.map(MapMode.READ_ONLY, startLine, BUFFERSIZE);
    //charBuffer = decoder.decode(buffer);
  }

  public void setStartLine(int startLine) {
    this.startLine = startLine;
  }

  public void setStop() {
    this.stop = false;
  }
}
