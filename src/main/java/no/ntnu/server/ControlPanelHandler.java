package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
        Logger.info(command.toString());
        socket.setSoTimeout(4000);
        String[] commands = (String[]) inputStream.readObject();
        server.putCommandNode(commands, Integer.parseInt(commands[1]));
      }catch(SocketTimeoutException s){
          if (this.command.get() != null && this.command.get().length > 0) {
            try {
              outputStream.writeObject(command);
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
    command.set(commands);
  }
}
