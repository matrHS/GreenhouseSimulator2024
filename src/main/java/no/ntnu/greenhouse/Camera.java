package no.ntnu.greenhouse;

import no.ntnu.listeners.common.CameraListener;

/**
 * A camera that can take pictures of the environment. The camera will make impact on the
 * sensors attached to this same node.
 */
public class Camera {
  private final int id;
  private final int nodeId;
  private static int nextId = 1;
  private String image;
  private CameraListener listener;

  /**
   * Create a camera. An ID will be auto-generated.
   *
   * @param nodeId ID of the node to which this camera is connected.
   * @param image The "image" of the camera.
   */
  public Camera(int nodeId, String image){
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
  public int getId(){
    return this.id;
  }

  /**
   * Get the image of the camera is connected.
   *
   * @return the image of the camera
   */
  public String getImage(){
    return this.image;
  }

  public void setListener(CameraListener listener) {
    this.listener = listener;
  }

}
