package no.ntnu.greenhouse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import no.ntnu.listeners.common.ActuatorListener;
import no.ntnu.listeners.common.CameraListener;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.listeners.greenhouse.SensorListener;
import no.ntnu.tools.Config;
import no.ntnu.tools.Logger;
import no.ntnu.tools.RSA;

/**
 * The GreenhouseNode class is responsible for handling the communication between the greenhouse and
 * the server. It listens for commands from the server and sends sensor readings to the server.
 */
public class GreenhouseNode extends TimerTask
    implements SensorListener, NodeStateListener, ActuatorListener,
    CameraListener {
  private final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();
  private final BigInteger[] keys = this.keyGen();
  private ObjectInputStream objectInputStream;
  private ObjectOutputStream objectOutputStream;
  private Socket socket;
  private SensorActuatorNode node;
  private LinkedBlockingQueue<String[]> commandQueue;
  private boolean allowSendReading;
  private ArrayList<ArrayList<SensorReading>> aggregateReadings = new ArrayList<>();


  /**
   * Create a greenhouse node that should connect to specified TCP port of server.
   */
  public GreenhouseNode() {
  }

  /**
   * Start the greenhouse.
   */
  public void start() {
    node.start();
    this.commandQueue = new LinkedBlockingQueue<>();
    Timer minAggregateTimer = new Timer();
    minAggregateTimer.schedule(this, 5000, 60000);
    while (!socket.isClosed()) {
      listenForCommands();
      sendCommandIfExists();

    }
  }


  /**
   * Initializes the greenhouse.
   * Uses arguments to decide number of nodes and their properties.
   * Parsed as integers.
   *
   * @param args temperature, humidity, windows, fans, heaters
   */
  public void initialize(String[] args) {
    allowSendReading = true;

    if (argsValidator(args)) {
      int temperature = Integer.parseInt(args[0]);
      int humidity = Integer.parseInt(args[1]);
      int windows = Integer.parseInt(args[2]);
      int fans = Integer.parseInt(args[3]);
      int heaters = Integer.parseInt(args[4]);

      createNode(temperature, humidity, windows, fans, heaters, 1);
    } else {
      createNode(1, 2, 1, 1, 1, 1);
      System.out.println("Greenhouse initialized with default sensors "
                         + "(1 temperature, 2 humidity, 1 window, 1 camera)");
    }
    this.node.addStateListener(this);

    initiateCommunication();
    System.out.println("Greenhouse initialized and connected");
    processCommand();
    aggregateReadings = new ArrayList<>();

  }

  /**
   * Checks the arguments for validity.
   * If all args are valid, return true.
   *
   * @param args arguments to check for integer validity
   * @return true if all arguments are valid, false otherwise
   */
  private boolean argsValidator(String[] args) {
    boolean valid = false;
    for (String arg : args) {
      try {
        Integer.parseInt(arg);
        valid = true;
      } catch (NumberFormatException e) {
        System.out.println("Invalid argument: " + arg);
      }
    }
    return valid;
  }


  /**
   * Create a greenhouse node with the given properties.
   *
   * @param temperature temperature sensor count
   * @param humidity    humidity sensor count
   * @param windows     window actuator count
   * @param fans        fan actuator count
   * @param heaters     heater actuator count
   * @param cameras     camera count
   */
  private void createNode(int temperature, int humidity, int windows, int fans, int heaters,
                          int cameras) {
    this.node = DeviceFactory.createNode(
        temperature, humidity, windows, fans, heaters, cameras);
    node.addSensorListener(this);
  }


  /**
   * Initializes communication between a node and the server.
   * Each node is defined as a greenhouse consisting of multiple sensors.
   */
  private void initiateCommunication() {
    try {
      this.socket = new Socket(Config.SERVER_ADDRESS, Config.SERVER_PORT);
      socket.setSoTimeout(Config.TIMEOUT);
      this.objectInputStream = new ObjectInputStream(socket.getInputStream());
      this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      String[] payload = nodeInfoForAddingNodesOnControlPanel();
      payload[0] = "add";
      Logger.info("sending node info to server");
      this.objectOutputStream.writeObject(payload);
    } catch (IOException e) {
      Logger.error("Failed to connect to server");
    }
  }

  /**
   * Returns the node information in the format: "empty-command", "nodeID", "actuatorType",
   * "actuatorID", actuatorState, "actuatorType", "actuatorID", ...
   *
   * @return the node information
   */
  private String[] nodeInfoForAddingNodesOnControlPanel() {
    String[] nodeInfo = new String[2 + node.getActuators().size() * 3];
    nodeInfo[1] = String.valueOf(socket.getLocalPort());
    int index = 2;
    for (Actuator actuator : this.node.getActuators()) {
      nodeInfo[index] = actuator.getType();
      nodeInfo[index + 1] = String.valueOf(actuator.getId());
      nodeInfo[index + 2] = String.valueOf(actuator.isOn());
      index = index + 3;
    }
    return nodeInfo;
  }

  /**
   * Processes the command received from the server.
   */
  private void processCommand() {
    try {
      String[] payload = (String[]) objectInputStream.readObject();
      String[] command = RSA.decrypt(payload, keys);
      switch (command[0]) {
        case "set":
          // Broadcast to all actuators
          if (command[1].contains("-1")) {
            Logger.info("Broadcasting set all actuators");
            node.getActuators()
                .forEach(actuator -> node.setActuator(actuator.getId(),
                                                      Boolean.parseBoolean(command[2])));
          } else {
            node.setActuator(Integer.parseInt(command[1]), Boolean.parseBoolean(command[2]));
          }
          break;
        case "toggle":
          // Broadcast to all actuators
          if (command[1].contains("-1")) {
            Logger.info("Broadcasting toggle all actuators");
            node.getActuators().forEach(actuator -> node.toggleActuator(actuator.getId()));
          } else {
            node.toggleActuator(Integer.parseInt(command[1]));
          }
          break;
        case "info":
          String[] nodeInfo = nodeInfoForAddingNodesOnControlPanel();
          nodeInfo[0] = "add";
          Logger.info("sending node info to server");
          this.setCommandQueue(nodeInfo);
          allowSendReading = true;
          break;

        default:
          Logger.info("Unknown command: " + command[0]);
      }
    } catch (RuntimeException e) {
      Logger.error(e.toString());
    } catch (SocketTimeoutException e) {
    } catch (IOException e) {
      Logger.error("Failed to read");
    } catch (ClassNotFoundException e) {
      Logger.error("Wrong type of object");
    }
  }

  /**
   * Sends a command to the server.
   * Command
   * SensorID
   * Value(s)
   */
  private void sendCommandIfExists() {
    while (this.commandQueue.peek() != null) {
      try {
        objectOutputStream.writeObject(commandQueue.poll());
      } catch (IOException e) {
        Logger.info("Failed to write to the server");
      }
    }
  }

  /**
   * Receives a command from the server.
   */
  private String[] receiveCommand() {
    try {
      return (String[]) objectInputStream.readObject();
    } catch (SocketTimeoutException e) {
      return null;
    } catch (IOException e) {
      // Logger.error("Timeout when reading command\n" + e.toString());
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      Logger.error(e.toString());
      throw new RuntimeException(e);
    }
  }

  /**
   * Closes the communication between the node and the server.
   */
  //TODO is this in use?
  private void stopCommunication() {
    try {
      this.socket.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Listens for commands from the server.
   */
  private void listenForCommands() {
    try {
      socket.setSoTimeout(Config.TIMEOUT);
      processCommand();
    } catch (IOException e) {
      Logger.error("failed to read from server");
    }
  }

  /**
   * Sets the command queue for the greenhouse node.
   *
   * @param command The command to be added to the queue
   */
  private void setCommandQueue(String[] command) {
    try {
      String[] payload = RSA.encrypt(command, keys);
      this.commandQueue.put(payload);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Listens for sensor updates.
   *
   * @param sensors A list of sensors having new values (readings)
   */
  @Override
  public void sensorsUpdated(List<Sensor> sensors) {
    String[] readings = new String[sensors.size() + 2];
    readings[0] = "data";
    readings[1] = String.valueOf(socket.getLocalPort());
    ArrayList<SensorReading> reading1 = new ArrayList<>();
    for (int i = 0; i < sensors.size(); i++) {
      readings[i + 2] = sensors.get(i).getReading().toString();
      reading1.add(sensors.get(i).getReading());
    }
    aggregateReadings.add(reading1);
    this.setCommandQueue(readings);

  }

  /**
   * Listens for node state updates.
   *
   * @param node the node which is ready now
   */
  @Override
  public void onNodeReady(SensorActuatorNode node) {
    node.addSensorListener(this);
    node.addActuatorListener(this);
    node.addCameraListener(this);
  }

  /**
   * Listens for node stop events.
   *
   * @param node the node which is stopped
   */
  @Override
  public void onNodeStopped(SensorActuatorNode node) {
    //TODO why empty?
  }

  /**
   * Updates the actuator state.
   *
   * @param nodeId   ID of the node on which this actuator is placed
   * @param actuator The actuator that has changed its state
   */
  @Override
  public void actuatorUpdated(int nodeId, Actuator actuator) {
    String[] payload = new String[4];
    payload[0] = "state";
    payload[1] = socket.getLocalPort() + ":" + actuator.getId();
    payload[2] = actuator.getType();
    payload[3] = String.valueOf(actuator.isOn());
    this.setCommandQueue(payload);
  }

  /**
   * Listens for camera updates.
   *
   * @param cameras A list of cameras having new images
   */
  @Override
  public void cameraUpdated(List<Camera> cameras) {
    String[] payload = new String[2 + cameras.size()];
    payload[0] = "camera";
    payload[1] = String.valueOf(socket.getLocalPort());
    for (int i = 0; i < cameras.size(); i++) {
      payload[i + 2] = cameras.get(i).getImage();
    }
    this.setCommandQueue(payload);
  }

  /**
   * Generates a public and private key for the greenhouse node.
   *
   * @return an array containing the private key, public key, and product of the two keys
   */
  private BigInteger[] keyGen() {
    int p = 7;
    int q = 19;
    BigInteger product = BigInteger.valueOf(p * q);
    int totient = (p - 1) * (q - 1);
    BigInteger pubKey = BigInteger.valueOf(29);
    BigInteger privKey = BigInteger.valueOf(41);
    return new BigInteger[] {privKey, pubKey, product};
  }


  /**
   * Aggregates the sensor readings and sends the average to the server.
   */
  @Override
  public void run() {
    ArrayList<SensorReading> aggregate = new ArrayList<>();
    for (SensorReading reading : aggregateReadings.get(0)) {
      aggregate.add(new SensorReading(reading.getType(), 0, reading.getUnit()));
    }
    for (ArrayList<SensorReading> readings : aggregateReadings) {
      for (int i = 0; i < readings.size(); i++) {
        aggregate.get(i).setValue(aggregate.get(i).getValue() + readings.get(i).getValue());
      }
    }
    for (SensorReading reading : aggregate) {
      reading.setValue(reading.getValue() / aggregateReadings.size());
    }
    String[] readings = new String[aggregate.size() + 2];
    readings[0] = "aggregate";
    readings[1] = String.valueOf(socket.getLocalPort());
    for (int i = 0; i < aggregate.size(); i++) {
      readings[i + 2] = aggregate.get(i).toString();
    }
    this.setCommandQueue(readings);
    aggregateReadings.clear();

  }
}
