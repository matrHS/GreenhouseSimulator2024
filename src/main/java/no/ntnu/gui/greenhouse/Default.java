package no.ntnu.gui.greenhouse;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

public class Default {

  public static VBox setHeader(MainGuiController controller) throws Exception{
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

  public static Scene defaultScene(Parent root){
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    Scene scene = new Scene(root, bounds.getMinX(), bounds.getMinY());
    return  scene;
  }
}
