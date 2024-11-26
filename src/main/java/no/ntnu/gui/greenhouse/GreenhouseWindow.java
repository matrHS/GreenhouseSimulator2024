package no.ntnu.gui.greenhouse;

import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import no.ntnu.greenhouse.SensorActuatorNode;

/**
 * A class that creates a window for a greenhouse.
 */
public class GreenhouseWindow {
  private MainGuiController mainGuiController;
  private Tab greenhouseTab;
  private SensorActuatorNode node;

  /**
   * Create a new greenhouse window.
   *
   * @param controller The controller for the main GUI.
   * @param node      The node for the greenhouse.
   */
  public GreenhouseWindow(MainGuiController controller, SensorActuatorNode node) {
    this.mainGuiController = controller;
    this.node = node;
    try {
      this.greenhouseTab = this.setMainStage();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private Tab setMainStage() throws Exception {
    BorderPane root = new BorderPane();

    root.setCenter(centerPane());

    String name = "Greenhouse " + node.getId();
    this.greenhouseTab = new Tab(name, root);
    this.greenhouseTab.getStyleClass().add("root");
    return greenhouseTab;
  }

  private HBox centerPane() {
    HBox centerPane = new HBox();
    NodeGuiBox nodeBoxObject = new NodeGuiBox(node);
    centerPane.getChildren().add(createNodeBox(nodeBoxObject));
    return centerPane;
  }

  private HBox createNodeBox(NodeGuiBox nodeBoxObject) {
    HBox nodeBox = nodeBoxObject.getNodeBox();
    return nodeBox;
  }

  public Tab getGreenhouseTab() {
    return this.greenhouseTab;
  }
}
