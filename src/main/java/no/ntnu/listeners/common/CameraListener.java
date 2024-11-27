package no.ntnu.listeners.common;

import java.util.List;
import no.ntnu.greenhouse.Camera;

/**
 * Listener for sensor update events.
 * This will (probably) be usable only on the sensor/actuator node (greenhouse) side, where the
 * real sensor objects are available. The control panel side has only sensor reading values
 * available, not the sensors themselves.
 */
public interface CameraListener {

  /**
   * An event that is fired every time sensor values are updated.
   *
   * @param cameras A list of sensors having new values (readings)
   */
  void cameraUpdated(List<Camera> cameras);
}
