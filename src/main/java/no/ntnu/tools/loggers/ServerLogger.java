package no.ntnu.tools.loggers;


/**
 * Represents a logger for the server.
 * This class is a singleton.
 * It is used to log messages from the server.
 */

public class ServerLogger extends GenericLogger {
  private static ServerLogger singleInstance = null;

  private ServerLogger() {
    this.init("serverLog");
  }

  /**
   * Get the instance of the ServerLogger.
   *
   * @return The instance of the ServerLogger.
   */
  public static ServerLogger getInstance() {
    if (singleInstance == null) {
      singleInstance = new ServerLogger();
    }
    return singleInstance;
  }

}
