package no.ntnu.run;

import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.server.Server;
import no.ntnu.tools.Logger;

/**
 * Run a greenhouse simulation using command-line interface (no GUI).
 */
public class CommandLineGreenhouse {
  /**
   * Application entrypoint for the command-line version of the simulator.
   *
   * @param args Command line arguments, only the first one of them used: when it is "fake",
   *             emulate fake events, when it is either something else or not present,
   *             use real socket communication.
   */
  public static void main(String[] args) {
    Logger.info("Running greenhouse simulator in command line (without GUI)...");
    boolean fake = false;
    if (args.length == 1 && "fake".equals(args[0])) {
      fake = true;
      Logger.info("Using FAKE events");
    }

    Server server = new Server();
    int tcp = server.init();
    server.start();

    GreenhouseSimulator simulator = new GreenhouseSimulator(tcp);
    simulator.initialize();
    simulator.start();

    System.out.println("here");
  }
}
