package no.ntnu.gui.common;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import no.ntnu.greenhouse.Camera;
import no.ntnu.greenhouse.SensorReading;


public class CameraPane extends TitledPane {
  private final List<SimpleStringProperty> cameras = new ArrayList<>();
  private final VBox contentBox = new VBox();


  public CameraPane(List<Camera> cameras){
    super();
    initialize(cameras.stream().map(Camera::getImage).toList());
  }
  public CameraPane(){
    initialize(new LinkedList<>());
  }

  private void initialize(Iterable<String> images) {
    setText("Cameras");
    images.forEach(image ->
                       Platform.runLater(() -> contentBox.getChildren().add(createImageBox(image)))
    );
    contentBox.setAlignment(Pos.TOP_LEFT);
    contentBox.getStyleClass().add("camera-scroll-pane");
    setContent(contentBox);
  }

  /**
   * Update the GUI according to the changes in camera data.
   *
   * @param images The camera data that has been updated
   */
  public void update(Iterable<String> images) {
    Platform.runLater(() -> contentBox.getChildren().clear());
    for (String image : images) {
      Platform.runLater(() -> contentBox.getChildren().add(createImageBox(image)));
    }
  }

  /**
   * Update the GUI according to the changes in sensor data.
   * Wrapper for the other method with SensorReading-iterable parameter
   *
   * @param cameras The sensor data that has been updated
   */
  public void update(List<Camera> cameras) {
    update(cameras.stream().map(Camera::getImage).toList());
  }

  private Node createImageBox(String image){
    byte[] imageBytes = Base64.getDecoder().decode(image);
    Image img = new Image(new ByteArrayInputStream(imageBytes));
    ImageView imageView = new ImageView(img);
    imageView.getStyleClass().add("camera-view");

    HBox hbox =new HBox(imageView);
    hbox.setAlignment(Pos.TOP_LEFT);
    return hbox;
  }
}
