package no.ntnu.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import no.ntnu.tools.Logger;

public class Server extends Thread{

  private HashMap<Integer, Socket> greenHouseSockets;

  static final int TCP_PORT = 0;
  private HashMap<Integer, Socket> controlPanels;
  private ServerSocket serverSocket;

  public static void main(String[] args){
    Server server = new Server();
    server.run();
  }

  public Server(HashMap<Integer, Socket> greenHouses){
    greenHouseSockets = greenHouses;
    controlPanels = new HashMap<>();

    try {
      serverSocket = new ServerSocket(TCP_PORT);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Server(){
    controlPanels = new HashMap<>();

    try {
      serverSocket = new ServerSocket(TCP_PORT);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int init(){
    return serverSocket.getLocalPort();
  }

  /**
   * Run the server, and handle the client.
   */
  public void run(){
    System.out.println("server starting");
    ServerSocket severSocket = openListeningPort();
    if (severSocket == null){
      return;
    }
    System.out.println("Running on port: " + serverSocket.getLocalPort());

    while(true){
      Socket socket = acceptNextClient();
      System.out.println("Connected to: " + socket.getPort());
      GreenhouseHandler handler = new GreenhouseHandler(socket);
      handler.start();


      System.out.println("Connected to: " + socket.getPort());
    }
  }


  /**
   * Open a listening port
   *
   * @return The server socket
   */
    private ServerSocket openListeningPort(){
      ServerSocket listeningSocket =null;
      try{
        listeningSocket = new ServerSocket(TCP_PORT);
      } catch(IOException e){
        System.out.println("Could not open listening socket: "+e.getMessage());
      }
      return listeningSocket;
    }

    /**
     * Accept the next client
     *
     * @return The client socket
     */
    private Socket acceptNextClient(){
      Socket socket = null;
      try{
        socket = serverSocket.accept();
        controlPanels.put(socket.getPort(), socket);
      } catch(IOException e){
        System.out.println("Could not accept the next client: "+e.getMessage());
      }
      return socket;
    }


}
