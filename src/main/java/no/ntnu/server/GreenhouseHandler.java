package no.ntnu.server;

import java.io.ObjectInputStream;
import java.net.Socket;

public class GreenhouseHandler extends Thread{
  private final Socket socket;

  private ObjectInputStream inputStream;

  public GreenhouseHandler(Socket clientSocket) {
    this.socket = clientSocket;
  }

  @Override
  public void run() {

  }

  public void testMessage() {
    System.out.println("I am greenouse: " + socket.getPort());
    try {
      inputStream = new ObjectInputStream( socket.getInputStream());
      System.out.println(inputStream.readObject().toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  public void readMessage() {
    try {

      System.out.println(inputStream.readObject().toString());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
