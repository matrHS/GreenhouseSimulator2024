package no.ntnu.gui.greenhouse;

import java.util.Objects;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import no.ntnu.tools.Logger;

/**
 * Run a greenhouse simulation with a graphical user interface (GUI), with JavaFX.
 */
public class MainGui {

  Tab mainTab; // The tab pane for the app
  private MainGuiController controller; // the controller for the main page

  /**
   * Create a new main GUI.
   *
   * @param controller The controller for the main page
   */
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
  private Tab setMainPage() {
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
  private HBox createCenterPane() {
    HBox centerPane = new HBox();
    try {
      Image greenhouseDrawing = new Image(
          Objects.requireNonNull(getClass().getResource(
              "/images/Greenhouse2.jpg")).toExternalForm());
      ImageView mainImage = new ImageView(greenhouseDrawing);

      mainImage.getStyleClass().add("main-image");
      GridPane.setHalignment(mainImage, HPos.CENTER);
      centerPane.getChildren().add(mainImage);

    } catch (Exception e) {
      System.out.println("error:" + e.getMessage());
    }

    return centerPane;

  }

  /**
   * Get the main tab for the application.
   *
   * @return The main tab for the application
   */
  public Tab getMainTab() {
    return this.mainTab;
  }
}
