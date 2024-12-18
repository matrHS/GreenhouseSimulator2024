package no.ntnu.gui.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import no.ntnu.greenhouse.Sensor;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.tools.loggers.Logger;

/**
 * A section of GUI displaying sensor data.
 */
public class SensorPane extends TitledPane {
  private final List<SimpleStringProperty> sensorProps = new ArrayList<>();
  private final VBox contentBox = new VBox();

  /**
   * Create a sensor pane.
   *
   * @param sensors The sensor data to be displayed on the pane.
   */
  public SensorPane(Iterable<SensorReading> sensors, String title) {
    super();
    initialize(sensors, title);
  }

  /**
   * Create an empty sensor pane, without any data.
   */
  public SensorPane(String title) {
    initialize(new LinkedList<>(), title);
  }

  /**
   * Create a sensor pane.
   * Wrapper for the other constructor with SensorReading-iterable parameter
   *
   * @param sensors The sensor data to be displayed on the pane.
   */
  public SensorPane(List<Sensor> sensors, String title) {
    initialize(sensors.stream().map(Sensor::getReading).toList(), title);
  }

  /**
   * Initialize the sensor pane.
   *
   * @param sensors The sensor data to display.
   * @param title   The title of the pane
   */
  private void initialize(Iterable<SensorReading> sensors, String title) {
    setText(title);
    sensors.forEach(sensor ->
        contentBox.getChildren().add(createAndRememberSensorLabel(sensor))
    );
    contentBox.getStyleClass().add("sensor-pane");
    setContent(contentBox);
  }

  /**
   * Update the GUI according to the changes in sensor data.
   *
   * @param sensors The sensor data that has been updated
   */
  public void update(Iterable<SensorReading> sensors) {
    int index = 0;
    for (SensorReading sensor : sensors) {
      updateSensorLabel(sensor, index++);
    }
  }

  /**
   * Update the GUI according to the changes in sensor data.
   * Wrapper for the other method with SensorReading-iterable parameter
   *
   * @param sensors The sensor data that has been updated
   */
  public void update(List<Sensor> sensors) {
    update(sensors.stream().map(Sensor::getReading).toList());
  }

  /**
   * Create a sensor label for a sensor and remember it for later updates.
   *
   * @param sensor The sensor to create a label for
   * @return The label created
   */
  private Label createAndRememberSensorLabel(SensorReading sensor) {
    SimpleStringProperty props = new SimpleStringProperty(generateSensorText(sensor));
    sensorProps.add(props);
    Label label = new Label();
    label.textProperty().bind(props);
    return label;
  }

  /**
   * Generate the text to display for a sensor.
   *
   * @param sensor The sensor to generate the text for
   * @return The text to display for the sensor
   */
  private String generateSensorText(SensorReading sensor) {
    String sensorType = sensor.getType();
    sensorType = sensorType.substring(0, 1).toUpperCase() + sensorType.substring(1);
    return sensorType + ": " + sensor.getFormatted();
  }

  /**
   * Update a sensor label with new sensor data.
   *
   * @param sensor The new sensor data
   * @param index  The index of the sensor in the list
   */
  private void updateSensorLabel(SensorReading sensor, int index) {
    if (sensorProps.size() > index) {
      SimpleStringProperty props = sensorProps.get(index);
      Platform.runLater(() -> props.set(generateSensorText(sensor)));
    } else {
      Logger.info("Adding sensor[" + index + "]");
      Platform.runLater(() -> contentBox.getChildren().add(createAndRememberSensorLabel(sensor)));
    }
  }
}
