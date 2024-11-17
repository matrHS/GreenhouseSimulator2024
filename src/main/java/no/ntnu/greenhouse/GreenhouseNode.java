package no.ntnu.greenhouse;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.listeners.greenhouse.SensorListener;
import no.ntnu.tools.Logger;

public class GreenhouseNode implements SensorListener, NodeStateListener {
  private final static String SERVER_HOST = "localhost";
  private int TCP_PORT = 1238;
  private ObjectInputStream objectInputStream;
  private ObjectOutputStream objectOutputStream;
  private Socket socket;

  private final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();
  private SensorActuatorNode node;

  private boolean allowSendReading;




  /**
   * Start the greenhouse.
   */
  public void start() {
    node.start();
  }

  /**
   * Create a greenhouse node that should connect to specified TCP port of server.
   * @param TCP_PORT
   */
  public GreenhouseNode(int TCP_PORT) {
    this.TCP_PORT = TCP_PORT;
  }

  /**
   * Initializes the greenhouse.
   * Uses arguments to decide number of nodes and their properties.
   * Parsed as integers.
   *
   * @param args temperature, humidity, windows, fans, heaters
   */
  public void initialize(String[] args) {
    allowSendReading = false;

    if (argsValidator(args)) {
      int temperature = Integer.parseInt(args[0]);
      int humidity = Integer.parseInt(args[1]);
      int windows = Integer.parseInt(args[2]);
      int fans = Integer.parseInt(args[3]);
      int heaters = Integer.parseInt(args[4]);
      createNode(temperature, humidity, windows, fans, heaters);
    } else {
      createNode(1, 2, 1, 0, 0);
      System.out.println("Greenhouse initialized with default sensors " +
          "(1 temperature, 2 humidity, 1 window)");
    }

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
      } catch (NumberFormatException e) {
        System.out.println("Invalid argument: " + arg);
        valid = true;
      }
    }
    return valid;
  }


  /**
   * Create a greenhouse node with the given properties.
   *
   * @param temperature temperature sensor count
   * @param humidity humidity sensor count
   * @param windows window actuator count
   * @param fans fan actuator count
   * @param heaters heater actuator count
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
      socket.setSoTimeout(1000);
      this.objectInputStream = new ObjectInputStream(socket.getInputStream());
      this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      String[] payload = nodeInfoForAddingNodesOnCPanel();
      payload[0] = "add";
      Logger.info("sending node info to server");
      this.objectOutputStream.writeObject(payload);
    } catch (IOException e) {
      // TODO: Replace with logger
      System.out.println(e);
    }
  }

  /**
   * Returns the node information in the format: "empty-command", "nodeID", "actuatorType",
   * "actuatorID", "actuatorType", "actuatorID", ...
   * @return
   */
  private String[] nodeInfoForAddingNodesOnCPanel(){
    String[] nodeInfo = new String[2 + node.getActuators().size()*2];
    nodeInfo[1] = String.valueOf(socket.getLocalPort());
    int index = 2;
    for (Actuator actuator : this.node.getActuators()) {
      nodeInfo[index] = actuator.getType();
      nodeInfo[index + 1] = String.valueOf(actuator.getId());
      index = index +2;
    }

    return nodeInfo;
  }
  /**
   * Processes the command received from the server.
   */
  private void processCommand() {

    try {
      String[] command = receiveCommand();
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
          sendCommand(payload);
          allowSendReading = true;
          break;

        default:
          System.out.println("Unknown command: " + command[0]);
      }
    } catch (RuntimeException e) {
      Logger.error(e.toString());
    }
  }

  /**
   * Sends a command to the server.
   * Command
   * SensorID
   * Value(s)
   *
   * @param command the command to send
   */
  private void sendCommand(String[] command) {
    try {

        objectOutputStream.writeObject(command);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Receives a command from the server.
   */
  private String[] receiveCommand() {
    try {

      return (String[]) objectInputStream.readObject();
    } catch (IOException e) {
      Logger.error("Timeout when reading command\n" + e.toString());
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

  private void listenForCommands(){
    try {
      socket.setSoTimeout(4000);
      processCommand();
    } catch (IOException e) {
      Logger.error("failed to read from server");
    }
  }

  @Override
  public void sensorsUpdated(List<Sensor> sensors) {
    listenForCommands();
    if(allowSendReading) {

      String[] command = new String[sensors.size() + 2];
      command[0] = "data";
      command[1] = String.valueOf(socket.getLocalPort());
      for (int i = 0; i < sensors.size(); i++) {
        command[i + 2] = sensors.get(i).getReading().toString();
      }
      sendCommand(command);
    }
  }


  @Override
  public void onNodeReady(SensorActuatorNode node) {
    node.addSensorListener(this);
  }

  @Override
  public void onNodeStopped(SensorActuatorNode node) {

  }
}
