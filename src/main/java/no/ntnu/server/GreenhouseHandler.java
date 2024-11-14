package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;
import no.ntnu.tools.Logger;

public class GreenhouseHandler extends Thread {
  private final Socket socket;

  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  private AtomicReference<String[]> command;
  private Server server;

  /**
   * Constructor for the GreenhouseHandler
   *
   * @param clientSocket The greenhouse client socket
   */
  public GreenhouseHandler(Socket clientSocket, ObjectOutputStream outputStream, ObjectInputStream inputStream, Server server) {
    this.socket = clientSocket;
    try {
      System.out.println("I am greenouse: " + socket.getPort());
      this.outputStream = outputStream;
      this.inputStream = inputStream;
      this.command = new AtomicReference<>();
      this.server = server;
      socket.setSoTimeout(4000);

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
      testReceiveCommand();
      sendCommandIfExists();
    }
  }

  public void setCommand(String[] commands ){
    this.command.set(commands);
  }

  private void sendCommandIfExists() {
    if (this.command.get() != null && this.command.get().length > 0) {
      try {
        this.command.get()[1] = "2";
        outputStream.writeObject(this.command.get());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Send a command to the greenhouse, currently only for testing actuating
   */
  public void testSendCommand() {
    System.out.println("trying to write to : " + socket.getPort());
    String[] command = {"actuate", "2", "true"};
    try {
      Thread.sleep(4000);
      outputStream.writeObject(command);
      System.out.println("sent command");
    } catch (IOException e) {
      if(this.command.get() != null && this.command.get().length > 0)
      {
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
  public void testReceiveCommand() {
    try {
      String[] command = (String[]) inputStream.readObject();
      server.putCommandControlPanel(command);

    } catch (IOException e) {
      Logger.error("Timeout when reading command\n" + e.toString());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }


}
