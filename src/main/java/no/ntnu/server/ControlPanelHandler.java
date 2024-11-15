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

  private  ObjectInputStream inputStream;
  private AtomicReference<String[]> command;
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
    this.command = new AtomicReference<>();


  }

  @Override
  public void run() {
      while (true) {
        try{
       // Logger.info(command.toString());
        socket.setSoTimeout(4000);
        String[] commands = (String[]) inputStream.readObject();
        server.putCommandNode(commands, Integer.parseInt(commands[1]));
      }catch(SocketTimeoutException s){
          if (this.command.get() != null && this.command.get().length > 0) {
            try {
              Logger.info(Arrays.toString(this.command.get()));
              String[] allowedCommands = new String[]{"set", "get", "add", "remove", "data", "state", "update"};
              ArrayList<String[]> commandQueue = new ArrayList<>();
              String[] allCommands = this.command.get();
              int start = 0;
              for (int i = 2; i < allCommands.length; i++) {
                for (int j = 0; j < allowedCommands.length; j++) {
                  if (allCommands[i].equals(allowedCommands[j]) || i == allCommands.length - 1){
                    commandQueue.add(Arrays.copyOfRange(allCommands, start, i));
                    start = i;
                  }
                }
              }
              for(String[] command : commandQueue){
                if(command != null && command.length > 0) {
                  outputStream.writeObject(command);
                }
              }
              this.command = new AtomicReference<>();
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }}catch(IOException e){
          throw new RuntimeException(e);
        } catch(ClassNotFoundException e){
          throw new RuntimeException(e);
      }
      }
    }

  public void setCommand(String[] commands) {
    String[] queued = this.command.get();
    String[] all;
    if(queued != null){
      all = new String[queued.length + commands.length];
      System.arraycopy(queued, 0, all, 0, queued.length);
      System.arraycopy(commands, 0, all, queued.length, commands.length);
    }else{
      all = commands;
    }

    this.command.set(all);
  }
}
