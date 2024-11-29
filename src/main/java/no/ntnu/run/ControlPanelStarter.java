package no.ntnu.run;

import no.ntnu.controlpanel.CommunicationChannel;
import no.ntnu.controlpanel.ControlPanelCommunication;
import no.ntnu.controlpanel.ControlPanelLogic;
import no.ntnu.gui.controlpanel.ControlPanelApplication;
import no.ntnu.tools.loggers.Logger;

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

    channel = initiateSocketCommunication(logic);

    return channel;
  }

  private CommunicationChannel initiateSocketCommunication(ControlPanelLogic logic) {
    return new ControlPanelCommunication(logic);
  }

}
