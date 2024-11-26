package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import no.ntnu.tools.Logger;

/**
 * The control panel handler class. This class is responsible for handling the communication between
 * the control panel and the server.
 */
public class ControlPanelHandler extends Thread {

  private final Socket socket;
  private final String[] allowedCommands = new String[] {"set", "get", "add", "remove", "data",
                                                         "state", "update"};
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;
  private AtomicReference<String[]> cmdStack;

  private LinkedBlockingQueue<String[]> commandQueue;
  private Server server;

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
  }

  /**
   * The main run method of this handler.
   */
  @Override
  @SuppressWarnings("InfiniteLoopStatement")
  public void run() {
    while (!socket.isClosed()) {
      try {
        socket.setSoTimeout(1000);
        String[] commands = (String[]) inputStream.readObject();
        int id;
        if (commands[0].equals("set")) {
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
        try {
          Logger.info("Attempting to close control panel socket with port " + socket.getPort());
          socket.close();
          server.controlPanels.remove(socket.getPort());
          Logger.info("Control panel socket with port " + socket.getPort() + " closed");
        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
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
      sendCommandToCP(commandQueue.poll());
    }
  }

  /**
   * Writes all processed commands in the queue to the control panel.
   */
  private void sendCommandToCP(String[] command) {
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
