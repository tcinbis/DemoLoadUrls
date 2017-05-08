package GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Max
 */
public class AutoSuggestion extends Application {

  static Label entryCount;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    AutocompletionTextField actf = new AutocompletionTextField();
    actf.setMaxWidth(400.0);
    BorderPane root = new BorderPane();
    root.setTop(actf);
    primaryStage.setResizable(false);
    primaryStage.setScene(new Scene(root, 600, 400));
    primaryStage.setTitle("Hackaton");
    actf.getEntries().addAll(actf.getEntries());
    entryCount = new Label();
    entryCount.setAlignment(Pos.CENTER);
    root.setBottom(entryCount);
    primaryStage.show();
  }

  @Override
  public void init() {

  }

  public static void displayCount(String string) {
    entryCount.setText("Treffer: " + string);
  }
}
