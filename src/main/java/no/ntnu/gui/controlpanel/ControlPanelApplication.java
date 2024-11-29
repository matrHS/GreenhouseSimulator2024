package no.ntnu.gui.controlpanel;

import static javafx.application.Platform.exit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelCommunication;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.SensorActuatorNodeInfo;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.ActuatorCollection;
import no.ntnu.greenhouse.Camera;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.gui.common.ActuatorPane;
import no.ntnu.gui.common.CameraPane;
import no.ntnu.gui.common.SensorPane;
import no.ntnu.gui.greenhouse.Default;
import no.ntnu.gui.greenhouse.MainGuiController;
import no.ntnu.listeners.common.CommunicationChannelListener;
import no.ntnu.listeners.controlpanel.ActuatorChangedListener;
import no.ntnu.listeners.controlpanel.GreenhouseEventListener;
import no.ntnu.tools.ControlPanelLogger;
import no.ntnu.tools.Logger;

/**
 * Run a control panel with a graphical user interface (GUI), with JavaFX.
 */
public class ControlPanelApplication extends Application implements GreenhouseEventListener,
    CommunicationChannelListener, ActuatorChangedListener {

  private static ControlPanelLogic logic;
  private static ControlPanelCommunication channel;
  private final Map<Integer, SensorPane> sensorPanes = new HashMap<>();
  private final Map<Integer, SensorPane> aggregatePanes = new HashMap<>();
  private final Map<Integer, ActuatorPane> actuatorPanes = new HashMap<>();
  private final Map<Integer, CameraPane> cameraPanes = new HashMap<>();
  private final Map<Integer, SensorActuatorNodeInfo> nodeInfos = new HashMap<>();
  private final Map<Integer, Tab> nodeTabs = new HashMap<>();
  private HBox mainPane;
  private Scene scene;
  private Stage stage;
  private TabPane tabPane; // The tab pane for the app
  private MainGuiController controller;
  private ControlPanelLogger logger = ControlPanelLogger.getInstance();


  private LinkedBlockingQueue<String[]> commandQueue;

  /**
   * Start the control panel application.
   *
   * @param logic   The logic for the control panel
   * @param channel The communication channel
   */
  public static void startApp(ControlPanelLogic logic, CommunicationChannel channel) {
    ControlPanelLogger logger = ControlPanelLogger.getInstance();
    if (logic == null) {
      throw new IllegalArgumentException("Control panel logic can't be null");
    }
    ControlPanelApplication.logic = logic;
    ControlPanelApplication.channel = (ControlPanelCommunication) channel;
    logger.info("Running control panel GUI...");
    launch();
  }

  /**
   * Create an empty sensor pane for the application.
   *
   * @return The empty sensor pane
   */
  private static SensorPane createEmptySensorPane(String title) {

    return new SensorPane(title);
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
    controller = new MainGuiController(this);

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
   * Set the scene for the application.
   *
   * @param scene The scene to set
   */
  public void setScene(Scene scene) {
    scene.getStylesheets().add(getClass().getResource("/css/stylesheet.css").toExternalForm());
    stage.getIcons().add(new Image(
        Objects.requireNonNull(getClass().getResource("/images/Frokostklubben.jpg"))
            .toExternalForm()));
    stage.setScene(scene);
    stage.setResizable(false);
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
      root.setTop(headerPane);
      root.setCenter(tabPane);
      root.setBottom(createBottomPane());
      return root;
    } catch (Exception e) {
      Logger.error("Error: " + e.getMessage());
    }
    return null;
  }

  /**
   * Create the bottom pane for the application.
   * This pane contains buttons to open, close and toggle actuators.
   *
   * @return The bottom pane for the application
   */
  private Node createBottomPane() {

    HBox bottomPane = new HBox();
    bottomPane.getStyleClass().add("actuators-buttons");

    Button openActuators = new Button("Open all Actuators");
    Button closeActuators = new Button("Close all Actuators");
    Button toggleActuators = new Button("Toggle all Actuators");

    openActuators.setOnAction(e -> channel.openActuators());
    closeActuators.setOnAction(e -> channel.closeActuators());
    toggleActuators.setOnAction(e -> channel.toggleActuators());

    bottomPane.getChildren().addAll(openActuators, closeActuators, toggleActuators);

    return bottomPane;
  }

  private Node createActuatorGroupButtons(SensorActuatorNodeInfo node) {
    HBox bottomPane = new HBox();
    bottomPane.getStyleClass().add("actuators-multi-buttons");

    Button openActuators = new Button("Open Actuators");
    Button closeActuators = new Button("Close Actuators");
    Button toggleActuators = new Button("Toggle Actuators");

    openActuators.setOnAction(e -> channel.openActuatorsForNode(node.getId()));
    closeActuators.setOnAction(e -> channel.closeActuatorsForNode(node.getId()));
    toggleActuators.setOnAction(e -> channel.toggleActuatorsForNode(node.getId()));

    bottomPane.getChildren().addAll(openActuators, closeActuators, toggleActuators);

    return bottomPane;
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
        tabPane.getTabs().remove(nodeTabs.get(nodeId));
        nodeTabs.remove(nodeId);
      });
      logger.info("Greenhouse " + nodeId + " removed");
    } else {
      logger.error("Can't remove greenhouse " + nodeId);
    }
  }

  /**
   * Handle the event when new sensor data is received from a node.
   *
   * @param nodeId  ID of the node
   * @param sensors List of all current sensor values
   */
  @Override
  public void onSensorData(int nodeId, List<SensorReading> sensors) {
    logger.info("Sensor data from greenhouse " + nodeId);
    SensorPane sensorPane = sensorPanes.get(nodeId);
    if (sensorPane != null) {
      sensorPane.update(sensors);
    } else {
      logger.error("No sensor section for greenhouse " + nodeId);
    }
  }

  /**
   * Handle the event when new images from camera is received from a node.
   *
   * @param nodeId  ID of the node
   * @param cameras List of all current cameras
   */
  @Override
  public void onImageSensor(int nodeId, List<Camera> cameras) {
    Logger.info("Image data from greenhouse " + nodeId);
    CameraPane cameraPane = cameraPanes.get(nodeId);
    if (cameraPane != null) {
      cameraPane.update(cameras);
    } else {
      Logger.error("No camera section for greenhouse " + nodeId);
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
    logger.info("actuator[" + actuatorId + "] on greenhouse " + nodeId + " is " + state);
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
        logger.error(" actuator not found");
      }
    } else {
      logger.error("No actuator section for greenhouse " + nodeId);
    }
  }

  /**
   * Handle the event when new aggregate sensor data is received from a node.
   *
   * @param nodeId  ID of the node
   * @param sensors List of all current sensor values
   */
  @Override
  public void onAggregateSensorData(int nodeId, List<SensorReading> sensors) {
    logger.info("1 minute aggregate data from greenhouse " + nodeId);
    SensorPane aggregatePane = aggregatePanes.get(nodeId);
    if (aggregatePane != null) {
      aggregatePane.update(sensors);
    } else {
      logger.error("No sensor section for greenhouse " + nodeId);
    }
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
      logger.info("Duplicate node spawned, ignore it");
    }
  }

  /**
   * Create a node tab.
   *
   * @param nodeInfo Information about the added node
   * @return The created node tab
   */
  private Tab createNodeTab(SensorActuatorNodeInfo nodeInfo) {

    SensorPane sensorPane = createEmptySensorPane("Sensors");
    sensorPanes.put(nodeInfo.getId(), sensorPane);

    ActuatorPane actuatorPane = new ActuatorPane(nodeInfo.getActuators(), this);
    actuatorPanes.put(nodeInfo.getId(), actuatorPane);

    Node actuatorGroupButtons = new HBox();
    if (nodeInfo.getActuators().size() > 1) {
      actuatorGroupButtons = createActuatorGroupButtons(nodeInfo);
    }


    CameraPane cameraPane = new CameraPane();
    cameraPanes.put(nodeInfo.getId(), cameraPane);

    SensorPane aggregatePane = new SensorPane("1 minute average");
    aggregatePanes.put(nodeInfo.getId(), aggregatePane);

    VBox greenhuose = new VBox(sensorPane, actuatorPane, actuatorGroupButtons, cameraPane, aggregatePane);
    greenhuose.setMaxWidth(700);
    greenhuose.setMinWidth(700);
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    greenhuose.setMinWidth(bounds.getWidth());
    greenhuose.setMaxWidth(bounds.getWidth());
    ScrollPane scrollPane = new ScrollPane(greenhuose);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    Tab tab = new Tab("Greenhouse " + nodeInfo.getId());
    tab.setContent(scrollPane);
    nodeTabs.put(nodeInfo.getId(), tab);
    return tab;
  }

  /**
   * Put a command on the queue.
   *
   * @param payload The command to put on the queue
   */
  private void putOnCommandQueue(String[] payload) {
    try {
      commandQueue.put(payload);
    } catch (InterruptedException e) {
      Logger.info("failed to put command on queue.");
    }
  }

  /**
   * Handle the event when the communication channel is closed.
   */
  @Override
  public void onCommunicationChannelClosed() {
    logger.info("Communication closed, closing the GUI");
    Platform.runLater(Platform::exit);
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

  /**
   * Stop the application.
   */
  @Override
  public void stop() {
    // This code is reached only after the GUI-window is closed
    logger.info("Exiting the control panel application");
    channel.closeCommunication();
    exit();
  }
}
