package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import no.ntnu.tools.Config;
import no.ntnu.tools.Logger;

/**
 * The control panel handler class. This class is responsible for handling the communication between
 * the control panel and the server.
 */
public class ControlPanelHandler extends Thread {
  private final Socket socket;
  private final String[] allowedCommands = new String[] {"set", "get", "add", "remove", "data",
                                                         "state", "update", "camera"};
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;
  private AtomicReference<String[]> cmdStack;
  private LinkedBlockingQueue<String[]> commandQueue;
  private Server server;
  private int socketAddress;

  /**
   * Constructor for the control panel handler.
   *
   * @param clientSocket The socket for the control panel.
   * @param output       the object outputstream for the socket.
   * @param input        the object inputstream for the socket.
   * @param server       The server this handler belongs to.
   */

  public ControlPanelHandler(Socket clientSocket, ObjectOutputStream output,
                             ObjectInputStream input, Server server) {
    this.socket = clientSocket;
    this.outputStream = output;
    this.inputStream = input;
    this.server = server;
    this.cmdStack = new AtomicReference<>();
    this.commandQueue = new LinkedBlockingQueue<>();
    this.socketAddress = socket.getPort();
  }

  /**
   * The main run method of this handler.
   */
  @Override
  public void run() {

    while (!socket.isClosed()) {
      try {
        socket.setSoTimeout(Config.TIMEOUT);
        String[] commands = (String[]) inputStream.readObject();
        int id;
        if (commands[0].equals("set") || commands[0].equals("toggle")) {
          String[] ids = commands[1].split(":");
          commands[1] = ids[1];
          id = Integer.parseInt(ids[0]);
        } else {
          id = Integer.parseInt(commands[1]);
        }
        server.putCommandNode(commands, id);
      } catch (SocketTimeoutException s) {
        processNextQueuedElement();
      } catch (IOException e) {
        server.closeSocket(server.getCpMap(), this.socket);
      } catch (ClassNotFoundException e) {
        Logger.error(e.toString());
      }
    }
  }

  /**
   * Processes all the commands on the atomic stack and then sends each command sequentially to the
   * control panel.
   */
  private void processNextQueuedElement() {
    while (!commandQueue.isEmpty()) {
      sendCommandToCp(commandQueue.poll());
    }
  }

  /**
   * Writes all processed commands in the queue to the control panel.
   */
  private void sendCommandToCp(String[] command) {
    try {
      outputStream.writeObject(command);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Puts a command on the queue for the control panel.
   *
   * @param command The command to put on the queue.
   */
  public void putOnQueue(String[] command) {
    try {
      this.commandQueue.put(command);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
