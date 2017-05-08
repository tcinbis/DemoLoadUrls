package GUI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
  private ArrayList<String> results = new ArrayList<>();
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
    searchResults = statement.executeQuery(selectDatabase(enteredText,false)+"LIMIT 5");
    System.out.println("Search results loaded.");
    entries.clear();
    ArrayList<String> sortedResults = new ArrayList<>();
    while (searchResults != null && searchResults.next()) {
      sortedResults.add(searchResults.getString(1));
      System.out.println(searchResults.getString(1));
    }
    Collections.sort(sortedResults);
  }

  private void countOccurences(String enteredText) throws SQLException {
    searchCount = statement.executeQuery(selectDatabase(enteredText,true));
    System.out.println("Search count done.");
    if (searchCount != null && searchCount.next()) {
      System.out.println(searchCount.getString(1));
      AutoSuggestion.displayCount(searchCount.getString(1));
    }
  }

  private String selectDatabase(String enteredText,boolean count) {
    boolean https = false;
    boolean http = false;
    char firstLetter;
    if (enteredText.startsWith("https://")) {
      https = true;
    } else if (enteredText.startsWith("http://")) {
      http = true;
    }
    if (https){
      if(enteredText.startsWith("www.",8)){
        firstLetter = enteredText.charAt(12);
        String query;
        if (count){
          query = "SELECT COUNT(*) FROM "+HTTPS_WWW_DATABASE+"-"+firstLetter+" WHERE url LIKE '"+enteredText+"%' ";
        } else {
          query = "SELECT * FROM "+HTTPS_WWW_DATABASE+"-"+firstLetter+" WHERE url LIKE '"+enteredText+"%' ";
        }
        return query;
      } else {
        firstLetter = enteredText.charAt(8);
        String query;
        if (count) {
          query = "SELECT COUNT(*) FROM "+HTTPS_DATABASE+"-"+firstLetter+" WHERE url LIKE '"+enteredText+"%' ";
        } else {
          query = "SELECT * FROM "+HTTPS_DATABASE+"-"+firstLetter+" WHERE url LIKE '"+enteredText+"%' ";
        }
        return query;
      }
    } else if (http){
      if(enteredText.startsWith("www.",7)){
        firstLetter = enteredText.charAt(11);
        String query;
        if (count){
          query = "SELECT COUNT(*) FROM "+HTTP_WWW_DATABASE+"-"+firstLetter+" WHERE url LIKE '"+enteredText+"%' ";
        } else {
          query = "SELECT * FROM "+HTTP_WWW_DATABASE+"-"+firstLetter+" WHERE url LIKE '"+enteredText+"%' ";
        }
        return query;
      } else {
        firstLetter = enteredText.charAt(7);
        String query;
        if(count){
          query = "SELECT COUNT(*) FROM "+HTTP_DATABASE+"-"+firstLetter+" WHERE url LIKE '"+enteredText+"%' ";
        } else {
          query = "SELECT * FROM "+HTTP_DATABASE+"-"+firstLetter+" WHERE url LIKE '"+enteredText+"%' ";
        }
        return query;
      }
    } else {
      String query;
      if (count){
        query = "SELECT COUNT(*) FROM "+REST_DATABASE+" WHERE url LIKE '"+enteredText+"%' ";
      } else {
        query = "SELECT * FROM "+REST_DATABASE+" WHERE url LIKE '"+enteredText+"%' ";
      }
      return query;
    }

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
