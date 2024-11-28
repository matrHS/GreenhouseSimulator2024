package no.ntnu.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GreenhouseLogger {
  private java.util.logging.Logger logger;
  private static  GreenhouseLogger single_instance = null;


  public static GreenhouseLogger getInstance(){
    if(single_instance == null){
      single_instance = new GreenhouseLogger();
    }
    return single_instance;
  }

  private GreenhouseLogger(){
    logger = init();
  }
  private java.util.logging.Logger init() {
    Handler handler;
    java.util.logging.Logger logger;
    try {
      Files.createDirectories(Path.of("logs"));
      handler = new FileHandler("logs/greenhouseLog.log");
      logger = java.util.logging.Logger.getLogger("greenhouseLog");
      Logger.getLogger("greenhouseLog").addHandler(handler);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return  logger;
  }

  public void info(String message){
    logger.log(Level.INFO, message);
  }

  public void error(String message){
    logger.log(Level.WARNING, message);
  }

}
