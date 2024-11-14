# Communication protocol

This document describes the protocol used for communication between the different nodes of the
distributed application.

## Terminology

* Sensor - a device which senses the environment and describes it with a value (an integer value in
  the context of this project). Examples: temperature sensor, humidity sensor.
* Actuator - a device which can influence the environment. Examples: a fan, a window opener/closer,
  door opener/closer, heater.
* Sensor and actuator node - a computer which has direct access to a set of sensors, a set of
  actuators and is connected to the Internet.
* Control-panel node - a device connected to the Internet which visualizes status of sensor and
  actuator nodes and sends control commands to them.
* Node - Represents a greenhouse where each greenhouse consists of various sensors and actuators.
* Server - Central server that handles communication between nodes and control panels.
* Graphical User Interface (GUI) - A graphical interface where users of the system can interact with
  it.

## The underlying transport protocol

TODO - what transport-layer protocol do you use? TCP? UDP? What port number(s)? Why did you 
choose this transport layer protocol?

TCP Port 1238.
TCP is used to ensure that our data is successfully transferred between the various parts of the system.

## The architecture

TODO - show the general architecture of your network. Which part is a server? Who are clients? 
Do you have one or several servers? Perhaps include a picture here. 

Control Panel is the server and sensors are the Clients (Control Panels are the nodes whilst the sensors are the actors )

Greenhouses and control panels are both Clients whilst a central server handles communication between them.


## The flow of information and events

TODO - describe what each network node does and when. Some periodic events? Some reaction on 
incoming packets? Perhaps split into several subsections, where each subsection describes one 
node type (For example: one subsection for sensor/actuator nodes, one for control panel nodes).

TODO - Expand

For sensor data the data should be buffered and sent in "bulks" to the server. If the temperature rises too fast the
server should be notified immediately. Sensor nodes periodically sends buffered sensor data every 30 seconds to reduce
congestion on the network. There should also be aggregate data like avg, etc. included in the sent data.

Sensor data from the Node is sent to the server when the sensor data is updated. 
The server updates all connected control panels with the newly acquired sensor data.

## Connection and state

TODO - is your communication protocol connection-oriented or connection-less? Is it stateful or 
stateless? 

Connection-oriented. It is a stateful communication due to the sensors holding its own data if the server disconnect 
away.

## Types, constants

TODO - Do you have some specific value types you use in several messages? They you can describe 
them here.

We plan to use an object we define

## Message format

TODO - describe the general format of all messages. Then describe specific format for each 
message type in your protocol.

The start of the message should contain commands and statuses. Due to us using objects we can use a HashMap to easily
identify the commands, status and data. This will be variable length.

Messages are structured as a list of objects where the objects are strings.
First argument is the command, second argument is the destination port along with eventual nodeId for actuators and
every argument after are context dependent on the command used.

An example of a command that sets actuator 2 on node with socket port 35124 to Open.

{"Set","35124:2","True"}


### Error messages

TODO - describe the possible error messages that nodes can send in your system.

If the command or state is incorrect, the server should be notified about the error. If the data is incorrectly formatted,
it should be rejected and an error should be thrown.

Error messages should be structured the same as normal messages however the command argument is "Error".
The address field will still be the same, but the 3rd argument will be the error message in question

## An example scenario

TODO - describe a typical scenario. How would it look like from communication perspective? When 
are connections established? Which packets are sent? How do nodes react on the packets? An 
example scenario could be as follows:


1. Server is started
2. New node connects to the server. It has a temperature sensor, two humidity sensors and a window.
   1. The window actuator gets a nodeId of 2
   2. This node gets the local socket port of 30000
3. New node connects to the server. It has a temperature sensor, two fans and a heater.
   1. First fan gets nodeId of 2
   2. Second fan gets nodeId of 3
   3. First heater gets nodeId of 4
   4. This node gets the local socket port of 30001
4. A control panel is connected to the server.
   1. The control panel gets socket 30002
5. A second control panel is connected to the server
   1. The control panel gets socket 30003
5. New node connects to server. It has two temperature sensors and no actuators.
   1. This node gets the local socket port of 30004
6. After 5 seconds, all three nodes broadcast their sensor data
   1. Server parses data from all nodes and sends them to the control panel. Commands created on server:
      1. { "Update", "30000", { "Temp", 20, "deg" }, { "Humidity", 73, "%" }, { "Humidity", 63, "%" }, { "Window", 2, True } }
      2. { "Update", "30001", { "Temp", 21, "deg" }, { "Fan", 2, False }, { "Fan", 3, True }, { "Heater", 4, False } }
      3. { "Update", "30004", { "Temp", 23, "deg" }, { "Temp", 31, "deg" } }
   2. Control panel receives the data and parses the sensor data using the socket ID to map the data to different nodes.
7. User activates first fan on node 2 from the first control panel. Fan has nodeId=2
   1. Control panel creates a command to send to the server
      1. { "Set", "30001:2", True }
   2. Server receives the command, checks the address of the node and uses the nodeId 2 from the address field to send a new command to the correct node
      1. { "Set", "30001:2", True }
   3. Node receives the command and parses the data to actuate the actuator
8. User presses "turn off all actuators" on second control panel
   1. Control panel creates a command to send to server
      1. { "Set", "all", False }
   2. Server receives command and equates address "all" to every node. Server now creates command to set every actuator to off.
      1. { "SetAll", "30000", False }
      2. { "SetAll", "30001", False }
   3. Each node receives command with "SetAll" command and iterates over its own actuators to ensure they are all set to false
9. Sensor nodes broadcast their state and all control panels are updated from server

## Reliability and security

TODO - describe the reliability and security mechanisms your solution supports.
