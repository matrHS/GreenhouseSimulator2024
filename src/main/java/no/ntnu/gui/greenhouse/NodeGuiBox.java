package no.ntnu.gui.greenhouse;

import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.Sensor;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.gui.common.ActuatorPane;
import no.ntnu.gui.common.SensorPane;
import no.ntnu.listeners.common.ActuatorListener;
import no.ntnu.listeners.greenhouse.SensorListener;

/**
 * A class that creates a GUI window for a node.
 */
public class NodeGuiBox implements SensorListener, ActuatorListener {

  private final SensorActuatorNode node;
  private ActuatorPane actuatorPane;
  private SensorPane sensorPane;

  /**
   * Create a GUI window for a specific node.
   *
   * @param node The node which will be handled in this window
   */
  public NodeGuiBox(SensorActuatorNode node) {
    this.node = node;
    initializeListeners(node);
  }

  /**
   * Get the node box.
   *
   * @return The node box
   */
  public HBox getNodeBox() {
    HBox nodeBox = new HBox();
    actuatorPane = new ActuatorPane(node.getActuators());
    sensorPane = new SensorPane(node.getSensors(), "Sensors");
    nodeBox.getChildren().add(createContent(actuatorPane, sensorPane));
    return nodeBox;
  }

  /**
   * Initialize listeners for the node.
   *
   * @param node The node to initialize listeners for
   */
  private void initializeListeners(SensorActuatorNode node) {
    node.addSensorListener(this);
    node.addActuatorListener(this);
  }

  /**
   * Set the actuator pane.
   *
   * @param actuatorPane The actuator pane
   */
  public void setActuatorPane(ActuatorPane actuatorPane) {
    this.actuatorPane = actuatorPane;
  }

  /**
   * Set the sensor pane.
   *
   * @param sensorPane The sensor pane
   */
  public void setSensorPane(SensorPane sensorPane) {
    this.sensorPane = sensorPane;
  }

  /**
   * Shut down the node.
   */
  private void shutDownNode() {
    node.stop();
  }

  /**
   * Create the content for the node box.
   *
   * @param actuatorPane The actuator pane
   * @param sensorPane The sensor pane
   * @return The node box
   */
  public VBox createContent(ActuatorPane actuatorPane, SensorPane sensorPane) {
    VBox nodeBox = new VBox(sensorPane, actuatorPane);
    nodeBox.setMinWidth(300);
    nodeBox.setMaxHeight(600);
    nodeBox.setAlignment(Pos.CENTER);
    return nodeBox;
  }

  /**
   * Update the sensors in the sensor pane.
   *
   * @param sensors A list of sensors having new values (readings)
   */
  @Override
  public void sensorsUpdated(List<Sensor> sensors) {
    if (sensorPane != null) {
      sensorPane.update(sensors);
    }
  }

  /**
   * Update the actuator in the actuator pane.
   *
   * @param nodeId   ID of the node on which this actuator is placed
   * @param actuator The actuator that has changed its state
   */
  @Override
  public void actuatorUpdated(int nodeId, Actuator actuator) {
    if (actuatorPane != null) {
      actuatorPane.update(actuator);
    }
  }
}
