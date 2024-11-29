package no.ntnu.gui.common;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.ActuatorCollection;
import no.ntnu.listeners.controlpanel.ActuatorChangedListener;

/**
 * A section of the GUI representing a list of actuators. Can be used both on the sensor/actuator
 * node, and on a control panel node.
 */
public class ActuatorPane extends TitledPane {
  private final Map<Actuator, SimpleStringProperty> actuatorValue = new HashMap<>();
  private final Map<Actuator, SimpleBooleanProperty> actuatorActive = new HashMap<>();
  ActuatorChangedListener listener;

  /**
   * Create an actuator pane.
   *
   * @param actuators A list of actuators to display in the pane.
   */
  public ActuatorPane(ActuatorCollection actuators) {
    super();
    setText("Actuators");
    VBox vbox = new VBox();
    vbox.setSpacing(10);
    vbox.getStyleClass().add("actuator-pane");
    setContent(vbox);
    addActuatorControls(actuators, vbox);
  }

  /**
   * Create an actuator pane.
   *
   * @param actuators A list of actuators to display in the pane.
   * @param listener  A listener to be notified when an actuator is changed.
   */
  public ActuatorPane(ActuatorCollection actuators, ActuatorChangedListener listener) {
    this(actuators);
    this.listener = listener;
  }

  /**
   * Add the actuators to the GUI.
   *
   * @param actuators The actuators to add.
   * @param parent    The parent pane to add the actuators to.
   */
  private void addActuatorControls(ActuatorCollection actuators, Pane parent) {
    actuators.forEach(actuator ->
        parent.getChildren().add(createActuatorGui(actuator))
    );
  }

  /**
   * Create the GUI for an actuator.
   *
   * @param actuator The actuator to create the GUI for.
   * @return The GUI for the actuator.
   */
  private Node createActuatorGui(Actuator actuator) {
    HBox actuatorGui = new HBox(createActuatorLabel(actuator), createActuatorCheckbox(actuator));
    actuatorGui.setSpacing(5);
    return actuatorGui;
  }

  /**
   * Create a checkbox for an actuator.
   *
   * @param actuator The actuator to create the checkbox for.
   * @return The checkbox for the actuator.
   */
  private CheckBox createActuatorCheckbox(Actuator actuator) {
    CheckBox checkbox = new CheckBox();
    SimpleBooleanProperty isSelected = new SimpleBooleanProperty(actuator.isOn());
    actuatorActive.put(actuator, isSelected);
    checkbox.selectedProperty().bindBidirectional(isSelected);
    checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null && newValue) {
        actuator.turnOn();
      } else {
        actuator.turnOff();
      }

      if (listener != null) {
        listener.onActuatorChanged(actuator.getNodeId(), actuator.getId(), actuator.isOn());
      }
    });
    return checkbox;
  }

  /**
   * Create a label for an actuator.
   *
   * @param actuator The actuator to create the label for.
   * @return The label for the actuator.
   */
  private Label createActuatorLabel(Actuator actuator) {
    SimpleStringProperty props = new SimpleStringProperty(generateActuatorText(actuator));
    actuatorValue.put(actuator, props);
    Label label = new Label();
    label.textProperty().bind(props);
    return label;
  }

  /**
   * Generate the text for an actuator.
   *
   * @param actuator The actuator to generate the text for.
   * @return The text for the actuator.
   */
  private String generateActuatorText(Actuator actuator) {
    String onOff = actuator.isOn() ? "ON" : "OFF";
    String actuatorType = actuator.getType();
    actuatorType = actuatorType.substring(0, 1).toUpperCase() + actuatorType.substring(1);
    return actuatorType + ": " + onOff;
  }

  /**
   * An actuator has been updated, update the corresponding GUI parts.
   *
   * @param actuator The actuator which has been updated
   */
  public void update(Actuator actuator) {
    SimpleStringProperty actuatorText = actuatorValue.get(actuator);
    SimpleBooleanProperty actuatorSelected = actuatorActive.get(actuator);
    if (actuatorText == null || actuatorSelected == null) {
      throw new IllegalStateException("Can't update GUI for an unknown actuator: " + actuator);
    }

    Platform.runLater(() -> {
      actuatorText.set(generateActuatorText(actuator));
      actuatorSelected.set(actuator.isOn());
    });
  }
}
