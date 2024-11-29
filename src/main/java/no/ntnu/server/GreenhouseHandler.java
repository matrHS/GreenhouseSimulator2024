package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import no.ntnu.tools.Config;
import no.ntnu.tools.loggers.ServerLogger;

/**
 * The greenhouse handler class. This class
 * is responsible for handling the communication between the greenhouse and the server.
 */
public class GreenhouseHandler extends Thread {
  private final Socket socket;
  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;
  private LinkedBlockingQueue<String[]> commandQueue;
  private Server server;
  private int socketAddress;

  private ServerLogger logger = ServerLogger.getInstance();

  /**
   * Constructor for the GreenhouseHandler.
   *
   * @param clientSocket The greenhouse client socket
   */

  public GreenhouseHandler(Socket clientSocket, ObjectOutputStream outputStream,
                           ObjectInputStream inputStream, Server server) {
    this.socket = clientSocket;
    try {
      logger.info("I am greenouse: " + socket.getPort());
      this.outputStream = outputStream;
      this.inputStream = inputStream;
      this.commandQueue = new LinkedBlockingQueue<>();
      this.server = server;
      this.socketAddress = socket.getPort();
      socket.setSoTimeout(Config.TIMEOUT);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Starts thread for greenhouse handler connection and continuously listens for commands.
   */
  @Override
  public void run() {
    while (!socket.isClosed()) {
      receiveCommand();
      sendCommandIfExists();
    }
    server.putCommandControlPanel(new String[] {"remove", Integer.toString(socketAddress)});
  }

  /**
   * Set a command to be sent to the control panel.
   *
   * @param commands The command to be sent.
   */
  public void setCommand(String[] commands) {
    try {
      this.commandQueue.put(commands);
    } catch (InterruptedException e) {
      logger.error("failed to put command on command queue from node to cp");
    }
  }

  /**
   * Send a command to the greenhouse if it exists.
   */
  private void sendCommandIfExists() {
    if (this.commandQueue.peek() != null) {
      try {
        outputStream.writeObject(this.commandQueue.poll());
      } catch (IOException e) {
        logger.error("Failed to write a command to greenhouse");
      }
    }
  }

  /**
   * Receive a command from the greenhouse.
   * Currently only sensor readings.
   */
  public void receiveCommand() {
    try {
      String[] command = (String[]) inputStream.readObject();
      server.putCommandControlPanel(command);
    } catch (SocketTimeoutException e) {
      logger.info("Timeout");
    } catch (IOException e) {
      server.closeSocket(server.getNodeMap(), this.socket);
    } catch (ClassNotFoundException e) {
      logger.info("Wrong message format or class");
    }
  }


}
