package GUI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javax.print.DocFlavor.STRING;

public class AutocompletionTextField extends TextField {

  //Local variables
  //entries to autocomplete
  //private final SortedSet<String> entries;
  private final ArrayList<String> entries;
  //popup GUI
  private ContextMenu entriesPopup;

  private static final String DATABASEURL = "jdbc:postgresql://localhost:8090/urldatabase";
  private Connection connection;
  private Statement statement;
  private ResultSet searchResults;
  private ResultSet searchCount;
  private boolean animate = false;
  private Animation animation;

  private final String HTTP_DATABASE = "urls-http";
  private final String HTTPS_DATABASE = "urls-https";
  private final String HTTP_WWW_DATABASE = "urls-www-http";
  private final String HTTPS_WWW_DATABASE = "urls-www-https";
  private final String REST_DATABASE = "urls-rest";




  public AutocompletionTextField() {
    super();
    this.entries = new ArrayList<>();
    this.entriesPopup = new ContextMenu();
    animation = new Animation();
    animation.start();
    //Connect the database
    try {
      connection = DriverManager.getConnection(DATABASEURL, "postgres", "admin");
      if (connection != null) {
        System.out.println("Connection created");
      } else {
        System.out.println("No Database found");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    setListner();
  }


  /**
   * "Suggestion" specific listners
   */
  private void setListner() {
    //Add "suggestions" by changing text
    textProperty().addListener((observable, oldValue, newValue) -> {
      String enteredText = getText();
      //always hide suggestion if nothing has been entered
      if (enteredText == null || enteredText.isEmpty()) {
        entriesPopup.hide();
      } else {
        //Process the entry text
        try {
          statement = connection.createStatement();
          //animation.setAnimate(true);
          countOccurences(enteredText);
          findOccurences(enteredText);
          //animation.setAnimate(false);
        } catch (SQLException e) {
          e.printStackTrace();
        }
        //filter all possible suggestions depends on "Text", case insensitive
        List<String> filteredEntries = entries.stream()
            .filter(e -> e.toLowerCase().startsWith(enteredText.toLowerCase()))
            .collect(Collectors.toList());
        //some suggestions are found
        if (!filteredEntries.isEmpty()) {
          //build popup - list of "CustomMenuItem"
          populatePopup(filteredEntries, enteredText);
          if (!entriesPopup.isShowing()) { //optional
            entriesPopup.show(AutocompletionTextField.this, Side.BOTTOM, 0, 0); //position of popup
          }
          //no suggestions -> hide
        } else {
          entriesPopup.hide();
        }
      }
    });

    //Hide always by focus-in (optional) and out
    focusedProperty().addListener((observableValue, oldValue, newValue) -> {
      entriesPopup.hide();
    });
  }

  private void findOccurences(String enteredText) throws SQLException {
    ArrayList<String> databases = selectMatchingDatabases(enteredText);
    ArrayList<String> result = new ArrayList<>();
    Character letter = getLetter(enteredText);
    boolean letterischar = false;
    if (letter != null) {
      letterischar = letter.charValue() >= (int) 'a' && letter.charValue() <= (int) 'z';
    }
    while (result.size() < 5) {
      Random ran = new Random();
      int r = ran.nextInt(databases.size());
      int letterint = ran.nextInt(26);
      int offset = ran.nextInt(20);
      char c = (char) (ran.nextInt(26) + 'a');
      switch (databases.get(r)) {
        case HTTP_DATABASE:
          if (letter == null) {
            if (letterint == 0) {
              ResultSet resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTP_DATABASE + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
            } else {
              ResultSet resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTP_DATABASE + "-" + c + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
            }
          } else if (letterischar) {
            ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTP_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          } else {
            ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTP_DATABASE + "\" WHERE url LIKE '" + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          }
          break;
        case HTTP_WWW_DATABASE:
          if (letter == null){
            if (letterint == 0 || enteredText.length() <= "http://www.".length()){
              ResultSet resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTPS_WWW_DATABASE + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
            } else {
              ResultSet resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTP_WWW_DATABASE + "-" + c + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
            }
          } else if (letterischar){
            ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTP_WWW_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          } else {
            ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTP_WWW_DATABASE + "\" WHERE url LIKE '" + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          }
          break;
        case HTTPS_DATABASE:
          if (letter == null) {
            if (letterint == 0) {
              ResultSet resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTPS_DATABASE + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
            } else {
              ResultSet resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTPS_DATABASE + "-" + c + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
            }
          } else if (letterischar) {
            ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTPS_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          } else {
            ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTPS_DATABASE + "\" WHERE url LIKE '" + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          }
          break;
        case HTTPS_WWW_DATABASE:
          if (letter == null){
            if (letterint == 0 || enteredText.length() <= "https://www.".length()){
              ResultSet resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTPS_WWW_DATABASE + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
            } else {
              ResultSet resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTP_WWW_DATABASE + "-" + c + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
            }
          } else if (letterischar){
            ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTP_WWW_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          } else {
            ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTP_WWW_DATABASE + "\" WHERE url LIKE '" + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          }
          break;
        case REST_DATABASE:
          ResultSet resultSet = statement.executeQuery(
              "SELECT * FROM \"" + REST_DATABASE + "\" WHERE url LIKE '" + enteredText
                  + "%' LIMIT 1 OFFSET " + offset);
      }
      while (searchResults != null && searchResults.next()) {
        result.add(searchResults.getString(1));
      }
    }

    System.out.println("Search results loaded.");

    entries.clear();

    Collections.sort(result);
    entries.addAll(result);
  }

  private Character getLetter(String enteredText) {
    Character letter = null;
    if (enteredText.startsWith("http://")){
      if (enteredText.length() > 7) {
        letter = enteredText.charAt(7);
      }
    } if (enteredText.startsWith("http://www.")){
      if (enteredText.length() > 11) {
        letter = enteredText.charAt(11);
      } else {
        letter = null;
      }
    } if (enteredText.startsWith("https://")){
      if (enteredText.length() > 8) {
        letter = enteredText.charAt(8);
      }
    } if (enteredText.startsWith("https://www.")){
      if (enteredText.length() > 12) {
        letter = enteredText.charAt(12);
      } else {
        letter = null;
      }
    }
    return letter;
  }

  private void countOccurences(String enteredText) throws SQLException {
    //searchCount = statement.executeQuery(selectDatabase(enteredText,true));
    //System.out.println(selectDatabase(enteredText,true));
    System.out.println("Search count done.");
    /*
    if (searchCount != null && searchCount.next()) {
      System.out.println(searchCount.getString(1));
      AutoSuggestion.displayCount(searchCount.getString(1));
    }*/
  }

  private ArrayList<String> selectMatchingDatabases(String enteredText) throws SQLException {
    String Shttps = "https://";
    String Shttpswww = "https://www.";
    String Shttp = "http://";
    String Shttpwww = "http://www.";
    ArrayList<String>  resultlist = new ArrayList<>();
    if (enteredText.regionMatches(0,Shttps,0,Math.min(enteredText.length(),Shttps.length())) && !enteredText.startsWith(Shttpswww)) {
      resultlist.add(HTTPS_DATABASE);
    } if (enteredText.regionMatches(0,Shttp,0,Math.min(enteredText.length(),Shttp.length())) && !enteredText.startsWith(Shttpwww)) {
      resultlist.add(HTTP_DATABASE);
    } if (enteredText.regionMatches(0,Shttpswww,0,Math.min(enteredText.length(),Shttpswww.length()))){
      resultlist.add(HTTPS_WWW_DATABASE);
    } if (enteredText.regionMatches(0,Shttpwww,0,Math.min(enteredText.length(),Shttpwww.length()))){
      resultlist.add(HTTP_WWW_DATABASE);
    } if (enteredText.length() < Shttp.length() || !(enteredText.startsWith(Shttp)|| enteredText.startsWith(Shttps))){
      resultlist.add(REST_DATABASE);
    }
    System.out.println(Arrays.toString(resultlist.toArray()));
    return resultlist;
  }

  /**
   * Populate the entry set with the given search results. Display is limited to 10 entries, for
   * performance.
   *
   * @param searchResult The set of matching strings.
   */
  private void populatePopup(List<String> searchResult, String searchReauest) {
    //List of "suggestions"
    List<CustomMenuItem> menuItems = new LinkedList<>();
    //List size - 10 or founded suggestions count
    int maxEntries = 5;
    int count = Math.min(searchResult.size(), maxEntries);
    //Build list as set of labels
    for (int i = 0; i < count; i++) {
      final String result = searchResult.get(i);
      //label with graphic (text flow) to highlight founded subtext in suggestions
      Label entryLabel = new Label();
      //entryLabel.setGraphic(Styles.buildTextFlow(result, searchReauest));
      entryLabel.setPrefHeight(30);  //don't sure why it's changed with "graphic"
      //entryLabel.setPrefWidth(100);
      entryLabel.setText(result);
      CustomMenuItem item = new CustomMenuItem(entryLabel, true);
      menuItems.add(item);

      //if any suggestion is select set it into text and close popup
      item.setOnAction(actionEvent -> {
        System.out.println(result);
        setText(result);
        positionCaret(result.length());
        entriesPopup.hide();
      });
    }

    //"Refresh" context menu
    entriesPopup.getItems().clear();
    entriesPopup.getItems().addAll(menuItems);
  }

  /**
   * Get the autocomplete entries.
   *
   * @return The existing autocomplete entries.
   */
  public ArrayList<String> getEntries() {
    return entries;
  }
}
