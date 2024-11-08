package no.ntnu.run;

import no.ntnu.greenhouse.GreenhouseNode;

public class GreenhouseNodeStarter {
  public static void main(String[] args) {
    GreenhouseNode manager = new GreenhouseNode(1238);
    manager.initialize(args);
    manager.start();
  }
}
