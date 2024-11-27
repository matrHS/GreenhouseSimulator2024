package no.ntnu.greenhouse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.tools.Logger;

/**
 * Application entrypoint - a simulator for a greenhouse.
 */
public class GreenhouseSimulator {
  private final static String SERVER_HOST = "localhost";
  private final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();
  private final List<PeriodicSwitch> periodicSwitches = new LinkedList<>();
  private final boolean fake;
  private int TCP_PORT = 1238;
  private ObjectInputStream objectInputStream;
  private ObjectOutputStream objectOutputStream;
  private Socket socket;
  private GreenhouseNode greenhouseNode;

  /**
   * Create a greenhouse simulator.
   *
   * @param fake When true, simulate a fake periodic events instead of creating
   *             socket communication
   */
  public GreenhouseSimulator(boolean fake) {
    this.fake = fake;
  }

  public GreenhouseSimulator(int tcp) {
    TCP_PORT = tcp;
    this.fake = false;
  }

  /**
   * Initialise the greenhouse but don't start the simulation just yet.
   */
  public void initialize() {
    this.greenhouseNode = new GreenhouseNode(TCP_PORT);
    this.greenhouseNode.initialize(new String[] {"1", "2", "1", "0", "0"});

    Logger.info("Greenhouse initialized");
  }

  // TODO: Refactor into separate NODE class. Separate all node functionality to its own class
  private void createNode(int temperature, int humidity, int windows, int fans, int heaters,
                          int cameras) {
    initiateCommunication();
    SensorActuatorNode node = DeviceFactory.createNode(
        temperature, humidity, windows, fans, heaters, cameras);
    nodes.put(node.getId(), node);
  }

  /**
   * Start a simulation of a greenhouse - all the sensor and actuator nodes inside it.
   */
  public void start() {

    this.greenhouseNode.start();

    Logger.info("Simulator started");
  }

  private void initiateCommunication() {
    if (fake) {
      initiateFakePeriodicSwitches();
    } else {
      initiateRealCommunication();
    }
  }

  /**
   * Initializes communication between a node and the server.
   * Each node is defined as a greenhouse consisting of multiple sensors.
   */
  private void initiateRealCommunication() {
    // TODO - here you can set up the TCP or UDP communication
    try {
      this.socket = new Socket(SERVER_HOST, this.TCP_PORT);
      objectInputStream = new ObjectInputStream(socket.getInputStream());

      System.out.println("Connection Established");
      objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      objectOutputStream.writeObject("Hello from the client");
      objectOutputStream.writeObject("Hello from the client again");
    } catch (IOException e) {
      // TODO: Replace with logger
      System.out.println(e);
    }
  }

  private void initiateFakePeriodicSwitches() {
    periodicSwitches.add(new PeriodicSwitch("Window DJ", nodes.get(1), 2, 20000));
    periodicSwitches.add(new PeriodicSwitch("Heater DJ", nodes.get(2), 7, 8000));
  }

  /**
   * Stop the simulation of the greenhouse - all the nodes in it.
   */
  public void stop() {
    stopCommunication();
    for (SensorActuatorNode node : nodes.values()) {
      node.stop();
    }
  }

  private void stopCommunication() {
    if (fake) {
      for (PeriodicSwitch periodicSwitch : periodicSwitches) {
        periodicSwitch.stop();
      }
    } else {
      // TODO - here you stop the TCP/UDP communication
      try {
        this.socket.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Add a listener for notification of node staring and stopping.
   *
   * @param listener The listener which will receive notifications
   */
  public void subscribeToLifecycleUpdates(NodeStateListener listener) {
    for (SensorActuatorNode node : nodes.values()) {
      node.addStateListener(listener);
    }
  }

  public Map<Integer, SensorActuatorNode> getNodes() {
    return nodes;
  }
}
