import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by tcinb on 07.05.2017.
 */
public class PreOrderDatabase {

  private static String DATABASEURL = "jdbc:postgresql://localhost:8090/urldatabase";
  private static final String DATABASENAME = "urldatabase";
  private Connection connection;

  PreOrderDatabase() {
    try {
      connection = DriverManager.getConnection(DATABASEURL, "postgres", "admin");
      if (connection != null) {
        System.out.println("Connection created");
        System.out.println(DATABASEURL);
        connection = DriverManager.getConnection(DATABASEURL, "postgres", "admin");
      } else {
        System.err.println("Missing datapase urlDatabase");
        throw new InvalidParameterException();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Here we will calculate some counts which we always need at runtime and which would take too long to get at runtime. That's why we
   * decided to preload/pre calculate these values and store them inside a table.
   */
  public void calculateCounts(){
    try {
      Statement statement = connection.createStatement();
      String createCountTable = "CREATE TABLE IF NOT EXISTS counts (count text NOT NULL);";
      statement.execute(createCountTable);

      String[] querries = new String[19];
      querries[0] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'h%'));";
      querries[1] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'ht%'));";
      querries[2] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'htt%'));";
      querries[3] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'http%'));";
      querries[4] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'http:%'));";
      querries[5] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'http:/%'));";
      querries[6] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'http://%'));";
      querries[7] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'https%'));";
      querries[8] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'https:%'));";
      querries[9] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'https:/%'));";
      querries[10] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'https://%'));";
      querries[11] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'http://w%'));";
      querries[12] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'http://ww%'));";
      querries[13] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'http://www%'));";
      querries[14] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'https://w%'));";
      querries[15] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'https://ww%'));";
      querries[16] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'https://www%'));";
      querries[17] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'https://www.%'));";
      querries[18] = "INSERT INTO counts (count) VALUES ((SELECT COUNT(*) FROM urls1 WHERE url LIKE 'http://www.%'));";

      int counter = 0;
      long time = System.currentTimeMillis();
      for (String querry:querries){
        statement.addBatch(querry);
        System.out.println("Added "+counter);
        ++counter;
      }
      statement.executeBatch();
      System.out.println("Needed time "+ (System.currentTimeMillis()-time)/1000 +" s");
    } catch (SQLException e){
      e.printStackTrace();
    }
  }

  /**
   * This method will split our 'big' Table in two smaller amounts and store them in an according table.
   */
  public void splitInHttpAndS() {
    try {
      Statement statement = connection.createStatement();

      String createTableFromSelectHttps =
          "SELECT * INTO \"urls-https\" FROM urls1 WHERE url LIKE 'https://%';";
      String renameOldTable = "ALTER TABLE \"urls1\" RENAME TO tmp";
      String moveUnprocessedHttpEntries = "SELECT * INTO \"urls1\" FROM tmp WHERE url NOT LIKE 'https://%'";

      System.out.println("Start splitting into two");
      //statement.execute(createTableFromSelectHttps);
      System.out.println("Moved all https:// entries to urls-https");
      statement.execute(renameOldTable);
      System.out.println("Renamed urls1 to tmp");
      statement.execute(moveUnprocessedHttpEntries);
      System.out.println("Created table with unprocessed entries");
      statement.execute("DROP TABLE tmp");
      System.out.println("tmp dropped");

      String createTableFromSelectHttp =
          "SELECT * INTO \"urls-rest\" FROM urls1 WHERE url NOT LIKE 'http://%';";
      moveUnprocessedHttpEntries = "SELECT * INTO urls1 FROM tmp WHERE url LIKE 'http://%'";
      statement.execute(createTableFromSelectHttp);
      System.out.println("Moved all NOT http:// entries to urls-rest");
      statement.execute(renameOldTable);
      System.out.println("Renamed table to tmp");
      statement.execute(moveUnprocessedHttpEntries);
      System.out.println("Created table with unprocessed entries");
      statement.execute("DROP TABLE tmp");
      System.out.println("Dropped table tmp");

      String renameTabel = "ALTER TABLE urls1 RENAME TO \"urls-http\";";
      statement.execute(renameTabel);
      System.out.println("Renamed url1 to urls-http");

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method is supposed to order all entries starting with http:// (without www) by a-z
   * First we select all matching entries and write them into a new table called by the according prefix, www or without www and
   * the correct letter (a-z). Then we rename the old table, because there are some entries inside we already processed, to later be able to delete
   * it. But before deleting the 'tmp' table we select all unprocessed entries and save them. Now we can drop the tmp table and continue
   * processing the data. Everything starts again now.
   * @param whichPrefix Prefix can be http or https
   */
  public void orderWithoutWWW(String whichPrefix) {
    try {
      long timeStart = System.currentTimeMillis();
      Statement statement = connection.createStatement();
      // i=97 ,because 97 is an 'a' in ascii code and 122 is a 'z'
      for (int i = 97; i <= 122; i++) {

        //First select all matching entries and write them in a new table
        String createTableFromSelect =
            "SELECT * INTO \"urls-"+whichPrefix+ "-" + (char) i + "\" FROM \"urls-"+whichPrefix+"\" WHERE url LIKE '"+whichPrefix+"://" + (char) i + "%';";
        statement.execute(createTableFromSelect);

        //Rename the table from where we selected matching entries before
        String renameOldTable = "ALTER TABLE \"urls-"+whichPrefix+"\" RENAME TO tmp";
        statement.execute(renameOldTable);

        //Select all entries which are left for processing and write them in a new table
        String createTableFromSelectAfter =
            "SELECT * INTO \"urls-"+whichPrefix+ "\" FROM tmp WHERE url NOT LIKE '"+whichPrefix+"://" + (char) i + "%';";
        statement.execute(createTableFromSelectAfter);

        //Now we can delete the old Table which only holds entries, we already processed before.
        String dropTmpTable = "DROP TABLE tmp";
        statement.execute(dropTmpTable);
        System.out.println((char) i + " executed.");
      }
      System.out.println("Time to order took: " + (System.currentTimeMillis() - timeStart) / 1000+" s");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method is supposed to order all entries starting with http://www by a-z
   * First we select all matching entries and write them into a new table called by the according prefix, www or without www and
   * the correct letter (a-z). Then we rename the old table, because there are some entries inside we already processed, to later be able to delete
   * it. But before deleting the 'tmp' table we select all unprocessed entries and save them. Now we can drop the tmp table and continue
   * processing the data. Everything starts again now.
   * @param whichPrefix Prefix can be http or https
   */
  public void orderWithWWW(String whichPrefix) {
    try {
      long timeStart = System.currentTimeMillis();
      Statement statement = connection.createStatement();
      // i=97 ,because 97 is an 'a' in ascii code and 122 is a 'z'
      for (int i = 97; i <= 122; i++) {

        //First select all matching entries and write them in a new table
        String createTableFromSelect =
            "SELECT * INTO \"urls-www-"+whichPrefix+ "-" + (char) i + "\" FROM \"urls-"+whichPrefix+"\" WHERE url LIKE '"+whichPrefix+"://www." + (char) i + "%';";
        statement.execute(createTableFromSelect);

        //Rename the table from where we selected matching entries before
        String renameOldTable = "ALTER TABLE \"urls-"+whichPrefix+"\" RENAME TO tmp";
        statement.execute(renameOldTable);

        //Select all entries which are left for processing and write them in a new table
        String createTableFromSelectAfter =
            "SELECT * INTO \"urls-"+whichPrefix+ "\" FROM tmp WHERE url NOT LIKE '"+whichPrefix+"://www." + (char) i + "%';";
        statement.execute(createTableFromSelectAfter);

        //Now we can delete the old Table which only holds entries, we already processed before.
        String dropTmpTable = "DROP TABLE tmp";
        statement.execute(dropTmpTable);
        System.out.println((char) i + " executed.");
      }
      System.out.println("Time to order took: " + (System.currentTimeMillis() - timeStart) / 1000+" s");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
