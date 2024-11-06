package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GreenhouseHandler extends Thread{
  private final Socket socket;

  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  public GreenhouseHandler(Socket clientSocket, ObjectOutputStream output, ObjectInputStream input) {
    this.socket = clientSocket;
    try {
      System.out.println("I am greenouse: " + socket.getPort());
      outputStream = output;
      inputStream = input;

      System.out.println(inputStream.readObject().toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public void run() {


  }

  public void testMessage() {
    System.out.println("trying to read from : " + socket.getPort());
    try {
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
