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

class AutocompletionTextField extends TextField {

  //Local variables
  //entries to autocomplete
  //private final SortedSet<String> entries;
  private final ArrayList<String> entries;
  //popup GUI
  private ContextMenu entriesPopup;

  private static final String DATABASEURL = "jdbc:postgresql://localhost:8090/urldatabase";
  private Connection connection;
  private Statement statement;
  private ResultSet searchCount;

  //The names of the splitted Databases
  private final String HTTP_DATABASE = "urls-http";
  private final String HTTPS_DATABASE = "urls-https";
  private final String HTTP_WWW_DATABASE = "urls-www-http";
  private final String HTTPS_WWW_DATABASE = "urls-www-https";
  private final String REST_DATABASE = "urls-rest";
  private final ArrayList<String> countvalues = new ArrayList<>();


  /**
   * A Texfield, which listens to input and loads autocompletion suggestions from the databases
   */
  public AutocompletionTextField() {
    super();
    //all precounted inputs, stored into a ArrayList
    countvalues.add("h");
    countvalues.add("ht");
    countvalues.add("htt");
    countvalues.add("http");
    countvalues.add("http:");
    countvalues.add("http:/");
    countvalues.add("http://");
    countvalues.add("https");
    countvalues.add("https:");
    countvalues.add("https:/");
    countvalues.add("https://");
    countvalues.add("http://w");
    countvalues.add("http://ww");
    countvalues.add("http://www");
    countvalues.add("https://w");
    countvalues.add("https://ww");
    countvalues.add("https://www");
    this.entries = new ArrayList<>();
    this.entriesPopup = new ContextMenu();
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
   * "Suggestion" specific listeners
   */
  private void setListner() {
    textProperty().set("http://");
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
          countOccurrences(enteredText);
          findOccurrences(enteredText);
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
   * Method that scans the entered text and compares this String to our smaller databases http and https
   * both with a www-version and a normal version.
   * If the right database is found the method now checks the additional letters and counts the occurances
   * of the entered text.
   * This will be displayed in the end in our GUI.
   *
   * @param enteredText the user input from the textfield
   */
  private void countOccurrences(String enteredText) throws SQLException {
    searchCount = null;
    if (countvalues.contains(enteredText)) { //the current search was already preprocessed
      int place = countvalues.indexOf(enteredText);
      //Select the stored value in the database
      searchCount = statement.executeQuery("SELECT * FROM counts LIMIT 1 OFFSET "+place);
    }
    if (searchCount != null && searchCount.next()) { //There is a value, display it
      AutoSuggestion.displayCount(searchCount.getString(1));
    } else { //This search wasn't already preprocessed
      ArrayList<String> databases = selectMatchingDatabases(enteredText);
      Character letter = getLetter(enteredText);
      boolean letterischar = false;
      if (letter != null) {
        letterischar = letter.charValue() >= (int) 'a' && letter.charValue()
            <= (int) 'z'; // Check if the letter is actually a char, that means is useful, before selecting a database with it
      }
      if (databases.size() > 1) { //Error check for the programmers, shouldn't be executed
        System.err.println("Not all double database cases caught !");
        System.err.println(Arrays.toString(databases.toArray()));
      }
      switch (databases.get(0)) { // Determine which database is the correct one
        case HTTP_DATABASE:
          if (letterischar) { // Select the Database : "http://'letter' ...
            searchCount = statement.executeQuery(
                "SELECT COUNT(*) FROM \"" + HTTP_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%'");
          } else { // Select the Database : "http:// 'non-letter'...
            searchCount = statement.executeQuery(
                "SELECT COUNT(*) FROM \"" + HTTP_DATABASE + "\" WHERE url LIKE '"
                    + enteredText
                    + "%'");
          }
          break;
        case HTTP_WWW_DATABASE:
          if (letterischar) {// Select the Database : "http://www.'letter' ...
            searchCount = statement.executeQuery(
                "SELECT COUNT(*) FROM \"" + HTTP_WWW_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%'");
          } else {// Select the Database : "http:// www.'non-letter'...
            searchCount = statement.executeQuery(
                "SELECT COUNT(*) FROM \"" + HTTP_DATABASE + "\" WHERE url LIKE '"
                    + enteredText
                    + "%'");
          }
          break;
        case HTTPS_DATABASE:
          if (letterischar) {// Select the Database : "https://'letter' ...
            searchCount = statement.executeQuery(
                "SELECT COUNT(*) FROM \"" + HTTPS_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%'");
          } else {// Select the Database : "https://'non-letter'...
            searchCount = statement.executeQuery(
                "SELECT COUNT(*) FROM \"" + HTTPS_DATABASE + "\" WHERE url LIKE '"
                    + enteredText
                    + "%'");
          }
          break;
        case HTTPS_WWW_DATABASE:
          if (letterischar) {// Select the Database : "https://www.'letter' ...
            searchCount = statement.executeQuery(
                "SELECT COUNT(*) FROM \"" + HTTPS_WWW_DATABASE + "-" + letter
                    + "\" WHERE url LIKE '"
                    + enteredText
                    + "%'");
          } else {
            searchCount = statement
                .executeQuery(// Select the Database : "https://www.'non-letter'...
                    "SELECT COUNT(*) FROM \"" + HTTPS_DATABASE + "\" WHERE url LIKE '"
                        + enteredText
                        + "%'");
          }
          break;
        case REST_DATABASE: //Select the Database with all other entrys
          searchCount = statement.executeQuery(
              "SELECT COUNT(*) FROM \"" + REST_DATABASE + "\" WHERE url LIKE '"
                  + enteredText
                  + "%'");
          break;
      }
    }
    System.out.println("Search count done.");
    if (searchCount != null && searchCount.next()) {
      System.out.println(searchCount.getString(1));
      AutoSuggestion.displayCount(searchCount.getString(1)); //Display the result
    }
  }

  /**
   * Method that scans the entered text and compares this String to our smaller databases http and https
   * both with a www-version and a normal version.
   * If the right database is found the method now checks the additional letters and gets 5 suggestions randomly
   * which will be added to our entries array list.
   * @param enteredText The text entered in the GUI-TextField
   */
  private void findOccurrences(String enteredText) throws SQLException {
    ArrayList<String> databases = selectMatchingDatabases(
        enteredText); // The databases which contain possible solutions for the input
    ArrayList<String> lookUpDatabases = new ArrayList<>();
    lookUpDatabases.addAll(databases); // the databases to look into for matching results
    ArrayList<String> result = new ArrayList<>(); //Storage for the results
    Character letter = getLetter(
        enteredText); // The letter to determine which database is to be used
    boolean letterIsChar = false;
    if (letter != null) {
      letterIsChar = letter.charValue() >= (int) 'a'
          && letter.charValue() <= (int) 'z'; // Check weather the important char is a letter
    }
    int loops = 0;
    boolean notrandom = false;
    while (result.size() < 5 && loops < databases
        .size()) { // Searches for results until 5 are found or the databases contain less matching entries
      Random ran = new Random();
      int r = ran.nextInt(lookUpDatabases.size());
      int letterint = ran.nextInt(26);
      int offset = 0;
      if (!notrandom) {
        offset = ran.nextInt(200);
      }
      char c = 'a';
      if (!notrandom) {
        c = (char) (ran.nextInt(26)
            + 'a'); // Random character to determine a database to look for occurrences if the string is not long enough to match only one database
      }
      ResultSet resultSet = null; //Storage for the results

      switch (lookUpDatabases.get(r)) { // switches over the current databases
        case HTTP_DATABASE:
          if (letter == null) {
            boolean accepted4 = false;
            if (letterint == 0) { //Select the Database : "http://..."
              resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTP_DATABASE + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
              accepted4 = resultSet.isBeforeFirst();
            }
            if (!accepted4) {
              while (resultSet == null || !resultSet.isBeforeFirst() && c <= 'z') {
                resultSet = statement.executeQuery(
                    "SELECT * FROM \"" + HTTP_DATABASE + "-" + c + "\" WHERE url LIKE '"
                        + enteredText
                        + "%' LIMIT 1 OFFSET " + offset);
                c++;
              }
            }
          } else if (letterIsChar) { //Select the Database : "http://'letter' ..."
            resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTP_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          } else {  //Select the Database : "http://...
            resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTP_DATABASE + "\" WHERE url LIKE '" + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          }
          break;
        case HTTP_WWW_DATABASE:
          boolean accepted = false;
          if (letter == null) { //Select the Database : "http://www..."
            if (letterint == 0 || enteredText.length() <= "http://www.".length()) {
              resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTP_DATABASE + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
              accepted = resultSet.isBeforeFirst();
            }
            if (!accepted) {
              while (resultSet == null || !resultSet.isBeforeFirst() && c <= 'z') {
                resultSet = statement.executeQuery(
                    "SELECT * FROM \"" + HTTP_WWW_DATABASE + "-" + c + "\" WHERE url LIKE '"
                        + enteredText
                        + "%' LIMIT 1 OFFSET " + offset);
                c++;
              }
            }
          } else if (letterIsChar) { //Select the Database : "http://www.'letter'..."
            resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTP_WWW_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          } else { //Select the Database : "http://www..."
            resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTP_DATABASE + "\" WHERE url LIKE '" + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          }
          break;
        case HTTPS_DATABASE:
          boolean accepted2 = false;
          if (letter == null) { //Select the Database : "https://..."
            if (letterint == 0) {
              resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTPS_DATABASE + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
              accepted2 = resultSet.isBeforeFirst();
            }
            if (!accepted2) {
              while (resultSet == null || !resultSet.isBeforeFirst() && c <= 'z') {
                resultSet = statement.executeQuery(
                    "SELECT * FROM \"" + HTTPS_DATABASE + "-" + c + "\" WHERE url LIKE '"
                        + enteredText
                        + "%' LIMIT 1 OFFSET " + offset);
                c++;
              }
            }
          } else if (letterIsChar) { //Select the Database : "https://www.'letter'..."
            resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTPS_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          } else {
            resultSet = statement.executeQuery( //Select the Database : "https://..."
                "SELECT * FROM \"" + HTTPS_DATABASE + "\" WHERE url LIKE '" + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          }
          break;
        case HTTPS_WWW_DATABASE:
          boolean accepted3 = false;
          if (letter == null) { //Select the Database : "https://www..."
            if (letterint == 0 || enteredText.length() <= "https://www.".length()) {
              resultSet = statement.executeQuery(
                  "SELECT * FROM \"" + HTTPS_DATABASE + "\" WHERE url LIKE '" + enteredText
                      + "%' LIMIT 1 OFFSET " + offset);
              accepted3 = resultSet.isBeforeFirst();
            }
            if (!accepted3) {
              while (resultSet == null || !resultSet.isBeforeFirst() && c <= 'z') {
                resultSet = statement.executeQuery(
                    "SELECT * FROM \"" + HTTPS_WWW_DATABASE + "-" + c + "\" WHERE url LIKE '"
                        + enteredText
                        + "%' LIMIT 1 OFFSET " + offset);
                c++;
              }
            }
          } else if (letterIsChar) { //Select the Database : "https://www.'letter'..."
            resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTPS_WWW_DATABASE + "-" + letter + "\" WHERE url LIKE '"
                    + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          } else { //Select the Database : "https://www..."
            resultSet = statement.executeQuery(
                "SELECT * FROM \"" + HTTPS_DATABASE + "\" WHERE url LIKE '" + enteredText
                    + "%' LIMIT 1 OFFSET " + offset);
          }
          break;
        case REST_DATABASE: //Select the Database with all non specified urls.
          resultSet = statement.executeQuery(
              "SELECT * FROM \"" + REST_DATABASE + "\" WHERE url LIKE '" + enteredText
                  + "%' LIMIT 1 OFFSET " + offset);
          break;
      }

      if (notrandom) {
        loops++;
      }
      boolean hasresult = false;
      while (resultSet.next()) {
        result.add(resultSet.getString(1));
        hasresult = true;
      }
      if (!hasresult) {
        lookUpDatabases.remove(r);
      }
      if (lookUpDatabases.size() == 0) {
        notrandom = true;
        lookUpDatabases.addAll(databases);
      }
    }
    System.out.println("Search results loaded.");

    entries.clear();

    Collections.sort(result);
    entries.addAll(result);
  }

  /**
   * Method to get the letter after the prefixes (https://,http://,http://www.,https://www.)
   * if there is one or to set it to null if there is no character after it.
   * @param enteredText The entered text of the user
   * @return the letter after the prefix
   */
  private Character getLetter(String enteredText) {
    Character letter = null;
    if (enteredText.startsWith("http://")) {
      if (enteredText.length() > 7) {
        letter = enteredText.charAt(7);
      }
    }
    if (enteredText.startsWith("http://www.")) {
      if (enteredText.length() > 11) {
        letter = enteredText.charAt(11);
      } else {
        letter = null;
      }
    }
    if (enteredText.startsWith("https://")) {
      if (enteredText.length() > 8) {
        letter = enteredText.charAt(8);
      }
    }
    if (enteredText.startsWith("https://www.")) {
      if (enteredText.length() > 12) {
        letter = enteredText.charAt(12);
      } else {
        letter = null;
      }
    }
    return letter;
  }

  /**
   * Depending on the input of the user the matching databases will be determined
   * @param enteredText The text the user enters in the GUI
   * @return ArrayList of Strings consisting of the matching databases
   * @throws SQLException
   */
  private ArrayList<String> selectMatchingDatabases(String enteredText) throws SQLException {
    String Shttps = "https://";
    String Shttpswww = "https://www.";
    String Shttp = "http://";
    String Shttpwww = "http://www.";
    ArrayList<String> resultlist = new ArrayList<>();
    if (enteredText.regionMatches(0, Shttps, 0, Math.min(enteredText.length(), Shttps.length()))
        && !enteredText.startsWith(Shttpswww)) {
      resultlist.add(HTTPS_DATABASE);
    }
    if (enteredText.regionMatches(0, Shttp, 0, Math.min(enteredText.length(), Shttp.length()))
        && !enteredText.startsWith(Shttpwww)) {
      resultlist.add(HTTP_DATABASE);
    }
    if (enteredText
        .regionMatches(0, Shttpswww, 0, Math.min(enteredText.length(), Shttpswww.length()))) {
      resultlist.add(HTTPS_WWW_DATABASE);
    }
    if (enteredText
        .regionMatches(0, Shttpwww, 0, Math.min(enteredText.length(), Shttpwww.length()))) {
      resultlist.add(HTTP_WWW_DATABASE);
    }
    if (enteredText.length() < Shttp.length() || !(enteredText.startsWith(Shttp) || enteredText
        .startsWith(Shttps))) {
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