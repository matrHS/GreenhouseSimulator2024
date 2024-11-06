package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ControlPanelHandler extends Thread{

  private final Socket socket;

  private ObjectOutputStream outputStream;
  private  ObjectInputStream inputStream;


  public ControlPanelHandler(Socket clientSocket, ObjectOutputStream output, ObjectInputStream input) {
    this.socket = clientSocket;
    this.outputStream = output;
    this.inputStream = input;

  }

  @Override
  public void run() {
    try {
      inputStream.readObject();
      System.out.println( inputStream.readObject());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }
}
