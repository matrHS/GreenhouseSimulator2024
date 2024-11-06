package no.ntnu.greenhouse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class GreenhouseNode {
  private final static String SERVER_HOST = "localhost";
  private int TCP_PORT = 1238;
  private ObjectInputStream objectInputStream;
  private ObjectOutputStream objectOutputStream;
  private Socket socket;

  private final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();
  private SensorActuatorNode node;



  public static void main(String[] args) {
    GreenhouseNode manager = new GreenhouseNode(1238);
    manager.initialize(args);
    manager.start();
  }

  private void start() {
    node.start();
  }

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
    try {
      Thread.sleep(4000);
      processCommand();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
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
  }


  /**
   * Initializes communication between a node and the server.
   * Each node is defined as a greenhouse consisting of multiple sensors.
   */
  private void initiateCommunication() {
    // TODO - here you can set up the TCP or UDP communication
    try {
      this.socket = new Socket(SERVER_HOST, this.TCP_PORT);
      objectInputStream = new ObjectInputStream(socket.getInputStream());
      objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      // TODO: Replace with logger
      System.out.println(e);
    }
  }

  private void processCommand() {
    String[] command = receiveCommand();
    switch (command[0]) {
      case "set":
        node.setActuator(Integer.parseInt(command[1]), Boolean.parseBoolean(command[2]));
        break;
      case "actuate":
        node.toggleActuator(Integer.parseInt(command[1]));
        break;
      default:
        System.out.println("Unknown command: " + command[0]);
    }
  }

  /**
   * Sends a command to the server.
   * Command
   * SensorID
   * Value
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
   * Receives a command from the server. TLV format.
   */
  private String[] receiveCommand() {
    try {
      return (String[]) objectInputStream.readObject();
    } catch (Exception e) {
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




}
