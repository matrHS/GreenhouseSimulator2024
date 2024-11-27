package no.ntnu.gui.common;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import no.ntnu.greenhouse.Camera;
import no.ntnu.greenhouse.Sensor;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.tools.Logger;

public class CameraPane extends TitledPane {
  private final List<SimpleStringProperty> cameras = new ArrayList<>();
  private final VBox contentBox = new VBox();

  public CameraPane(List<Camera> cameras){
    initialize(cameras.stream().map(Camera::getCamera).toList());
  }
  public CameraPane(){
    initialize(new LinkedList<>());
  }
  private void initialize(Iterable<Camera> cameras) {
    setText("Cameras");
    cameras.forEach(camera ->
                        contentBox.getChildren().add(createImageBox(camera))
    );
    setContent(contentBox);
  }

  /**
   * Update the GUI according to the changes in camera data.
   *
   * @param cameras The camera data that has been updated
   */
  public void update(Iterable<Camera> cameras) {
    for (Camera camera : cameras) {
      createImageBox(camera);
    }
  }

  /**
   * Update the GUI according to the changes in sensor data.
   * Wrapper for the other method with SensorReading-iterable parameter
   *
   * @param cameras The sensor data that has been updated
   */
  public void update(List<Camera> cameras) {
    update(cameras.stream().map(Camera::getCamera).toList());
  }

  private Node createImageBox(Camera camera){
    String name = "Camera " + camera.getId();
    String cameraImage = camera.getImage();
    ImageView imageView = new ImageView(String.valueOf(cameraImage));
    BorderPane box = new BorderPane();
    box.setTop(new Text(name));
    box.setCenter(imageView);
    return box;
  }
}
