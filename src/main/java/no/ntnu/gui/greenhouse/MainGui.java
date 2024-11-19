package no.ntnu.gui.greenhouse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.tools.Logger;

/**
 * Run a greenhouse simulation with a graphical user interface (GUI), with JavaFX.
 */
public class MainGui {

  private MainGuiController controller; // the controller for the main page
  Tab mainTab; // The tab pane for the app

  public MainGui(MainGuiController controller) {
    this.controller = controller;
    try {
      this.mainTab = setMainPage();
    } catch (Exception e) {
      Logger.error("Error in MainGui: " + e.getMessage());
    }

  }
  /**
   * Create the main scene for the application.
   *
   * @return The main scene for the application
   */
  private Tab setMainPage(){
    BorderPane mainScene = new BorderPane();

    mainScene.getStyleClass().add("main-center-pane");

    mainScene.setCenter(createCenterPane());
    mainScene.setRight(new HBox());
    return new Tab("Home Page", mainScene);
  }

  /**
   * Create the center pane for the application.
   *
   * @return The center pane for the application
   */
  private GridPane createCenterPane(){
    GridPane centerPane = new GridPane();
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    centerPane.getColumnConstraints().add(new ColumnConstraints(bounds.getWidth()/2));
    centerPane.getColumnConstraints().add(new ColumnConstraints(bounds.getWidth()/2));

    try {
      Image greenhouseDrawing = new Image(
          Objects.requireNonNull(getClass().getResource(
              "/images/Greenhouse2.jpg")).toExternalForm());
      ImageView mainImage = new ImageView(greenhouseDrawing);

      mainImage.getStyleClass().add("main-image");
      GridPane.setHalignment(mainImage, HPos.CENTER);
      centerPane.add(mainImage, 0,0);

    } catch(Exception e){
      System.out.println("error:" + e.getMessage());
    }

    HBox textBox = new HBox();
    Text description = getDescription();
    textBox.getChildren().add(description);
    textBox.getStyleClass().add("text-box-main");

    GridPane.setHalignment(textBox, HPos.CENTER);
    centerPane.add(textBox, 1,0);

    return centerPane;

  }

  /**
   * Get the description box for the main page of the application.
   *
   * @return The description box for the main page of the application
   */
  private static Text getDescription() {
    Text description = new Text();
    description.setText("Your task in this project is to implement a meaningful " +
                        "application and its components for a" +
                        "complete smart farming solution. The solution includes " +
                        "communication with sockets. You need to" +
                        "design your application-layer communication protocol and implement" +
                        " necessary communication between the nodes so that the " +
                        "control-panel nodes can visualize sensor data and control " +
                        "the actuators on the sensor nodes.");
    description.setFont(new Font(16));
    description.setWrappingWidth(480);
    description.setTextAlignment(TextAlignment.CENTER);
    description.setX(100);
    return description;
  }


  public Tab getMainTab() {
    return this.mainTab;
  }
}
