package no.ntnu.tools.loggers;

import no.ntnu.tools.loggers.GenericLogger;

/**
 * Represents a logger for a greenhouse.
 * This class is a singleton.
 * It is used to log messages from the greenhouse.
 */
public class GreenhouseLogger extends GenericLogger {
  private static  GreenhouseLogger singleInstance = null;

  /**
   * Get the instance of the GreenhouseLogger.
   *
   * @return The instance of the GreenhouseLogger.
   */

  public static GreenhouseLogger getInstance() {
    if (singleInstance == null) {
      singleInstance = new GreenhouseLogger();
    }
    return singleInstance;
  }

  private GreenhouseLogger() {
    this.init("greenhouseLog");
  }
}
