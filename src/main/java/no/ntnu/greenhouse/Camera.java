package no.ntnu.greenhouse;

import no.ntnu.listeners.common.CameraListener;

/**
 * A camera that can take pictures of the environment. The camera will make impact on the
 * sensors attached to this same node.
 */
public class Camera {
  private static int nextId = 1;
  private final int id;
  private final int nodeId;
  private String image;
  private CameraListener listener;

  /**
   * Create a camera. An ID will be auto-generated.
   *
   * @param nodeId ID of the node to which this camera is connected.
   * @param image  The "image" of the camera.
   */
  public Camera(int nodeId, String image) {
    this.nodeId = nodeId;
    this.id = generateUniqueId();
    this.image = image;
  }

  /**
   * Create a camera id that is unique.
   *
   * @return A unique camera id.
   */
  private static int generateUniqueId() {
    return nextId++;
  }

  /**
   * Get the ID of the camera.
   *
   * @return the ID
   */
  public int getId() {
    return this.id;
  }

  /**
   * Get the ID of the node the camera is connected to.
   *
   * @return the ID of the node
   */
  public int getNodeId() {
    return this.nodeId;
  }

  /**
   * Get the image of the camera is connected.
   *
   * @return the image of the camera
   */
  public String getImage() {
    return this.image;
  }

  /**
   * Set the listener of the camera.
   *
   * @param listener The listener of state change events
   */
  public void setListener(CameraListener listener) {
    this.listener = listener;
  }

}
