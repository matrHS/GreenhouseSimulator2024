package no.ntnu.tools.loggers;

import no.ntnu.tools.loggers.GenericLogger;

/**
 * Represents a logger for the controlPanel.
 * This class is a singleton.
 * It is used to log messages from the controlPanel.
 */
public class ControlPanelLogger extends GenericLogger {

  private static  ControlPanelLogger singleInstance = null;

  /**
   * Get the instance of the ControlPanelLogger.
   *
   * @return The instance of the ControlPanelLogger.
   */
  public static ControlPanelLogger getInstance() {
    if (singleInstance == null) {
      singleInstance = new ControlPanelLogger();
    }
    return singleInstance;
  }

  private ControlPanelLogger() {
    this.init("controlPanelLog");
  }

}
