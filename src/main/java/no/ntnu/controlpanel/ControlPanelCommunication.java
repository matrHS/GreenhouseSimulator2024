package no.ntnu.controlpanel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import no.ntnu.greenhouse.Actuator;

import no.ntnu.greenhouse.SensorReading;
import no.ntnu.tools.Logger;

public class ControlPanelCommunication extends Thread implements CommunicationChannel {
  private final ControlPanelLogic logic;
  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  private final static String SERVER_HOST = "localhost";
  private final int TCP_PORT = 1238;
  private Socket socket;


    /**
     * Constructor for the ControlPanelCommunication
     *
     * @param logic The logic for the control panel
     */
  public ControlPanelCommunication(ControlPanelLogic logic) {
    this.logic = logic;
  }

  @Override
  public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn) {
    String[] payload = new String[3];
    payload[0] = "set";
    payload[1] = nodeId + ":" + actuatorId;
    payload[2] = Boolean.toString(isOn);
    try {
      outputStream.writeObject(payload);
    } catch (IOException e) {
      Logger.error("Failed to send actuator change");
    }
  }

  @Override
  public boolean open() {
    return false;
  }

  /**
   * Opens a communication socket with the remote server and sets up the input and output streams.
   */
  private void instantiate() {
    try {
      this.socket = new Socket(SERVER_HOST, TCP_PORT);
      this.inputStream = new ObjectInputStream(socket.getInputStream());
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
      this.outputStream.writeObject("cp");
      logic.setCommunicationChannel(this);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Handles the readings from the sensor. The readings are split by comma and the type,
   * value and unit are extracted.
   * @param readings The readings from the sensor
   * @return A list of sensor readings
   */
  private List<SensorReading> handleReadings(String[] readings) {
    List<SensorReading> list = new ArrayList<>();
    if (readings.length > 3) {
      for(int i = 2; i < readings.length; i++){
        String[] values = readings[i].split(",");
        String type = values[0].split("=")[1];
        float value  = Float.parseFloat(values[1].split("=")[1]);
        String unit = values[2].split("=")[1];
        unit = unit.replace("}", "");
        list.add(new SensorReading(type, value, unit));
      }
    }
  return  list;
  }

  /**
   * Handles the payload from the server. The payload is split by comma and the type is extracted.
   * @param object The object from the server
   */
  private void handlePayload(Object object) {
    String[] payload = (object instanceof String[]) ? (String[]) object : null;
    if (payload != null) {
      switch (payload[0]) {
        case "add":
          SensorActuatorNodeInfo nodeInfo = new SensorActuatorNodeInfo(Integer.parseInt(payload[1]));
          for (int i = 2 ; i < payload.length; i+=2) {
            Actuator actuator = new Actuator(Integer.parseInt(payload[i+1]), payload[i], Integer.parseInt(payload[1]));
            nodeInfo.addActuator(actuator);
          }
          logic.onNodeAdded(nodeInfo);
          break;

        case "remove":
          break;

        case "data":
          logic.onSensorData(Integer.parseInt(payload[1]),handleReadings(payload));
          break;

        case "state":
          String[] IDs = payload[1].split(":");
          int nodeId = Integer.parseInt(IDs[0]);
          Actuator actuator = new Actuator(Integer.parseInt(IDs[1]), payload[2], nodeId);
          actuator.set(Boolean.parseBoolean(payload[3]));
         logic.actuatorUpdated(nodeId, actuator);
          break;

        case "update":
          break;

        default:
          break;
      }
    }
  }

  /**
   * Starts the thread for the control panel communication and listens for commands from the server.
   */
  @Override@SuppressWarnings("InfiniteLoopStatement")
  public void run() {
    this.instantiate();
    while(true) {
      try {
        socket.setSoTimeout(4000);
        Object object  = inputStream.readObject();
        Logger.info(object.toString());
       if (object != null){
         this.handlePayload(object);
       }
//       String[] payload = new String[3];
//       payload[0] = "set";
//       payload[1] = "53490";
//       payload[2] = "true";
//       outputStream.writeObject(payload);
       //run = false;
      }catch (SocketTimeoutException s) {
      Logger.info("thread timeout restarting");
      }
      catch (IOException e) {
        Logger.error("Thread timeout ");
      } catch (ClassNotFoundException e) {
        Logger.error("Failed to understand sent object");
      }
    }
  }
}
