import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Tom
 */
public class Main extends Thread {

  public static void main(String[] args) {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    UrlDatabase database = new UrlDatabase(1+"");
    database.copyFromFile(false);

    PreOrderDatabase preOrderDatabase = new PreOrderDatabase();

    preOrderDatabase.calculateCounts();

    preOrderDatabase.splitInHttpAndS();
    preOrderDatabase.orderWithWWW("http");
    preOrderDatabase.orderWithoutWWW("http");

    preOrderDatabase.orderWithWWW("https");
    preOrderDatabase.orderWithoutWWW("https");
  }
}
