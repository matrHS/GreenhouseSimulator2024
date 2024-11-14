package no.ntnu.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server extends Thread {
  static final int TCP_PORT = 1238;
  private HashMap<Integer, GreenhouseHandler> greenHouseSockets;
  private HashMap<Integer, ControlPanelHandler> controlPanels;
  private ServerSocket serverSocket;

  public static void main(String[] args){
    Server server = new Server();
    server.run();
  }

  public Server(HashMap<Integer, Socket> greenHouses){
    controlPanels = new HashMap<>();
    serverSocket = openListeningPort();
  }

  public Server(){
    controlPanels = new HashMap<>();
    greenHouseSockets = new HashMap<>();
    serverSocket = openListeningPort();


  }

  public void putCommandNode(String[] commands, int Id) {
    if(greenHouseSockets.containsKey(Id)) {
      greenHouseSockets.get(Id).setCommand(commands);
    }
  }
  public void putCommandControlPanel(String[] commands) {
      controlPanels.forEach((k,v) -> v.setCommand(commands));
  }

  public int init(){
    return serverSocket.getLocalPort();
  }

  /**
   * Run the server, and handle the client.
   */
  @Override
  public void run(){
    System.out.println("server starting");
    System.out.println("Running on port: " + serverSocket.getLocalPort());
    while(true){

      Socket socket = acceptNextClient();
      System.out.println("Connected to: " + socket.getPort());
      System.out.println("holding sockets for: " + greenHouseSockets.keySet() + " and "
          + controlPanels.keySet());



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

        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        Object obj = inputStream.readObject();
        String type = (obj instanceof String) ? obj.toString() : "fake";

        if(type.equals("cp")){
          ControlPanelHandler handler = new ControlPanelHandler(socket,outputStream, inputStream, this);
          controlPanels.put(socket.getPort(), handler);
          handler.start();
          System.out.println("new control panel connected");
        }else{

          GreenhouseHandler handler = new GreenhouseHandler(socket, outputStream, inputStream, this);
          greenHouseSockets.put(socket.getPort(),handler);
          handler.start();
          System.out.println(handler.isAlive());
        }
      } catch(IOException e){
        System.out.println("Could not accept the next client: "+e.getMessage());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
      return socket;
    }



}
