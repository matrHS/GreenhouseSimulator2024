package no.ntnu.controlpanel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import no.ntnu.greenhouse.Sensor;
import no.ntnu.greenhouse.SensorReading;

public class ControlPanelCommunication extends Thread implements CommunicationChannel {
  private final ControlPanelLogic logic;
  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  private final static String SERVER_HOST = "localhost";
  private final int TCP_PORT = 1238;
  private Socket socket;

  enum CLAZZ{
    NODE_INFO,
  }

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

  }

  @Override
  public boolean open() {
    return false;
  }

  /**
   * Opens a communication socket with the remote server and sets up the input and output streams.
   */
  private void instasiate() {
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

  private List<SensorReading> handleReadings(String[] readings) {
    List<SensorReading> list = new ArrayList<>();
    if (readings.length > 3) {
      for(int i = 2; i < readings.length-1; i++){
        String[] values = readings[i].split(",");
        String type = values[0].split("=")[1];
        int value  = Integer.parseInt(values[0].split("=")[1]);
        String unit = values[0].split("=")[1];
        list.add(new SensorReading(type, value, unit));
      }
    }
  return  list;
  }

  private void handlePayload(Object object) {
    String[] payload = (object instanceof String[]) ? (String[]) object : null;
    if (payload != null) {
      switch (payload[0]) {
        case "add":
          break;

        case "remove":
          break;

        case "data":
          logic.onSensorData(Integer.parseInt(payload[1]),handleReadings(payload));
          break;

        case "state":
          break;

        case "update":
          break;

        default:
          break;
      }
    }
  }

  @Override@SuppressWarnings("InfiniteLoopStatement")
  public void run() {
    this.instasiate();
    while(true) {
      try {
        Object object  = inputStream.readObject();
       this.handlePayload(object);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
