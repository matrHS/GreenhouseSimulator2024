package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import no.ntnu.tools.Logger;

public class ControlPanelHandler extends Thread{

  private final Socket socket;

  private ObjectOutputStream outputStream;

  private final String[] allowedCommands = new String[]{"set", "get", "add", "remove", "data", "state", "update"};

  private  ObjectInputStream inputStream;
  private AtomicReference<String[]> cmdStack;
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
          processCommandsOnStack();
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
   * TODO: rewrite this to use a queue and fix the issue of it adding empty objects.
   */

  private void processCommandsOnStack(){
      if (this.cmdStack.get() != null && this.cmdStack.get().length > 0) {
          ArrayList<String[]> commandQueue = new ArrayList<>();
          String[] allCommands = this.cmdStack.get();
          int start = 0;
          for (int i = 2; i < allCommands.length; i++) {
            for (String allowedCommand : allowedCommands) {
              if (allCommands[i].equals(allowedCommand) || i == allCommands.length - 1) {
                commandQueue.add(Arrays.copyOfRange(allCommands, start, i));
                start = i;
              }
            }
          }
          writeAllCommandsToCP(commandQueue);
      }
    }

    /**
     * Writes all processed commands in the queue to the control panel.
     *
     */

    private void writeAllCommandsToCP(ArrayList<String[]> commandQueue){
      for(String[] command : commandQueue){
        if(command != null && command.length > 0) {
          try {
            outputStream.writeObject(command);
          } catch (IOException e) {
            Logger.info("failed to write to control panels");
          }
        }
      }
      this.cmdStack = new AtomicReference<>();
    }

  /**
   * Sets the command queue by adding more and more commands at the end.
   * @param command the command to be added at the end of the queue.
   */
  public void setCmdStack(String[] command) {
    String[] queued = this.cmdStack.get();
    String[] all;
    if(queued != null){
      all = new String[queued.length + command.length];
      System.arraycopy(queued, 0, all, 0, queued.length);
      System.arraycopy(command, 0, all, queued.length, command.length);
    }else{
      all = command;
    }

    this.cmdStack.set(all);
  }
}
