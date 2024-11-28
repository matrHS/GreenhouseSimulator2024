package no.ntnu.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControlPanelLogger {

  private Logger logger;
  private static  ControlPanelLogger  single_instance = null;


  public static ControlPanelLogger getInstance(){
    if(single_instance == null){
      single_instance = new ControlPanelLogger();
    }
    return single_instance;
  }

  private ControlPanelLogger(){
    logger = init();
  }
  private Logger init() {
    Handler handler;
    Logger logger;
    try {
      Files.createDirectories(Path.of("logs"));
      handler = new FileHandler("logs/controlPanelLog.log");
      logger = Logger.getLogger("controlPanelLog");
      Logger.getLogger("controlPanelLog").addHandler(handler);
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
