package no.ntnu.gui.controlpanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelCommunication;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.SensorActuatorNodeInfo;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.Sensor;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.gui.common.ActuatorPane;
import no.ntnu.gui.common.SensorPane;
import no.ntnu.gui.greenhouse.NodeGuiBox;
import no.ntnu.listeners.common.CommunicationChannelListener;
import no.ntnu.listeners.controlpanel.GreenhouseEventListener;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.tools.Logger;

/**
 * Run a control panel with a graphical user interface (GUI), with JavaFX.
 */
public class ControlPanelApplication implements GreenhouseEventListener,
    CommunicationChannelListener {

  private static ControlPanelLogic logic;
  private static ControlPanelCommunication channel;


  private Tab controlPanelTab;
  private HBox mainPane;
  private final Map<Integer, SensorPane> sensorPanes = new HashMap<>();
  private final Map<Integer, ActuatorPane> actuatorPanes = new HashMap<>();
  private final Map<Integer, SensorActuatorNodeInfo> nodeInfos = new HashMap<>();

  public ControlPanelApplication(ControlPanelLogic logic, CommunicationChannel channel){
    if (logic == null) {
      throw new IllegalArgumentException("Control panel logic can't be null");
    }
    ControlPanelApplication.logic = logic;
    ControlPanelApplication.channel = (ControlPanelCommunication) channel;
    Logger.info("Running control panel GUI...");
    if (channel == null) {
      throw new IllegalStateException(
          "No communication channel. See the README on how to use fake event spawner!");
    }
    this.controlPanelTab = setMainStage();
  }

  private Tab setMainStage(){
    this.mainPane = new HBox();
    logic.addListener(this);
    logic.setCommunicationChannelListener(this);
    channel.start();
    if (!channel.open()) {
      logic.onCommunicationChannelClosed();
    }
    this.controlPanelTab = new Tab("Control panel", this.mainPane);
    this.controlPanelTab.getStyleClass().add("root");
    return this.controlPanelTab;
  }

  @Override
  public void onNodeAdded(SensorActuatorNodeInfo nodeInfo) {
    Platform.runLater(() -> addNode(nodeInfo));
  }

  @Override
  public void onNodeRemoved(int nodeId) {
    SensorActuatorNodeInfo nodeInfo = nodeInfos.get(nodeId);
    if (nodeInfo != null) {
      Platform.runLater(() -> {
        forgetNodeInfo(nodeId);
        if (nodeInfos.isEmpty()) {
          mainPane.getChildren().add(new HBox(new Label("No greenhouses found")));
        }
        mainPane.getChildren().clear();
        mainPane.getChildren().add(centerPane());
      });
      Logger.info("Greenhouse " + nodeId + " removed");
    } else {
      Logger.error("Can't remove greenhouse " + nodeId);
    }
  }

  @Override
  public void onSensorData(int nodeId, List<SensorReading> sensors) {
    Logger.info("Sensor data from greenhouse " + nodeId);
    SensorPane sensorPane = sensorPanes.get(nodeId);
    if (sensorPane != null) {
      sensorPane.update(sensors);
    } else {
      Logger.error("No sensor section for greenhouse " + nodeId);
    }
  }

  @Override
  public void onActuatorStateChanged(int nodeId, int actuatorId, boolean isOn) {
    String state = isOn ? "ON" : "off";
    Logger.info("actuator[" + actuatorId + "] on greenhouse " + nodeId + " is " + state);
    ActuatorPane actuatorPane = actuatorPanes.get(nodeId);
    if (actuatorPane != null) {
      Actuator actuator = getStoredActuator(nodeId, actuatorId);
      if (actuator != null) {
        if (isOn) {
          actuator.turnOn();
        } else {
          actuator.turnOff();
        }
        actuatorPane.update(actuator);
      } else {
        Logger.error(" actuator not found");
      }
    } else {
      Logger.error("No actuator section for greenhouse " + nodeId);
    }
  }

  private Actuator getStoredActuator(int nodeId, int actuatorId) {
    Actuator actuator = null;
    SensorActuatorNodeInfo nodeInfo = nodeInfos.get(nodeId);
    if (nodeInfo != null) {
      actuator = nodeInfo.getActuator(actuatorId);
    }
    return actuator;
  }

  private void forgetNodeInfo(int nodeId) {
    sensorPanes.remove(nodeId);
    actuatorPanes.remove(nodeId);
    nodeInfos.remove(nodeId);
    reloadCenterPane();
  }

  private void addNode(SensorActuatorNodeInfo nodeInfo) {
    if (nodeInfos.get(nodeInfo.getId()) == null) {
      nodeInfos.put(nodeInfo.getId(), nodeInfo);
      reloadCenterPane();
    } else {
      Logger.info("Duplicate node spawned, ignore it");
    }
  }

  private void reloadCenterPane() {
    mainPane.getChildren().clear();
    mainPane.getChildren().add(centerPane());
  }

  private HBox centerPane() {
    HBox centerPane = new HBox();

    for(Map.Entry<Integer, SensorActuatorNodeInfo> entry : nodeInfos.entrySet()){

      SensorActuatorNodeInfo nodeInfo = entry.getValue();
      SensorPane sensorPane = new SensorPane();
      sensorPanes.put(nodeInfo.getId(), sensorPane);
      ActuatorPane actuatorPane = new ActuatorPane(nodeInfo.getActuators());
      actuatorPanes.put(nodeInfo.getId(), actuatorPane);

      SensorActuatorNode node = new SensorActuatorNode(nodeInfo.getId());
      for (Actuator actuator : nodeInfo.getActuators()) {
        if(actuator != null){
          node.addActuator(actuator);
        }
      }
      BorderPane nodeBox = new BorderPane(new TitledPane("Greenhouse " + nodeInfo.getId(),
                                                         createNodeBox(new NodeGuiBox(node),
                                                                       nodeInfo.getId())));
      centerPane.getChildren().add(nodeBox);

    }
    return centerPane;
  }

  private HBox createNodeBox(NodeGuiBox nodeBoxObject, int nodeId){
    return new HBox(nodeBoxObject.createContent(actuatorPanes.get(nodeId), sensorPanes.get(nodeId)));
  }

  @Override
  public void onCommunicationChannelClosed() {
    Logger.info("Communication closed, closing the GUI");
    Platform.runLater(Platform::exit);
  }

  public Tab getControlPanelTab(){
    this.controlPanelTab = new Tab("Control panel", this.mainPane);
    return this.controlPanelTab;
  }
}
