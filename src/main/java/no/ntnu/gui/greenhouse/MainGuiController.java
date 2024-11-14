package no.ntnu.gui.greenhouse;

import java.util.Map;
import javafx.scene.control.Tab;
import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.gui.controlpanel.ControlPanelApplication;
import no.ntnu.run.ControlPanelStarter;

public class MainGuiController {

  private ControlPanelApplication mainWindow;
  private static Map<Integer, SensorActuatorNode> nodes;

  public MainGuiController(ControlPanelApplication mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void setMainPageTab() { this.mainWindow.setMainWindow();}

  public Tab getGreenhouseWindow(SensorActuatorNode node){
    GreenhouseWindow greenhouseWindow = new GreenhouseWindow(this, node);
    return greenhouseWindow.getGreenhouseTab();
  }

}
