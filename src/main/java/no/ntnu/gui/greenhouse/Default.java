package no.ntnu.gui.greenhouse;

import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

/**
 * A class that contains default values for the GUI.
 */
public class Default {

  /**
   * Set the header of the GUI.
   *
   * @param controller The controller of the main GUI.
   * @return The header of the GUI.
   * @throws Exception If the banner could not be found.
   */
  public static VBox setHeader(MainGuiController controller) throws Exception {
    Image banner = null;
    VBox topPane = new VBox();
    VBox bannerPane = new VBox();

    try {
      banner = new Image(String.valueOf(Default.class.getResource("/images/header.png")));
      ImageView bannerView = new ImageView(banner);
      bannerPane.getChildren().addAll(bannerView);

    } catch (NullPointerException e) {
      throw new NullPointerException("Could not find the banner");
    }
    bannerPane.getStyleClass().add("header");
    topPane.getChildren().addAll(bannerPane);
    return topPane;
  }

  /**
   * Create a default scene.
   *
   * @param root The root of the scene.
   * @return The default scene.
   */
  public static Scene defaultScene(Parent root) {
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    Scene scene = new Scene(root, bounds.getMinX(), bounds.getMinY());
    return scene;
  }
}
