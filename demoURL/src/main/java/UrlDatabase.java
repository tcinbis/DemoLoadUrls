import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by tcinb on 27.04.2017.
 */
public class UrlDatabase {

  private static final String DATABASEURL = "jdbc:sqlite:url.db";
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
    INSERT = "INSERT INTO urls"+nameFromThread+" (url) VALUES(?);";
    INDEX = "CREATE INDEX urlIndex"+nameFromThread+" ON urls"+nameFromThread+" (url);";
    DROP_TABLE = "DROP TABLE IF EXISTS urls"+nameFromThread+";";

    try {
      connection = DriverManager.getConnection(DATABASEURL);
      if (connection != null) {
        System.out.println("Connection created");
      } else {
        System.out.println("Creating new database!");
        createNewDatabase();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    if (connection != null) {
      try (Statement statement = connection.createStatement()) {
        connection.setAutoCommit(true);
        statement.executeUpdate(CREATE_TABLE);
        System.out.println(INDEX);
        statement.executeUpdate(INDEX);
        preparedStatement = connection.prepareStatement(INSERT);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    disableAutoCommit();
  }

  public void createNewDatabase() {
    try (Connection conn = DriverManager.getConnection(DATABASEURL)) {
      if (conn != null) {
        connection = conn;
        DatabaseMetaData meta = conn.getMetaData();
        System.out.println("The driver name is " + meta.getDriverName());
        System.out.println("A new database has been created.");
      }

    } catch (SQLException e) {
      System.out.println(e.getMessage());
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
      preparedStatement.setString(1, urlFromFile);
      //TODO Compare with addBatch!
      preparedStatement.executeUpdate();
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
}
