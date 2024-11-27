package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import no.ntnu.tools.Logger;

/**
 * The server class.
 */
public class Server {
  static final int TCP_PORT = 1238;
  protected HashMap<Integer, GreenhouseHandler> greenHouseSockets;
  protected HashMap<Integer, ControlPanelHandler> controlPanels;
  private ServerSocket serverSocket;

  private HashMap<String, String[]> latestReading;

  /**
   * Constructor for the server.
   */
  public Server() {
    controlPanels = new HashMap<>();
    greenHouseSockets = new HashMap<>();
    serverSocket = openListeningPort();
    latestReading = new HashMap<>();
  }

  /**
   * Main method for the server.
   */
  public static void main(String[] args) {
    Server server = new Server();
    server.run();
  }

  /**
   * Put a command on the command queue for the greenhouse node.
   *
   * @param commands The commands to put on the queue
   * @param id       The id of the greenhouse node to send the command to.
   */
  public void putCommandNode(String[] commands, int id) {
    if (id == -1) {
      greenHouseSockets.forEach((k, v) -> v.setCommand(commands));
    } else if (greenHouseSockets.containsKey(id)) {
      greenHouseSockets.get(id).setCommand(commands);
    }
  }

  /**
   * Put a command on the command queue for the control panel.
   *
   * @param commands The commands to put on the queue
   */
  public void putCommandControlPanel(String[] commands) {
    if (controlPanels.isEmpty()) {
      latestReading.put(commands[1], commands);
    } else {
      controlPanels.forEach((k, v) -> v.putOnQueue(commands));

    }
  }

  public void closeSocket(HashMap map, Socket socket){
    try {
      Logger.info("Attempting to close greenouse socket with port " + socket.getPort());
      socket.close();
      map.remove(socket.getPort());
      Logger.info("Greenouse socket with port " + socket.getPort() + " closed");
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public HashMap getCPMap(){
    return this.controlPanels;
  }

  public HashMap getNodeMap(){
    return this.greenHouseSockets;
  }
  /**
   * Run the server, and handle the client.
   */
  @SuppressWarnings("all")
  public void run() {
    Logger.info("server starting");
    Logger.info("Running on port: " + serverSocket.getLocalPort());
    while (true) {

      Socket socket = acceptNextClient();
      Logger.info("Connected to: " + socket.getPort());
      Logger.info("holding sockets for: " + greenHouseSockets.keySet() + " and "
                  + controlPanels.keySet());
    }
  }

  /**
   * Open a listening port
   *
   * @return The server socket
   */
  private ServerSocket openListeningPort() {
    ServerSocket listeningSocket = null;
    try {
      listeningSocket = new ServerSocket(TCP_PORT);
    } catch (IOException e) {
      System.out.println("Could not open listening socket: " + e.getMessage());
    }
    return listeningSocket;
  }

  /**
   * Accept the next client.
   *
   * @return The client socket
   */
  private Socket acceptNextClient() {
    Socket socket = null;
    try {
      socket = serverSocket.accept();

      ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
      ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
      Object obj = inputStream.readObject();
      String type = (obj instanceof String) ? obj.toString() : "fake";

      if (type.equals("cp")) {
        ControlPanelHandler handler = new ControlPanelHandler(socket, outputStream, inputStream,
                                                              this);
        controlPanels.put(socket.getPort(), handler);
        handler.start();
        Logger.info("new control panel connected");
        this.putCommandNode(new String[] {"info"}, -1);
      } else {

        GreenhouseHandler handler = new GreenhouseHandler(socket, outputStream, inputStream, this);
        greenHouseSockets.put(socket.getPort(), handler);
        handler.start();
        if (!controlPanels.isEmpty()) {
          this.putCommandNode(new String[] {"info"}, -1);
        }
      }
    } catch (IOException e) {
      System.out.println("Could not accept the next client: " + e.getMessage());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return socket;
  }


}
