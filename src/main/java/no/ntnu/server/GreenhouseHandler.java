package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GreenhouseHandler extends Thread{
  private final Socket socket;

  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  public GreenhouseHandler(Socket clientSocket) {
    this.socket = clientSocket;
    try {
      System.out.println("I am greenouse: " + socket.getPort());
      outputStream = new ObjectOutputStream(socket.getOutputStream());
      inputStream = new ObjectInputStream( socket.getInputStream());
      testSendCommand();


    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public void run() {


  }

  public void testSendCommand() {
    System.out.println("trying to write to : " + socket.getPort());
    String[] command = {"set", "2", "true"};
    try {
      outputStream.writeObject(command);
      System.out.println("sent command");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
