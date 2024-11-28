package no.ntnu.listeners.common;

import java.util.List;
import no.ntnu.greenhouse.Camera;

/**
 * Listener for camera sensor update events.
 * This will (probably) be usable only on the camera/sensor/actuator node (greenhouse) side, where the
 * real sensor objects are available. The control panel side has only camera sensor reading values
 * available, not the camera sensors themselves.
 */
public interface CameraListener {

  /**
   * An event that is fired every time camera sensor values are updated.
   *
   * @param cameras A list of camera having new values (readings)
   */
  void cameraUpdated(List<Camera> cameras);
}
