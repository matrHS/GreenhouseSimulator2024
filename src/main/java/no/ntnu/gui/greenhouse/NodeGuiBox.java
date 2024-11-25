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

public class NodeGuiBox implements SensorListener, ActuatorListener{

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

    public HBox getNodeBox(){
      HBox nodeBox= new HBox();
      actuatorPane = new ActuatorPane(node.getActuators());
      sensorPane = new SensorPane(node.getSensors());
      nodeBox.getChildren().add(createContent(actuatorPane, sensorPane));
      return nodeBox;
    }


    private void initializeListeners(SensorActuatorNode node) {
      node.addSensorListener(this);
      node.addActuatorListener(this);
    }

  public void setActuatorPane(ActuatorPane actuatorPane) {
    this.actuatorPane = actuatorPane;
  }

  public void setSensorPane(SensorPane sensorPane) {
    this.sensorPane = sensorPane;
  }

  private void shutDownNode() {
      node.stop();
    }

    public VBox createContent(ActuatorPane actuatorPane, SensorPane sensorPane) {
      VBox nodeBox = new VBox(sensorPane, actuatorPane);
      nodeBox.setMinWidth(300);
      nodeBox.setMaxHeight(600);
      nodeBox.setAlignment(Pos.CENTER);
      return nodeBox;
    }

    @Override
    public void sensorsUpdated(List<Sensor> sensors) {
      if (sensorPane != null) {
        sensorPane.update(sensors);
      }
    }

    @Override
    public void actuatorUpdated(int nodeId, Actuator actuator) {
      if (actuatorPane != null) {
        actuatorPane.update(actuator);
      }
    }
}
