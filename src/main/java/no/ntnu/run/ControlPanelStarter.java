package no.ntnu.run;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelCommunication;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.controlpanel.FakeCommunicationChannel;
import no.ntnu.gui.controlpanel.ControlPanelApplication;
import no.ntnu.tools.Logger;

/**
 * Starter class for the control panel.
 * Note: we could launch the Application class directly, but then we would have issues with the
 * debugger (JavaFX modules not found)
 */
public class ControlPanelStarter {
  private final boolean fake;

  public ControlPanelStarter(boolean fake) {
    this.fake = fake;
  }

  /**
   * Entrypoint for the application.
   *
   * @param args Command line arguments, only the first one of them used: when it is "fake",
   *             emulate fake events, when it is either something else or not present,
   *             use real socket communication. Go to Run → Edit Configurations.
   *             Add "fake" to the Program Arguments field.
   *             Apply the changes.
   */
  public static void main(String[] args) {
    boolean fake = false; // make it true to test in fake mode
    if (args.length == 1 && "fake".equals(args[0])) {
      fake = true;
      Logger.info("Using FAKE events");
    }
    ControlPanelStarter starter = new ControlPanelStarter(fake);
    starter.start();
  }

  /**
   * Start the control panel application.
   */
  public void start() {
    ControlPanelLogic logic = new ControlPanelLogic();
    CommunicationChannel channel = initiateCommunication(logic, fake);
    ControlPanelApplication.startApp(logic, channel);

  }

  private CommunicationChannel initiateCommunication(ControlPanelLogic logic, boolean fake) {
    CommunicationChannel channel;
    if (fake) {
      channel = initiateFakeSpawner(logic);
    } else {
      channel = initiateSocketCommunication(logic);
    }
    return channel;
  }

  private CommunicationChannel initiateSocketCommunication(ControlPanelLogic logic) {
    return new ControlPanelCommunication(logic);
  }

  private CommunicationChannel initiateFakeSpawner(ControlPanelLogic logic) {
    // Here we pretend that some events will be received with a given delay
    FakeCommunicationChannel spawner = new FakeCommunicationChannel(logic);
    logic.setCommunicationChannel(spawner);
    final int startDelay = 5;
    spawner.spawnNode("4;3_window", startDelay);
    spawner.spawnNode("1", startDelay + 1);
    spawner.spawnNode("1", startDelay + 2);
    spawner.advertiseSensorData("4;temperature=27.4 °C,temperature=26.8 °C,humidity=80 %",
                                startDelay + 2);
    spawner.spawnNode("8;2_heater", startDelay + 3);
    spawner.advertiseActuatorState(4, 1, true, startDelay + 3);
    spawner.advertiseActuatorState(4, 1, false, startDelay + 4);
    spawner.advertiseActuatorState(4, 1, true, startDelay + 5);
    spawner.advertiseActuatorState(4, 2, true, startDelay + 5);
    spawner.advertiseActuatorState(4, 1, false, startDelay + 6);
    spawner.advertiseActuatorState(4, 2, false, startDelay + 6);
    spawner.advertiseActuatorState(4, 1, true, startDelay + 7);
    spawner.advertiseActuatorState(4, 2, true, startDelay + 8);
    spawner.advertiseSensorData("4;temperature=22.4 °C,temperature=26.0 °C,humidity=81 %",
                                startDelay + 9);
    spawner.advertiseSensorData("1;humidity=80 %,humidity=82 %", startDelay + 10);
    spawner.advertiseRemovedNode(8, startDelay + 11);
    spawner.advertiseRemovedNode(8, startDelay + 12);
    spawner.advertiseSensorData("1;temperature=25.4 °C,temperature=27.0 °C,humidity=67 %",
                                startDelay + 13);
    spawner.advertiseSensorData("4;temperature=25.4 °C,temperature=27.0 °C,humidity=82 %",
                                startDelay + 14);
    spawner.advertiseSensorData("4;temperature=25.4 °C,temperature=27.0 °C,humidity=82 %",
                                startDelay + 16);
    return spawner;
  }
}
