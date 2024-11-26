package no.ntnu.run;

import no.ntnu.greenhouse.GreenhouseNode;

/**
 * Start the greenhouse node.
 */
public class GreenhouseNodeStarter {

  /**
   * Main method for the greenhouse node.
   *
   * @param args The arguments to the program.
   */
  public static void main(String[] args) {
    GreenhouseNode manager = new GreenhouseNode(1238);
    manager.initialize(args);
    manager.start();
  }
}
