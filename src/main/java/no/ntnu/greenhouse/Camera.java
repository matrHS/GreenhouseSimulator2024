package no.ntnu.greenhouse;


public class Camera {
  private final int id;
  private final int nodeId;
  private static int nextId = 1;
  private String image;

  public Camera(int nodeId, String image){
    this.nodeId = nodeId;
    this.id = generateUniqueId();
    this.image = image;
  }

  private static int generateUniqueId() {
    return nextId++;
  }

  public int getId(){
    return this.id;
  }
  public String getImage(){
    return this.image;
  }

  public Camera getCamera(){ return this;}


  @Override
  public String toString(){
    return "Camera "+ this.id + " : "+ image.toString();
  }
}
