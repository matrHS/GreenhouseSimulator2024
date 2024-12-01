# Project

Course project for the
course [IDATA2304 Computer communication and network programming (2023)](https://www.ntnu.edu/studies/courses/IDATA2304/2023).

Project theme: a distributed smart greenhouse application, consisting of:

* Sensor-actuator nodes
* Visualization nodes

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

Compilation of the source code also assumes maven is installed on the target system.
The code can be compiled and packaged to a JAR using the command
`
mvn package
`
in the terminal or using the mvn plugin in your IDE of choice

Once the source code has been compiled and packaged, a JAR file will appear in the targets folder with the date of 
compilation. This is the JAR file used to run the application in the terminal. For the commands in this document, 
the commands will use the path to a JAR compiled on the 1st of december 2023, however if you compile and package the solution,
the date will be different meaning the path needs to be changed.

To start the server in the terminal the following command is ran. It is important to start the server before any other nodes.  
`
java -cp .\target\datakomm-project-2023.1.0.jar no.ntnu.server.Server
`  

To start the Greenhouse Node the following command can be ran in another terminal instance  
`
java -cp .\target\datakomm-project-2023.1.0.jar no.ntnu.run.GreenhouseNodeStarter
`  
Commandline arguments can also be included here to define how many of each sensors and actuator should be present
on the node by using the following command.  
`
java -cp .\target\datakomm-project-2023.1.0.jar no.ntnu.run.GreenhouseNodeStarter 1 2 3 4 5 1
`  
The numbers represents the number of each sensor or actuator type following the pattern 
"temperature humidity window fan heater camera"

The control panel uses JavaFX and can be started in 2 different ways.  
Using maven, it can be started using  
`
mvn javafx:run
`


|
To run the greenhouse part (with sensor/actuator nodes):

* Command line version: run the `main` method inside `CommandLineGreenhouse` class.
* GUI version: run the `main` method inside `GreenhouseGuiStarter` class. Note - if you run the
  `GreenhouseApplication` class directly, JavaFX will complain that it can't find necessary modules.

To run the control panel (only GUI-version is available): run the `main` method inside the
`ControlPanelStarter` class

## Simulating events

If you want to simulate fake communication (just some periodic events happening), you can run
both the greenhouse and control panel parts with a command line parameter `fake`. Check out
classes in the [`no.ntnu.run` package](src/main/java/no/ntnu/run) for more details. 