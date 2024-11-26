package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import no.ntnu.tools.Logger;

/**
 * The greenhouse handler class. This class
 * is responsible for handling the communication between the greenhouse and the server.
 */
public class GreenhouseHandler extends Thread {
  private final Socket socket;

  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  private AtomicReference<String[]> command;
  private Server server;

  /**
   * Constructor for the GreenhouseHandler.
   *
   * @param clientSocket The greenhouse client socket
   */
  public GreenhouseHandler(Socket clientSocket, ObjectOutputStream outputStream,
                           ObjectInputStream inputStream, Server server) {
    this.socket = clientSocket;
    try {
      System.out.println("I am greenouse: " + socket.getPort());
      this.outputStream = outputStream;
      this.inputStream = inputStream;
      this.command = new AtomicReference<>();
      this.server = server;
      socket.setSoTimeout(1000);

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
  }

  /**
   * Set a command to be sent to the greenhouse.
   *
   * @param commands The command to be sent
   */
  public void setCommand(String[] commands) {
    this.command.set(commands);
  }

  /**
   * Send a command to the greenhouse if it exists.
   */
  private void sendCommandIfExists() {
    if (this.command.get() != null && this.command.get().length > 0) {
      try {
        outputStream.writeObject(this.command.get());
        this.command = new AtomicReference<>();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Send a command to the greenhouse, currently only for testing actuating.
   */
  public void testSendCommand() {
    System.out.println("trying to write to : " + socket.getPort());
    String[] command = {"actuate", "2", "true"};
    try {
      Thread.sleep(4000);
      outputStream.writeObject(command);
      System.out.println("sent command");
    } catch (IOException e) {
      if (this.command.get() != null && this.command.get().length > 0) {
        Logger.info(this.command.get()[0]);
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Receive a command from the greenhouse.
   * Currently only sensor readings.
   */
  public void receiveCommand() {
    try {
      String[] command = (String[]) inputStream.readObject();
      //Logger.info(command[0]);
      server.putCommandControlPanel(command);

    } catch (SocketTimeoutException e){
      // Logger.error(e.getMessage());
    }catch (IOException e) {
        try {
          Logger.info("Attempting to close greenouse socket with port " + socket.getPort());
          socket.close();
          server.greenHouseSockets.remove(socket.getPort());
          Logger.info("Greenouse socket with port " + socket.getPort() + " closed");
        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }


}
