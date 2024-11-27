package no.ntnu.greenhouse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import no.ntnu.listeners.common.ActuatorListener;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.listeners.greenhouse.SensorListener;
import no.ntnu.tools.Config;
import no.ntnu.tools.Logger;

/**
 * The GreenhouseNode class is responsible for handling the communication between the greenhouse and
 * the server. It listens for commands from the server and sends sensor readings to the server.
 */
public class GreenhouseNode implements SensorListener, NodeStateListener, ActuatorListener {
  private final static String SERVER_HOST = "localhost";
  private final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();
  private int TCP_PORT = 1238;
  private ObjectInputStream objectInputStream;
  private ObjectOutputStream objectOutputStream;
  private Socket socket;
  private SensorActuatorNode node;

  private LinkedBlockingQueue<String[]> commandQueue;

  private boolean allowSendReading;


  /**
   * Create a greenhouse node that should connect to specified TCP port of server.
   *
   * @param TCP_PORT the TCP port of the server
   */
  public GreenhouseNode(int TCP_PORT) {
    this.TCP_PORT = TCP_PORT;
  }

  /**
   * Start the greenhouse.
   */
  public void start() {
    node.start();
    this.commandQueue = new LinkedBlockingQueue<>();
    while(!socket.isClosed()){
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
      createNode(temperature, humidity, windows, fans, heaters);
    } else {
      createNode(1, 2, 1, 0, 0);
      System.out.println("Greenhouse initialized with default sensors "
                         + "(1 temperature, 2 humidity, 1 window)");
    }
    this.node.addStateListener(this);

    initiateCommunication();
    System.out.println("Greenhouse initialized and connected");
    processCommand();

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
   */
  private void createNode(int temperature, int humidity, int windows, int fans, int heaters) {
    this.node = DeviceFactory.createNode(
        temperature, humidity, windows, fans, heaters);
    node.addSensorListener(this);

  }


  /**
   * Initializes communication between a node and the server.
   * Each node is defined as a greenhouse consisting of multiple sensors.
   */
  private void initiateCommunication() {
    // TODO - here you can set up the TCP or UDP communication
    try {
      this.socket = new Socket(SERVER_HOST, this.TCP_PORT);
      socket.setSoTimeout(Config.timeout);
      this.objectInputStream = new ObjectInputStream(socket.getInputStream());
      this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      String[] payload = nodeInfoForAddingNodesOnCPanel();
      payload[0] = "add";
      Logger.info("sending node info to server");
      this.objectOutputStream.writeObject(payload);
    } catch (IOException e) {
      Logger.error("Failed to connect to server");
    }
  }

  /**
   * Returns the node information in the format: "empty-command", "nodeID", "actuatorType",
   * "actuatorID", "actuatorType", "actuatorID", ...
   *
   * @return the node information
   */
  private String[] nodeInfoForAddingNodesOnCPanel(){
    String[] nodeInfo = new String[2 + node.getActuators().size()*3];

    nodeInfo[1] = String.valueOf(socket.getLocalPort());
    int index = 2;
    for (Actuator actuator : this.node.getActuators()) {
      nodeInfo[index] = actuator.getType();
      nodeInfo[index + 1] = String.valueOf(actuator.getId());

      nodeInfo[index + 2] = String.valueOf(actuator.isOn());
      index = index +3;

    }

    return nodeInfo;
  }

  /**
   * Processes the command received from the server.
   */
  private void processCommand() {

    try {
      String[] command = (String[]) objectInputStream.readObject();
      switch (command[0]) {
        case "set":
          node.setActuator(Integer.parseInt(command[1]), Boolean.parseBoolean(command[2]));
          break;
        case "actuate":
          node.toggleActuator(Integer.parseInt(command[1]));
          break;
        case "info":
          String[] payload = nodeInfoForAddingNodesOnCPanel();
          payload[0] = "add";
          Logger.info("sending node info to server");
          this.setCommandQueue(payload);
          allowSendReading = true;
          break;

        default:
          Logger.info("Unknown command: " + command[0]);
      }
    } catch (RuntimeException e) {
      Logger.error(e.toString());
    }catch (SocketTimeoutException e ){
    }catch (IOException e){
      Logger.error("failed to read");
    }catch (ClassNotFoundException e){
      Logger.error("wrong type of object dumbass");

    }
  }

  /**
   * Sends a command to the server.
   * Command
   * SensorID
   * Value(s)
   *
   */
  private void sendCommandIfExists(){
    while(this.commandQueue.peek() != null) {
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
    }catch (SocketTimeoutException e){
      return  null;
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
  private void stopCommunication() {
    try {
      this.socket.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void listenForCommands() {
    try {
      socket.setSoTimeout(Config.timeout);
      processCommand();
    } catch (IOException e) {
      Logger.error("failed to read from server");
    }
  }

  private void setCommandQueue(String[] command){
    try {
      this.commandQueue.put(command);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sensorsUpdated(List<Sensor> sensors) {
      String[] readings = new String[sensors.size() + 2];
      readings[0] = "data";
      readings[1] = String.valueOf(socket.getLocalPort());
      for (int i = 0; i < sensors.size(); i++) {
        readings[i + 2] = sensors.get(i).getReading().toString();
    }
      this.setCommandQueue(readings);

  }



  @Override
  public void onNodeReady(SensorActuatorNode node) {
    node.addSensorListener(this);
    node.addActuatorListener(this);
  }

  @Override
  public void onNodeStopped(SensorActuatorNode node) {

  }

  @Override
  public void actuatorUpdated(int nodeId, Actuator actuator) {
    String[] payload = new String[4];
    payload[0] = "state";
    payload[1] = socket.getLocalPort() + ":" + actuator.getId();
    payload[2] = actuator.getType();
    payload[3] = String.valueOf(actuator.isOn());
    this.setCommandQueue(payload);
  }
}