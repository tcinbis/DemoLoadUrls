import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * Created by tcinb on 27.04.2017.
 */
public class UrlDatabase {

  private static String DATABASEURL = "jdbc:postgresql://localhost:8090/postgres";
  private static final String DATABASENAME = "urldatabase";
  private String nameFromThread;
  private String CREATE_TABLE;
  private String INSERT;
  private String INDEX;
  private String DROP_TABLE;
  private Connection connection;
  private PreparedStatement preparedStatement;

  UrlDatabase(String nameFromThread) {
    this.nameFromThread = nameFromThread;
    CREATE_TABLE = "CREATE TABLE IF NOT EXISTS urls" + nameFromThread + " (url text NOT NULL);";
    INSERT = "INSERT INTO urls"+nameFromThread+" (url) VALUES (?);";
    INDEX = "CREATE INDEX urlIndex"+nameFromThread+" ON urls"+nameFromThread+" (url);";
    DROP_TABLE = "DROP TABLE IF EXISTS urls"+nameFromThread+";";

    try {
      connection = DriverManager.getConnection(DATABASEURL, "postgres", "admin");
      if (connection != null) {
        System.out.println("Connection created");
        createNewDatabase();
        System.out.println(DATABASEURL);
        connection = DriverManager.getConnection(DATABASEURL, "postgres", "admin");
      } else {
        System.out.println("Missing datapase urlDatabase");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (connection != null) {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate(CREATE_TABLE);
        statement.close();
        System.out.println("Created");
        connection = DriverManager.getConnection(DATABASEURL, "postgres", "admin");
        preparedStatement = connection.prepareStatement(INSERT);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public void createNewDatabase() {
    try {
      if (connection != null) {
        Statement createStatement = connection.createStatement();
        createStatement.executeUpdate("CREATE DATABASE "+DATABASENAME+";");
        System.out.println("Created Database");

      }

    } catch (SQLException e) {
      System.out.println(e.getMessage());
    } finally {
      DATABASEURL = "jdbc:postgresql://localhost:8090/"+DATABASENAME;
    }
  }

  public void dropTable() {
    try {
      preparedStatement = connection.prepareStatement(DROP_TABLE);
      preparedStatement.execute();
      preparedStatement = null;
      preparedStatement = connection.prepareStatement(INSERT);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void insert(String urlFromFile) {
    try {
      if (!connection.isClosed()) {
        preparedStatement.setString(1, urlFromFile);
        preparedStatement.executeUpdate();
      } else {
        connection = DriverManager.getConnection(DATABASEURL, "postgres", "admin");
        preparedStatement.setString(1, urlFromFile);
        preparedStatement.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void commit() {
    try {
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void disableAutoCommit() {
    try {
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void copyFromFile() {

    System.err.println("Copying text data rows from stdin");

    CopyManager copyManager = null;
    try {
      copyManager = new CopyManager((BaseConnection) connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    FileReader fileReader = null;
    try {
      fileReader = new FileReader("urls-sample.txt");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    try {
      copyManager.copyIn("COPY urls1 FROM STDIN", fileReader);
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    }

    System.err.println("Done.");
  }
}