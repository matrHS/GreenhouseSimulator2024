package no.ntnu.controlpanel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ControlPanelCommunication implements CommunicationChannel {
  private final ControlPanelLogic logic;
  private ObjectInputStream inputStream;
  private ObjectOutputStream outputStream;

  private final static String SERVER_HOST = "localhost";
  private  int TCP_PORT = 1238;
  private Socket socket;


  public ControlPanelCommunication(ControlPanelLogic logic) {
    this.logic = logic;
  }

  @Override
  public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn) {

  }

  @Override
  public boolean open() {
    try {
      this.socket = new Socket(SERVER_HOST, TCP_PORT);
      this.inputStream = new ObjectInputStream(socket.getInputStream());
      this.outputStream = new ObjectOutputStream(socket.getOutputStream());
      this.outputStream.writeObject("cp");
      logic.setCommunicationChannel(this);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return true;
  }
}
