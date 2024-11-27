package no.ntnu.gui.controlpanel;

import static javafx.application.Platform.exit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelCommunication;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.SensorActuatorNodeInfo;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.gui.common.ActuatorPane;
import no.ntnu.gui.common.SensorPane;
import no.ntnu.gui.greenhouse.Default;
import no.ntnu.gui.greenhouse.MainGuiController;
import no.ntnu.listeners.common.CommunicationChannelListener;
import no.ntnu.listeners.controlpanel.ActuatorChangedListener;
import no.ntnu.listeners.controlpanel.GreenhouseEventListener;
import no.ntnu.tools.Logger;

/**
 * Run a control panel with a graphical user interface (GUI), with JavaFX.
 */
public class ControlPanelApplication extends Application implements GreenhouseEventListener,
    CommunicationChannelListener, ActuatorChangedListener {

  private static ControlPanelLogic logic;
  private static ControlPanelCommunication channel;
  private final Map<Integer, SensorPane> sensorPanes = new HashMap<>();
  private final Map<Integer, ActuatorPane> actuatorPanes = new HashMap<>();
  private final Map<Integer, SensorActuatorNodeInfo> nodeInfos = new HashMap<>();
  private final Map<Integer, Tab> nodeTabs = new HashMap<>();
  private HBox mainPane;
  private Scene scene;
  private Stage stage;
  private TabPane tabPane; // The tab pane for the app
  private MainGuiController controller;


  private LinkedBlockingQueue<String[]> commandQueue;

  /**
   * Start the control panel application.
   *
   * @param logic   The logic for the control panel
   * @param channel The communication channel
   */
  public static void startApp(ControlPanelLogic logic, CommunicationChannel channel) {
    if (logic == null) {
      throw new IllegalArgumentException("Control panel logic can't be null");
    }
    ControlPanelApplication.logic = logic;
    ControlPanelApplication.channel = (ControlPanelCommunication) channel;
    Logger.info("Running control panel GUI...");
    launch();
  }

  /**
   * Create an empty content for the application.
   *
   * @return The empty content
   */
  private static Label createEmptyContent() {
    Label l = new Label("Waiting for node data...");
    l.setAlignment(Pos.CENTER);
    return l;
  }

  /**
   * Create an empty sensor pane for the application.
   *
   * @return The empty sensor pane
   */
  private static SensorPane createEmptySensorPane() {
    return new SensorPane();
  }

  /**
   * Start the application.
   *
   * @param stage The stage to start
   */
  @Override
  public void start(Stage stage) {
    if (channel == null) {
      throw new IllegalStateException(
          "No communication channel. See the README on how to use fake event spawner!");
    }
    commandQueue = new LinkedBlockingQueue<>();
    controller= new MainGuiController(this);

    this.stage = stage;
    this.stage.setTitle("Control Panel");

    BorderPane root = setMainPage();
    root.getStyleClass().add("root");
    mainPane = new HBox(root);
    scene = Default.defaultScene(mainPane);
    this.setScene(scene);

    logic.addListener(this);
    logic.setCommunicationChannelListener(this);
    channel.setCommandQueue(commandQueue);
    channel.start();
  }

  /**
   * Handle the event when a node is added.
   *
   * @param nodeInfo Information about the added node
   */
  @Override
  public void onNodeAdded(SensorActuatorNodeInfo nodeInfo) {
    Platform.runLater(() -> addNodeTab(nodeInfo));
  }

  /**
   * Handle the event when a node is removed.
   *
   * @param nodeId ID of the node which has disappeared (removed)
   */
  @Override
  public void onNodeRemoved(int nodeId) {
    SensorActuatorNodeInfo nodeInfo = nodeInfos.get(nodeId);
    if (nodeInfo != null) {
      Platform.runLater(() -> {
        forgetNodeInfo(nodeId);
        if (nodeInfos.isEmpty()) {
          mainPane.getChildren().clear();
          mainPane.getChildren().add(new HBox(new Label("No greenhouses found")));
        }
      });
      Logger.info("Greenhouse " + nodeId + " removed");
    } else {
      Logger.error("Can't remove greenhouse " + nodeId);
    }
  }

  /**
   * Set the scene for the application.
   *
   * @param scene The scene to set
   */
  public void setScene(Scene scene) {
    scene.getStylesheets().add(getClass().getResource("/css/stylesheet.css").toExternalForm());
    this.stage.setMaximized(true);
    stage.getIcons().add(new Image(
        Objects.requireNonNull(getClass().getResource("/images/Frokostklubben.jpg"))
               .toExternalForm()));
    stage.setScene(scene);
    stage.show();
    Logger.info("GUI subscribes to lifecycle events");
  }

  /**
   * Creates the tab pane for the application.
   *
   * @return The tab pane for the application.
   */
  private BorderPane setMainPage() {
    try {


      this.tabPane = new TabPane(controller.getHomeTab());
      tabPane.getStyleClass().add("tab-pane");
      tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

      BorderPane root = new BorderPane();
      VBox headerPane = Default.setHeader(this.controller);
      VBox top = new VBox(headerPane, tabPane);
      root.setTop(headerPane);
      root.setCenter(tabPane);
      root.setBottom(createBottomPane());
      return root;
    } catch (Exception e) {
      Logger.error("Error: " + e.getMessage());
    }
    return null;
  }

  private Node createBottomPane() {

    GridPane bottomPane = new GridPane();

    Button openActuators = new Button("Open Actuators");
    Button closeActuators = new Button("Close Actuators");
    Button toggleActuators = new Button("Toggle Actuators");

    openActuators.setOnAction(e -> channel.openActuators());
    closeActuators.setOnAction(e -> channel.closeActuators());
    toggleActuators.setOnAction(e -> channel.toggleActuators());

    bottomPane.add(openActuators, 0, 0);
    bottomPane.add(closeActuators, 1, 0);
    bottomPane.add(toggleActuators, 2, 0);

    return new HBox(bottomPane);
  }

  /**
   * Handle the event when new sensor data is received from a node.
   *
   * @param nodeId  ID of the node
   * @param sensors List of all current sensor values
   */
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

  /**
   * Handle the event when an actuator changes state.
   *
   * @param nodeId     ID of the node to which the actuator is attached
   * @param actuatorId ID of the actuator
   * @param isOn       When true, actuator is on; off when false.
   */
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

  /**
   * Get the stored actuator.
   *
   * @param nodeId     The node ID
   * @param actuatorId The actuator ID
   * @return The stored actuator
   */
  private Actuator getStoredActuator(int nodeId, int actuatorId) {
    Actuator actuator = null;
    SensorActuatorNodeInfo nodeInfo = nodeInfos.get(nodeId);
    if (nodeInfo != null) {
      actuator = nodeInfo.getActuator(actuatorId);
    }
    return actuator;
  }

  /**
   * Forget the node info.
   *
   * @param nodeId The node ID to forget
   */
  private void forgetNodeInfo(int nodeId) {
    sensorPanes.remove(nodeId);
    actuatorPanes.remove(nodeId);
    nodeInfos.remove(nodeId);
  }

  /**
   * Add a node.
   *
   * @param nodeInfo Information about the added node
   */
  private void addNode(SensorActuatorNodeInfo nodeInfo) {
    if (nodeInfos.get(nodeInfo.getId()) == null) {
      nodeInfos.put(nodeInfo.getId(), nodeInfo);

    } else {
      Logger.info("Duplicate node spawned, ignore it");
    }
  }

  /**
   * Reload the center pane.
   */
  private void reloadCenterPane() {
    mainPane.getChildren().clear();
    mainPane.getChildren().add(setMainPage());
  }

  /**
   * Add a node tab.
   *
   * @param nodeInfo Information about the added node
   */
  private void addNodeTab(SensorActuatorNodeInfo nodeInfo) {
    if (tabPane == null) {
      tabPane = new TabPane();
      scene.setRoot(tabPane);
    }
    Tab nodeTab = nodeTabs.get(nodeInfo.getId());
    if (nodeTab == null) {
      nodeInfos.put(nodeInfo.getId(), nodeInfo);
      tabPane.getTabs().add(createNodeTab(nodeInfo));
    } else {
      Logger.info("Duplicate node spawned, ignore it");
    }
  }

  /**
   * Create a node tab.
   *
   * @param nodeInfo Information about the added node
   * @return The created node tab
   */
  private Tab createNodeTab(SensorActuatorNodeInfo nodeInfo) {
    Tab tab = new Tab("Node " + nodeInfo.getId());
    SensorPane sensorPane = createEmptySensorPane();
    sensorPanes.put(nodeInfo.getId(), sensorPane);
    ActuatorPane actuatorPane = new ActuatorPane(nodeInfo.getActuators(), this);
    actuatorPanes.put(nodeInfo.getId(), actuatorPane);
    tab.setContent(new VBox(sensorPane, actuatorPane));
    nodeTabs.put(nodeInfo.getId(), tab);
    return tab;
  }

  /**
   * Handle the event when the communication channel is closed.
   */
  @Override
  public void onCommunicationChannelClosed() {
    Logger.info("Communication closed, closing the GUI");
    Platform.runLater(Platform::exit);
  }


  private void putOnCommandQueue(String[] payload){
    try {
     commandQueue.put(payload);
    } catch (InterruptedException e) {
      Logger.info("failed to put command on queue.");
    }
  }


  /**
   * Handle the event when an actuator is changed.
   *
   * @param nodeId     The ID of the node where the actuator is located.
   * @param actuatorId The ID of the actuator that has changed.
   * @param state      The new state of the actuator.
   */
  @Override
  public void onActuatorChanged(int nodeId, int actuatorId, boolean state) {
    String[] payload = new String[3];
    payload[0] = "set";
    payload[1] = nodeId + ":" + actuatorId;
    payload[2] = Boolean.toString(state);
    putOnCommandQueue(payload);
  }

  @Override
  public void stop() {
    // This code is reached only after the GUI-window is closed
    Logger.info("Exiting the control panel application");
    channel.closeCommunication();
    exit();
  }
}
