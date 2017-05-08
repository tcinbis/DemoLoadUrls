package GUI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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
          searchCount = statement.executeQuery(
              "SELECT COUNT(url) FROM urls1 WHERE url LIKE '" + enteredText.toLowerCase() + "%'");
          System.out.println("Search count done.");
          if (searchCount != null && searchCount.next()) {
            System.out.println(searchCount.getString(1));
            AutoSuggestion.displayCount(searchCount.getString(1));
          }
          searchResults = statement.executeQuery(
              " SELECT url FROM urls1 WHERE url LIKE '" + enteredText.toLowerCase() + "%' LIMIT 5");
          System.out.println("Search results loaded.");
          entries.clear();

          while (searchResults != null && searchResults.next()) {
            entries.add(searchResults.getString(1));
            System.out.println(searchResults.getString(1));
          }
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
