package no.ntnu.controlpanel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorReading;
import no.ntnu.tools.ControlPanelLogger;
import no.ntnu.tools.RSA;
import no.ntnu.tools.Config;

/**
 * The communication channel for the control panel. It communicates with the server and sends
 */
public class ControlPanelCommunication extends Thread implements CommunicationChannel {
  private final ControlPanelLogic logic;
  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;
  private Socket socket;
  private LinkedBlockingQueue<String[]> commandQueue;

  private final BigInteger[] keys = this.keyGen();
  private ControlPanelLogger logger = ControlPanelLogger.getInstance();

  /**
   * Constructor for the ControlPanelCommunication.
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

      outputStream.writeObject(RSA.encrypt(payload,keys));
    } catch (IOException e) {
      logger.error("Failed to send actuator change");
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
      this.socket = new Socket(Config.SERVER_ADDRESS, Config.SERVER_PORT);
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
   *
   * @param readings The readings from the sensor
   * @return A list of sensor readings
   */
  private List<SensorReading> handleReadings(String[] readings) {
    List<SensorReading> list = new ArrayList<>();
    if (readings.length >= 3) {
      for (int i = 2; i < readings.length; i++) {
        String[] values = readings[i].split(",");
        String type = values[0].split("=")[1];
        float value = Float.parseFloat(values[1].split("=")[1]);
        String unit = values[2].split("=")[1];
        unit = unit.replace("}", "");
        list.add(new SensorReading(type, value, unit));
      }
    }
    return list;
  }

  /**
   * Handles the payload from the server. The payload is split by comma and the type is extracted.
   *
   * @param object The object from the server
   */
  private void handlePayload(Object object) {
    String[] payload = (object instanceof String[]) ? RSA.decrypt((String[])object, keys) : null;

    if (payload != null) {
      switch (payload[0]) {
        case "add":

          SensorActuatorNodeInfo nodeInfo =
              new SensorActuatorNodeInfo(Integer.parseInt(payload[1]));
          for (int i = 2; i < payload.length; i += 3) {
            Actuator actuator = new Actuator(Integer.parseInt(payload[i + 1]), payload[i],
                Integer.parseInt(payload[1]));
            Boolean state = Boolean.parseBoolean(payload[i + 2]);
            actuator.set(state);

            nodeInfo.addActuator(actuator);
          }
          logic.onNodeAdded(nodeInfo);
          break;

        case "remove":
          break;

        case "data":
          logic.onSensorData(Integer.parseInt(payload[1]), handleReadings(payload));
          break;

        case "state":
          String[] ids = payload[1].split(":");
          int nodeId = Integer.parseInt(ids[0]);
          Actuator actuator = new Actuator(Integer.parseInt(ids[1]), payload[2], nodeId);
          actuator.set(Boolean.parseBoolean(payload[3]));
          logic.actuatorUpdated(nodeId, actuator);
          break;

        case "update":
          break;
        case "aggregate":
          logic.onAggregateSensorData(Integer.parseInt(payload[1]), handleReadings(payload));
          break;

        default:
          break;
      }
    }
  }

  private void sendCommandIfExists() {
    while (this.commandQueue.peek() != null) {
      try {
        String[] sealedPayload = RSA.encrypt(commandQueue.poll(), keys);
        outputStream.writeObject(sealedPayload);
      } catch (IOException e) {
        logger.info("Failed to write to the server");
      }
    }
  }

  /**
   * Closes the communication socket.
   */
  public void closeCommunication() {
    try {
      socket.close();
    } catch (IOException e) {
      logger.error("Failed to close communication");

    }
  }

  /**
   * Starts the thread for the control panel communication and listens for commands from the server.
   */
  @Override
  public void run() {
    this.instantiate();
    while (!socket.isClosed()) {
      try {
        socket.setSoTimeout(Config.TIMEOUT);
        Object object = inputStream.readObject();
        if (object != null) {
          this.handlePayload(object);
        }
      } catch (SocketTimeoutException s) {
        sendCommandIfExists();
      } catch (IOException e) {
        logger.error("Thread timeout ");
      } catch (ClassNotFoundException e) {
        logger.error("Failed to understand sent object");
      }
    }
  }

  public void setCommandQueue(LinkedBlockingQueue<String[]> commandQueue) {
    this.commandQueue = commandQueue;
  }

  /**
   * Open all actuators.
   * Broadcast.
   */
  public void openActuators() {
    sendActuatorChange(-1, -1, true);
  }

  /**
   * Close all actuators.
   * Broadcast.
   */
  public void closeActuators() {
    sendActuatorChange(-1, -1, false);
  }

  /**
   * Toggle all actuators.
   * Broadcast.
   */
  public void toggleActuators() {
    sentActuatorToggle(-1, -1);
  }

  private void sentActuatorToggle(int nodeId, int actuatorId) {
    String[] payload = new String[2];
    payload[0] = "toggle";
    payload[1] = nodeId + ":" + actuatorId;
    try {
      outputStream.writeObject(RSA.encrypt(payload, keys));
    } catch (IOException e) {
      logger.error("Failed to send actuator change");
    }
  }

  private BigInteger[] keyGen(){
    int p = 7;
    int q = 19;
    BigInteger product = BigInteger.valueOf(p*q);
    int totient = (p-1)*(q-1);
    BigInteger pubKey = BigInteger.valueOf(29);
    BigInteger privKey = BigInteger.valueOf(41);
    return new BigInteger[]{privKey,pubKey,product};
  }
}
