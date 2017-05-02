import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Created by tcinb on 30.04.2017.
 */
public class ReadFromFileNIO extends Thread {

  private static final String FILENAME = "urls-sample.txt";
  public static final int BUFFERSIZE = 170000000;
  public static char[] urls = new char[BUFFERSIZE];
  public static int[] start = new int[BUFFERSIZE];
  public static boolean startReading = true;

  public void read() {
    FileChannel inChannel = null;
    try {
      inChannel = new RandomAccessFile(FILENAME, "r").getChannel();
      CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();

      int neededBuffers = (int) (inChannel.size() / BUFFERSIZE);
      MappedByteBuffer buffer;
      CharBuffer charBuffer;
      for (int i = 0; i <= neededBuffers; i++) {
        buffer = inChannel.map(MapMode.READ_ONLY, i * BUFFERSIZE, BUFFERSIZE);
        charBuffer = decoder.decode(buffer);
        int counterForStart = 0;
        for (int j = 0; j < charBuffer.length(); j++) {
          char tmp = charBuffer.get();
          if (tmp == 10) { //new Line detected
            start[counterForStart] = j;
            ++counterForStart;
          } else {
            urls[j] = tmp;
          }
        }
        System.out.println("Done Reading one Buffer!");
        buffer.clear();
        charBuffer.clear();
        startReading = false;
        ProcessUrls.startProcessing = true;
        while (!startReading){
          //wait for processing to be done
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      inChannel.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    read();
  }
}
