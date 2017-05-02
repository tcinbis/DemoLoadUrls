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
  private static final long BUFFERSIZE = 1000000;
  private volatile boolean stop = false;

  private UrlDatabase database;
  private FileChannel fileChannel;
  private CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
  private MappedByteBuffer buffer;
  private CharBuffer charBuffer;

  ReadAndProcessThread(String name, int startLine) {
    this.setName(name);
    this.setStartLine(startLine);
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
      for (int i = 0; i < BUFFERSIZE; i++) {
        if (charBuffer.get(i) == 10) {
          int diff = i - startIndexOfChar;
          char[] tmp = new char[diff];
          for (int j = 0; j < diff; j++) {
            tmp[j] = charBuffer.get(startIndexOfChar + j);
          }
          database.insert(String.valueOf(tmp));
          startIndexOfChar = i + 1;
          ++counterOfUrls;
        }
        if (counterOfUrls % 1000000 == 0 && counterOfUrls > 0) {
          database.commit();
          System.out.println("Commit");
        }
      }
      database.commit();
      setStop();
      break;
    }
  }

  private void init() throws IOException {
    database = new UrlDatabase(this.getName());
    fileChannel = new RandomAccessFile(FILENAME, "r").getChannel();
    buffer = fileChannel.map(MapMode.READ_ONLY, startLine, BUFFERSIZE);
    charBuffer = decoder.decode(buffer);
  }

  public void setStartLine(int startLine) {
    this.startLine = startLine;
  }

  public void setStop() {
    this.stop = false;
  }
}
