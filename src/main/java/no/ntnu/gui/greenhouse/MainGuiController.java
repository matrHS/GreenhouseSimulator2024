package no.ntnu.gui.greenhouse;

import java.util.Map;
import javafx.scene.control.Tab;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.gui.controlpanel.ControlPanelApplication;

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
   * Get the home tab.
   *
   * @return The home tab
   */
  public Tab getHomeTab() {
    this.mainPage = new MainGui(this);
    return this.mainPage.getMainTab();
  }


}
