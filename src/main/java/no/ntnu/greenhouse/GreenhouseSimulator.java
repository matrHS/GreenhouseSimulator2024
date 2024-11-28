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
import no.ntnu.tools.Config;
import no.ntnu.tools.Logger;

/**
 * Application entrypoint - a simulator for a greenhouse.
 */
public class GreenhouseSimulator {
  private final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();
  private final List<PeriodicSwitch> periodicSwitches = new LinkedList<>();
  private final boolean fake;
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

  /**
   * Create a greenhouse simulator.
   */
  public GreenhouseSimulator() {
    this.fake = false;
  }

  /**
   * Initialise the greenhouse but don't start the simulation just yet.
   */
  public void initialize() {
    this.greenhouseNode = new GreenhouseNode();
    this.greenhouseNode.initialize(new String[] {"1", "2", "1", "0", "0"});

    Logger.info("Greenhouse initialized");
  }

  /**
   * Create a greenhouse with a specific configuration.
   *
   * @param temperature The temperature of the greenhouse
   * @param humidity    The humidity of the greenhouse
   * @param windows     The number of windows in the greenhouse
   * @param fans        The number of fans in the greenhouse
   * @param heaters     The number of heaters in the greenhouse
   * @param cameras     The number of cameras in the greenhouse
   */
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

  /**
   * Initialize communication between the greenhouse and the server.
   */
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
    try {
      this.socket = new Socket(Config.SERVER_ADDRESS, Config.SERVER_PORT);
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

  /**
   * Initialize fake periodic switches.
   */
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

  /**
   * Stop the communication between the greenhouse and the server.
   */
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

}
