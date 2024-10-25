package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ControlPanelHandler extends Thread{

  private final Socket socket;

  private  ObjectInputStream inputStream;


  public ControlPanelHandler(Socket clientSocket) {
    this.socket = clientSocket;
  }

  @Override
  public void run() {
    try {
      inputStream = new ObjectInputStream( socket.getInputStream());
      inputStream.readObject();

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }
}
