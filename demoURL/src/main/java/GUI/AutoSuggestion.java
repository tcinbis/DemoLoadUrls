package GUI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Max
 */
public class AutoSuggestion extends Application {

  private static Label entryCount;

  /**
   * Init the JavaFX application.
   *
   * @param args default arguments needed for starting.
   */
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * If the search was successful, the number of entries found are displayed.
   *
   * @param string is the number of found entries
   */
  static void displayCount(String string) {
    entryCount.setText("Entries found: " + string);
  }

  /**
   * Start the JavaFX Application by displaying a window with two elements.
   * One of it is basically special Textfield, the other is a simple label.
   *
   * @param primaryStage default stage.
   */
  @Override
  public void start(Stage primaryStage) {
    //Basic window properties
    primaryStage.setResizable(false);
    primaryStage.setTitle("Hackaton");
    //Init default pane
    BorderPane root = new BorderPane();
    //Window dimensions with default pane
    primaryStage.setScene(new Scene(root, 600, 400));
    //Init of a modified version of a TextField which can display five found entries as suggestions below it
    AutocompletionTextField actf = new AutocompletionTextField();
    actf.setMaxWidth(400.0);
    //Add (if found) five possible entries as suggestions
    actf.getEntries().addAll(actf.getEntries());
    //Init the entryCount label
    entryCount = new Label();
    //Add the two elements to the default pane
    root.setTop(actf);
    root.setBottom(entryCount);
    //Display the beauty
    primaryStage.show();
  }
}
