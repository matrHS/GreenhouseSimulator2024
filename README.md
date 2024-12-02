# Project

Course project for the
course [IDATA2304 Computer communication and network programming (2024)](https://www.ntnu.edu/studies/courses/IDATA2304/2024).

Project theme: a distributed smart greenhouse application, consisting of:

* Central server
* Greenhouse nodes
  * Sensors
  * Actuators
  * Camera
* Control panel
  * Data from sensors
  * State of actuators
  * Camera feeds from nodes
  * Method of controlling actuators

  
See protocol description in [protocol.md](protocol.md).

## Getting started


There are several runnable classes in the project. Running the classes from a development enviroment is the easiest way
of getting started.

### Running from IDE

The runnable classes consist of the Server, Node and Control Panel

The server needs to be ran before any of the other nodes are started, this is done by running the "server" class main method  
[Server.java](src%2Fmain%2Fjava%2Fno%2Fntnu%2Fserver%2FServer.java)

The control panel can be started from the ControlPanelStarter class.  
[ControlPanelStarter.java](src%2Fmain%2Fjava%2Fno%2Fntnu%2Frun%2FControlPanelStarter.java)

The greenhouse nodes can be started from the GreenhouseNodeStarter class.  
[GreenhouseNodeStarter.java](src%2Fmain%2Fjava%2Fno%2Fntnu%2Frun%2FGreenhouseNodeStarter.java)  
It is important to note that the greenhouse node has command arguements that can be used to create nodes with different 
sensors and actuators. Since every IDE is different, the way of setting these arguments will be different, but there is a
default configuration for the greenhouses to instantiate nodes with a couple of each sensor and actuator type.





### Running from CMD (Compiled code)

To run the application regardless of any IDE it is important that the code is compiled first into a JAR and that the
Java Development Kit is installed on the system. For Sindows machines it is important that the JDK bin folder is included
in the Systems PATH enviroment variables so that Java can be ran from the terminal.
It is also assumed that the mvn "shade" plugin is installed to ensure that the required javafx dependencies are included.

Compilation of the source code also assumes maven is installed on the target system.
The code can be compiled and packaged to a JAR using the commands  
`
mvn clean
`  
and  
`
mvn install
`  
in the terminal or using the mvn plugin in your IDE of choice

Once the source code has been compiled and packaged, a JAR file will appear in the [target](target) folder.
This is the JAR file used to run the application in the terminal.

To start the server in the terminal the following command should be used. It is important to start the server before any other nodes.  
`
java -cp .\target\datakomm-project-2023.1.0.jar no.ntnu.server.Server
`  

To start the Greenhouse Node the following command can be uesd in another terminal instance  
`
java -cp .\target\datakomm-project-2023.1.0.jar no.ntnu.run.GreenhouseNodeStarter
`  
Commandline arguments can also be included here to define how many of each sensors and actuator should be present
on the node by using the following command.  
`
java -cp .\target\datakomm-project-2023.1.0.jar no.ntnu.run.GreenhouseNodeStarter 1 2 3 4 5 1
`  
The numbers represent the number of each sensor or actuator type following the pattern 
"temperature humidity window fan heater camera"

The control panel uses JavaFX which is why the shaded build is needed if Javafx is not installed separately.
The following command will start the control panel and the user can start as many control panels as needed.  
`
java -cp .\target\datakomm-project-2023.1.0.jar no.ntnu.run.ControlPanelStarter
`


