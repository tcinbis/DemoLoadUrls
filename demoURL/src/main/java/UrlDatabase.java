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
      connection = DriverManager.getConnection(URL);
      if (connection != null){
        System.out.println("Connection created");
      } else {
        System.out.println("Creating new database!");
        createNewDatabase();
      }
    } catch (SQLException e){
      e.printStackTrace();
    }

    if (connection != null){
      try(Statement statement = connection.createStatement()){
        connection.setAutoCommit(true);
        statement.execute(CREATE);
        createIndexOnTable();
        preparedStatement = connection.prepareStatement(INSERT);
      } catch (SQLException e){
        e.printStackTrace();
      }
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

  public void createNewDatabase() {
    try (Connection conn = DriverManager.getConnection(URL)) {
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
      preparedStatement = null;
      createNewTable();
      preparedStatement = connection.prepareStatement(INSERT);
    } catch (SQLException e){
      e.printStackTrace();
    }
  }

  public void insert(String urlFromFile){
    try {
      if (!urlFromFile.equalsIgnoreCase("null")) {
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
