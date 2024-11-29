package no.ntnu.tools.loggers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * A logger class for encapsulating all the logging.
 *
 */

abstract class GenericLogger {
  private Logger logger;
  /**
   * Log an information message.
   *
   * @param message The message to log. A newline is appended automatically.
   */

  public void info(String message) {
    logger.log(Level.INFO, message);
  }

  /**
   * Log an error message to file.
   *
   * @param message The error message to log.
   */

  public void error(String message) {
    logger.log(Level.WARNING, message);
  }

  protected void init(String path) {
    Handler handler;
    java.util.logging.Logger logger;
    try {
      Files.createDirectories(Path.of("logs"));
      handler = new FileHandler("logs/" + path);
      logger = java.util.logging.Logger.getLogger(path);
      Logger.getLogger(path).addHandler(handler);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.logger = logger;
  }
}
