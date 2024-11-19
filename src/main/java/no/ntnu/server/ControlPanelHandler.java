package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import no.ntnu.tools.Logger;

public class ControlPanelHandler extends Thread{

  private final Socket socket;

  private ObjectOutputStream outputStream;

  private final String[] allowedCommands = new String[]{"set", "get", "add", "remove", "data", "state", "update"};

  private  ObjectInputStream inputStream;
  private AtomicReference<String[]> cmdStack;

  private LinkedBlockingQueue<String[]> commandQueue;
  private Server server;

  /**
   * Constructor for the control panel handler.
   * @param clientSocket The socket for the control panel.
   * @param output the object outputstream for the socket.
   * @param input the object inputstream for the socket.
   * @param server The server this handler belongs to.
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
  public void run() {
      while (true) {
        try{
        socket.setSoTimeout(4000);
        String[] commands = (String[]) inputStream.readObject();
        server.putCommandNode(commands, Integer.parseInt(commands[1]));
      }catch(SocketTimeoutException s){
         processNextQueuedElement();
        }catch(IOException e){
          throw new RuntimeException(e);
        } catch(ClassNotFoundException e){
          throw new RuntimeException(e);
      }
      }
    }

  /**
   * Processes all the commands on the atomic stack and then sends each command sequentially to the
   * control panel.
   *
   */
  private void processNextQueuedElement(){
    while(!commandQueue.isEmpty()){
         sendCommandToCP(commandQueue.poll());
    }
  }

  /**
   * Writes all processed commands in the queue to the control panel.
   *
   */
  private void sendCommandToCP(String[] command){
    try {
      outputStream.writeObject(command);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  public void putOnQueue(String[] command){
    try {
      this.commandQueue.put(command);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
