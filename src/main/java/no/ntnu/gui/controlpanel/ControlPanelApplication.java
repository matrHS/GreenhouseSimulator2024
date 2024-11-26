package no.ntnu.gui.controlpanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelCommunication;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.SensorActuatorNodeInfo;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.gui.common.ActuatorPane;
import no.ntnu.gui.common.SensorPane;
import no.ntnu.gui.greenhouse.Default;
import no.ntnu.gui.greenhouse.MainGuiController;
import no.ntnu.listeners.common.CommunicationChannelListener;
import no.ntnu.listeners.controlpanel.ActuatorChangedListener;
import no.ntnu.listeners.controlpanel.GreenhouseEventListener;
import no.ntnu.server.ControlPanelHandler;
import no.ntnu.tools.Logger;

/**
 * Run a control panel with a graphical user interface (GUI), with JavaFX.
 */
public class ControlPanelApplication extends Application implements GreenhouseEventListener,
    CommunicationChannelListener, ActuatorChangedListener {

  private static ControlPanelLogic logic;
  private static ControlPanelCommunication channel;

  private HBox mainPane;
  private Scene scene;
  private Stage stage;
  private TabPane tabPane; // The tab pane for the app
  private MainGuiController controller;
  private final Map<Integer, SensorPane> sensorPanes = new HashMap<>();
  private final Map<Integer, ActuatorPane> actuatorPanes = new HashMap<>();
  private final Map<Integer, SensorActuatorNodeInfo> nodeInfos = new HashMap<>();
  private final Map<Integer, Tab> nodeTabs = new HashMap<>();

  private LinkedBlockingQueue<String[]> commandQueue;


  public static void startApp(ControlPanelLogic logic, CommunicationChannel channel) {
    if (logic == null) {
      throw new IllegalArgumentException("Control panel logic can't be null");
    }
    ControlPanelApplication.logic = logic;
    ControlPanelApplication.channel = (ControlPanelCommunication) channel;
    Logger.info("Running control panel GUI...");
    launch();
  }

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

  private static Label createEmptyContent() {
    Label l = new Label("Waiting for node data...");
    l.setAlignment(Pos.CENTER);
    return l;
  }

  @Override
  public void onNodeAdded(SensorActuatorNodeInfo nodeInfo) {
    Platform.runLater(() -> addNodeTab(nodeInfo));
  }

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
  public void setScene(Scene scene){
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
  private BorderPane setMainPage(){
    try{
      VBox headerPane = Default.setHeader(this.controller);

      this.tabPane = new TabPane(controller.getHomeTab());
      tabPane.getStyleClass().add("tab-pane");
      tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

      BorderPane root = new BorderPane();
      VBox top = new VBox(headerPane,tabPane);
      root.setTop(top);
      return root;
    } catch (Exception e){
      Logger.error("Error: " + e.getMessage());
    }
    return null;
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
  }


  private void addNode(SensorActuatorNodeInfo nodeInfo) {
    if (nodeInfos.get(nodeInfo.getId()) == null) {
      nodeInfos.put(nodeInfo.getId(), nodeInfo);

    } else {
      Logger.info("Duplicate node spawned, ignore it");
    }
  }

  private void reloadCenterPane() {
    mainPane.getChildren().clear();
    mainPane.getChildren().add(setMainPage());
  }

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

  private static SensorPane createEmptySensorPane() {
    return new SensorPane();
  }


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

  @Override
  public void onActuatorChanged(int nodeId, int actuatorId, boolean state) {
    String[] payload = new String[3];
    payload[0] = "set";
    payload[1] = nodeId + ":" + actuatorId;
    payload[2] = Boolean.toString(state);
    Platform.runLater(()->{
      putOnCommandQueue(payload);
    });
  }
}
