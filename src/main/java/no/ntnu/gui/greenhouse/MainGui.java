package no.ntnu.gui.greenhouse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
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
import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.tools.Logger;

/**
 * Run a greenhouse simulation with a graphical user interface (GUI), with JavaFX.
 */
public class MainGui extends Application implements NodeStateListener {
  private Scene scene; // The scene for the app
  Stage stage; // The stage for the app
  private MainGuiController controller; // the controller for the main page
  TabPane tabPane; // The tab pane for the app
  private static GreenhouseSimulator simulator; // The simulator for the greenhouse
  private static Map<Integer, SensorActuatorNode> nodes;
  private static Boolean fake;

  /**
   * Start the GUI Application.
   *
   * @param fake When true, emulate fake events instead of opening real sockets
   */
  public static void mainApp(Boolean fake){
    MainGui.fake = fake;
    Logger.info("Running greenhouse simulator with JavaFX GUI...");
    simulator = new GreenhouseSimulator(fake);
    nodes = simulator.getNodes();
    launch();
  }

  /**
   * Start is responsible for starting the application and
   *  setting up the main page.
   *
   * @param primaryStage The stage for the application
   * @throws Exception If the application can't start
   */
  @Override
  public void start(Stage primaryStage) throws Exception {
    this.stage = primaryStage;
    stage.setTitle("Greenhouse Simulator");

    this.controller = new MainGuiController(this);

    simulator.initialize();
    simulator.subscribeToLifecycleUpdates(this);
    this.stage.setOnCloseRequest(event -> closeApplication());
    simulator.start();

    BorderPane root = setMainPage();
    root.getStyleClass().add("root");

    scene = Default.defaultScene(root);
    scene.setCursor(Cursor.DEFAULT);
    this.setScene(scene);
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
   * Close the application.
   */
  private void closeApplication() {
    Logger.info("Closing Greenhouse application...");
    simulator.stop();
    try {
      stop();
    } catch (Exception e) {
      Logger.error("Could not stop the application: " + e.getMessage());
    }
  }

  /**
   * Creates the tab pane for the application.
   *
   * @return The tab pane for the application.
   */
  private BorderPane setMainPage() throws Exception{
    VBox headerPane = Default.setHeader(this.controller);

    this.tabPane = new TabPane(createMainScene());
    this.tabPane.getTabs().add(controller.getControlPanelTab(this.fake));
    for(Map.Entry<Integer, SensorActuatorNode> entry : nodes.entrySet()){
      SensorActuatorNode node = entry.getValue();
      tabPane.getTabs().add(controller.getGreenhouseWindow(node));
    }

    tabPane.getStyleClass().add("tab-pane");
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    BorderPane root = new BorderPane();
    VBox top = new VBox(headerPane,tabPane);
    root.setTop(top);

    return root;
  }

  /**
   * Create the main scene for the application.
   *
   * @return The main scene for the application
   */
  private Tab createMainScene(){
    BorderPane mainScene = new BorderPane();

    mainScene.getStyleClass().add("main-center-pane");

    mainScene.setCenter(createCenterPane());
    mainScene.setRight(new HBox());
    return new Tab("Home Page", mainScene);
  }

  /**
   * Create the center pane for the application.
   *
   * @return The center pane for the application
   */
  private GridPane createCenterPane(){
    GridPane centerPane = new GridPane();
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    centerPane.getColumnConstraints().add(new ColumnConstraints(bounds.getWidth()/2));
    centerPane.getColumnConstraints().add(new ColumnConstraints(bounds.getWidth()/2));

    try {
      Image greenhouseDrawing = new Image(
          Objects.requireNonNull(getClass().getResource(
              "/images/Greenhouse2.jpg")).toExternalForm());
      ImageView mainImage = new ImageView(greenhouseDrawing);

      mainImage.getStyleClass().add("main-image");
      GridPane.setHalignment(mainImage, HPos.CENTER);
      centerPane.add(mainImage, 0,0);

    } catch(Exception e){
      System.out.println("error:" + e.getMessage());
    }

    HBox textBox = new HBox();
    Text description = getDescription();
    textBox.getChildren().add(description);
    textBox.getStyleClass().add("text-box-main");

    GridPane.setHalignment(textBox, HPos.CENTER);
    centerPane.add(textBox, 1,0);

    return centerPane;

  }

  /**
   * Get the description box for the main page of the application.
   *
   * @return The description box for the main page of the application
   */
  private static Text getDescription() {
    Text description = new Text();
    description.setText("Your task in this project is to implement a meaningful " +
                        "application and its components for a" +
                        "complete smart farming solution. The solution includes " +
                        "communication with sockets. You need to" +
                        "design your application-layer communication protocol and implement" +
                        " necessary communication between the nodes so that the " +
                        "control-panel nodes can visualize sensor data and control " +
                        "the actuators on the sensor nodes.");
    description.setFont(new Font(16));
    description.setWrappingWidth(480);
    description.setTextAlignment(TextAlignment.CENTER);
    description.setX(100);
    return description;
  }

  /**
   * Set the main window for the application.
   */
  public void setMainWindow(){
    this.tabPane.getSelectionModel().select(tabPane.getTabs().get(0));
  }

  /**
   * Called when a node is ready.
   *
   * @param node the node which is ready now
   */

  @Override
  public void onNodeReady(SensorActuatorNode node) {
     Logger.info("Starting window for node " + node.getId());
    /*
    NodeGuiWindow window = new NodeGuiWindow(node);
    nodeWindows.put(node, window);
    window.show();

      */
  }

  /**
   * Called when a node is stopped.
   *
   * @param node The node which is stopped
   */

  @Override
  public void onNodeStopped(SensorActuatorNode node) {
      /*
      NodeGuiWindow window = nodeWindows.remove(node);
    if (window != null) {
      Platform.runLater(window::close);
      if (nodeWindows.isEmpty()) {
        Platform.runLater(this.stage::close);
      }
    }  */
  }

}
