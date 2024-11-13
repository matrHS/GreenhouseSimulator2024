package no.ntnu.gui.greenhouse;

import java.util.Map;
import javafx.collections.FXCollections;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import no.ntnu.greenhouse.SensorActuatorNode;

public class GreenhouseWindow {
  private MainGuiController mainGuiController;
  private Tab greenhouseTab;
  private SensorActuatorNode node;

  public GreenhouseWindow(MainGuiController controller, SensorActuatorNode node){
    this.mainGuiController = controller;
    this.node = node;
    try {
      this.greenhouseTab = this.setMainStage();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private Tab setMainStage() throws Exception{
    BorderPane root = new BorderPane();

    root.setCenter(centerPane());

    String name = "Greenhouse "+node.getId();
    this.greenhouseTab = new Tab(name, root);
    this.greenhouseTab .getStyleClass().add("root");
    return greenhouseTab;
  }

  private HBox centerPane() {
    HBox centerPane = new HBox();
    NodeGuiBox nodeBoxObject = new NodeGuiBox(node);
    centerPane.getChildren().add(createNodeBox(nodeBoxObject));
    return centerPane;
  }
  private HBox createNodeBox(NodeGuiBox nodeBoxObject){
    HBox nodeBox = nodeBoxObject.getNodeBox();
    return nodeBox;
  }

  public Tab getGreenhouseTab(){
    return this.greenhouseTab;
  }
}
