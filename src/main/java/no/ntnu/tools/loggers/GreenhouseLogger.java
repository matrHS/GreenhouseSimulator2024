package no.ntnu.tools.loggers;

/**
 * Represents a logger for a greenhouse.
 * This class is a singleton.
 * It is used to log messages from the greenhouse.
 */
public class GreenhouseLogger extends GenericLogger {
  private static GreenhouseLogger singleInstance = null;

  private GreenhouseLogger() {
    this.init("greenhouseLog");
  }

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
}
