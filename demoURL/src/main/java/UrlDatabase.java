import java.beans.Encoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.PSQLException;

/**
 * @author Tom
 */
public class UrlDatabase {

  private static String DATABASEURL = "jdbc:postgresql://localhost:8090/postgres";
  private static final String DATABASENAME = "urldatabase";
  private String INSERT;
  private String CREATE_TABLE;
  private String DROP_TABLE;
  private Connection connection;
  private PreparedStatement preparedStatement;

  UrlDatabase(String nameFromThread) {
    CREATE_TABLE = "CREATE TABLE IF NOT EXISTS urls" + nameFromThread + " (url text NOT NULL);";
    INSERT = "INSERT INTO urls"+nameFromThread+" (url) VALUES (?);";
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
        System.out.println("Created Table");
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
        createStatement.executeUpdate("DROP DATABASE IF EXISTS " + DATABASENAME);
        createStatement.executeUpdate("CREATE DATABASE " + DATABASENAME + " ENCODING='UTF8'");
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

  public void copyFromFile(boolean useBigData) {

    System.err.println("Copying text data rows from stdin");

    CopyManager copyManager = null;
    try {
      copyManager = new CopyManager((BaseConnection) connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    FileReader fileReader = null;
    InputStreamReader reader = null;
    try {
      if (useBigData) {
        reader = new InputStreamReader(new FileInputStream("urls.txt"), "UTF8");
      } else {
        reader = new InputStreamReader(new FileInputStream("urls-sample.txt"), "UTF8");
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    try {
      copyManager.copyIn("COPY urls1 FROM STDIN WITH ENCODING 'UTF-8'", reader);
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    }
    System.err.println("Done.");
  }
}