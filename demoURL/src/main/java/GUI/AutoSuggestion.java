package GUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
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
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    StackPane root = new StackPane();
    AutocompletionTextField actf = new AutocompletionTextField();
    actf.setMaxWidth(400.0);
    HBox hBox = new HBox(2);
    hBox.getChildren().add(actf);
    primaryStage.setResizable(false);
    primaryStage.setScene(new Scene(root, 600, 400));
    primaryStage.setTitle("Hackaton");
    actf.getEntries().addAll(actf.getEntries());
    entryCount = new Label();
    hBox.getChildren().add(entryCount);
    root.getChildren().add(hBox);
    primaryStage.show();
  }

  @Override
  public void init() {

  }

  public static void displayCount(String string) {
    entryCount.setText("Treffer :" + string);
  }
}
