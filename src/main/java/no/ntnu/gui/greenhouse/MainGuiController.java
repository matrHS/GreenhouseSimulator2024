package no.ntnu.gui.greenhouse;

import java.util.Map;
import javafx.scene.control.Tab;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.gui.controlpanel.ControlPanelApplication;
import no.ntnu.tools.Logger;

/**
 * The controller for the main GUI.
 */
public class MainGuiController {

  private static Map<Integer, SensorActuatorNode> nodes;
  private ControlPanelApplication mainWindow;
  private MainGui mainPage;

  /**
   * Create a new main GUI controller.
   *
   * @param mainWindow The main window of the application
   */
  public MainGuiController(ControlPanelApplication mainWindow) {
    this.mainWindow = mainWindow;
  }

  /**
   * Get the greenhouse window.
   *
   * @param node The node to get the greenhouse window for
   * @return The greenhouse window
   */
  public Tab getGreenhouseWindow(SensorActuatorNode node) {
    GreenhouseWindow greenhouseWindow = new GreenhouseWindow(this, node);
    return greenhouseWindow.getGreenhouseTab();
  }

  /**
   * Get the home tab.
   *
   * @return The home tab
   */
  public Tab getHomeTab() {
    this.mainPage = new MainGui(this);
    return this.mainPage.getMainTab();
  }


}
