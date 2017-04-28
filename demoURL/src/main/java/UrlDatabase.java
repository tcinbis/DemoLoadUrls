import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by tcinb on 27.04.2017.
 */
public class UrlDatabase {

  private static final String URL = "jdbc:sqlite:C:/sqlite/db/url.db";
  private static final String CREATE = "CREATE TABLE IF NOT EXISTS urls (\n"
      + "	url text NOT NULL\n"
      + ");";
  private static final String INSERT = "INSERT INTO urls(url) VALUES(?);";
  private static final String INDEX = "CREATE INDEX urlIndex ON urls (url);";
  private static final String DROP_TABLE = "DROP TABLE IF EXISTS urls;";
  private Connection connection;
  private PreparedStatement preparedStatement;

  UrlDatabase(){
    try{
      boolean result = Files.deleteIfExists(Paths.get("C:/sqlite/db/url.db"));
      if (result){
        Files.delete(Paths.get("C:/sqlite/db/url.db"));
      }
      createNewDatabase();

      if (connection != null && !connection.isClosed()){
        try(Statement statement = connection.createStatement()){
          connection.setAutoCommit(true);
          preparedStatement = connection.prepareStatement(INSERT);
        } catch (SQLException e){
          e.printStackTrace();
        }
      } else if (connection.isClosed()){
        connection = DriverManager.getConnection(URL);
        connection.setAutoCommit(true);
        preparedStatement = connection.prepareStatement(INSERT);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void createNewDatabase() {
    try (Connection conn = DriverManager.getConnection(URL)) {
      if (conn != null) {
        System.out.println("Connection created");
        connection = conn;
        DatabaseMetaData meta = conn.getMetaData();
        System.out.println("The driver name is " + meta.getDriverName());
        System.out.println("A new database has been created.");
        createNewTable();
      }

    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public void createNewTable(){
    try(Statement statement = connection.createStatement()){
      if (connection != null) {
        connection.setAutoCommit(true);
        statement.execute(CREATE);
        createIndexOnTable();
        preparedStatement = connection.prepareStatement(INSERT);
      }

    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public void createIndexOnTable(){
    try (PreparedStatement preparedStatement = connection.prepareStatement(INDEX)) {
      preparedStatement.executeUpdate();
    } catch (SQLException e){
      e.printStackTrace();
    }
  }

  public void dropTable(){
    try {
      preparedStatement = connection.prepareStatement(DROP_TABLE);
      preparedStatement.execute();
      createNewTable();
      preparedStatement = connection.prepareStatement(INSERT);
    } catch (SQLException e){
      e.printStackTrace();
    }
  }

  public void insert(String urlFromFile){
    try {
      if (urlFromFile != null && !urlFromFile.equalsIgnoreCase("null")) {
        preparedStatement.setString(1, urlFromFile);
        preparedStatement.executeUpdate();
      }
    } catch (SQLException e){
      e.printStackTrace();
    }
  }

  public void commit(){
    try {
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void close(){
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void disableAutoCommit(){
    try {
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
