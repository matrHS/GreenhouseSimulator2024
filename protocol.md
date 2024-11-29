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

<!--TODO - what transport-layer protocol do you use? TCP? UDP? What port number(s)? Why did you 
choose this transport layer protocol? -->

The underlying transport-layer protocol is TCP, this ensures a reliable information transfer. It is important that the
greenhouse information arrives before the node readings and actuator state information, so that the elements of the GUI
can be built and handle the information. Without TCP we would not be able to ensure reliable and ordered transfer of
information. The main server in our application uses TCP or 1238, whilst socket connections use dynamically
assigned ports at the time of connection.

## The architecture

<!--TODO - show the general architecture of your network. Which part is a server? Who are clients? 
Do you have one or several servers? Perhaps include a picture here. -->

This protocol treats each greenhouse as a separate node with multiple sensors and actuators connected to it. The server
is able to handle a large amount of greenhouses. The greenhouses interfaces with a handler that is contained on a
singular
discrete server. From the server, commands are sent to and from the greenhouse nodes and control panels. control panels
are connected to the server similarly to the greenhouse nodes. Both control panels and greenhouses are uniquely
indetified on the server by their local socket port, since it's not possible for two sockets to use the same port. 
Disconnected ports are also removed so in the edge case of the same port being used after another closed, 
they are not mistaken for each other.

![ServerDiagramWhiteBG.png](images%2FServerDiagramWhiteBG.png)
## The flow of information and events

<!--TODO - describe what each network node does and when. Some periodic events? Some reaction on 
incoming packets? Perhaps split into several subsections, where each subsection describes one 
node type (For example: one subsection for sensor/actuator nodes, one for control panel nodes). -->

The server is the central hub of the network. It handles all incoming and outgoing messages from the sensor nodes and
control panels. The server is responsible for sending the sensor data to the control panels and sending the actuator
commands to the greenhouse nodes. 
The server also handles the connection and disconnection of the sensor nodes and control panels.  
When a sensor node
is connected, any sensor changes queues up a command containing sensor data to be sent to the server. The server then
sends this data to the control panels where the control panel parses all the sensor and actuator data into a graphical
interface.  
When a control panel is connected, the server asks all nodes to send an information package containing all the sensor
and actuator data needed to build a "base" structure for the sensors. The control panel will then wait to receive 
sensor data from the server. If a user toggles or sets an actuator from the control panel, the control panel sends a
command to the server with identification to what actuator or actuators to change.

![ServerFlowChart.drawio.png](images%2FServerFlowChart.drawio.png)
## Connection and state

<!-- TODO - is your communication protocol connection-oriented or connection-less? Is it stateful or 
stateless? -->
The application uses a connection-oriented protocol as nodes and control panels will connect to the server and stay
connected until they disconnect. The server is stateful as it keeps track of all connected nodes and control panels, 
however once a node or control panel disconnects, the server will remove the connection from its list of connected.  


## Types, constants

<!--TODO - Do you have some specific value types you use in several messages? They you can describe 
them here. --> 
The config class contains all the current constants, which are the socket timeout timer, the server TCP port and
address. All messages are structured as string arrays.

## Message format

<!--TODO - describe the general format of all messages. Then describe specific format for each 
message type in your protocol. -->

Commands and information sent over the TCP socket is sent as a string array, where the first position is the
specific command being sent and the second position is the node id and optionally actuator ID. This means this slot
functions as both a destination and source address. When sending from the control panel, second slot identifies the
target for actions to be performed on, while when receiving information from the greenhouse, this slot is used to
identify which node the information is sent from. In the case of actuators being toggled or set, the actuator ID is
appended to the node id with a colon separating them. This slot can also be used to broadcast, in which case it is set
to -1 for broadcasting to all greenhouses. The actuator ID can also be used for multicasting by setting the actuator ID
to -1.  
The server does not process the commands, it only forwards them to the correct node or control panel.

From slot 3 and onward is the information actually being transmitted. This part can contain a variety information and
is also end-to-end encrypted using a rudimentary RSA encryption.

**Current usable commands are:**   
Greenhouse Node commands: "add", "remove", "data", "state", "aggregate", "camera"  
Control panel commands: "set", "toggle", "info"

**command structures**:  
adding a node to the server.  
{"add", "nodeId", "actuatorType", "actuatorId", "actuatorState", "actuatorType" ... "actuatorState"}  
here the third slot is the type of actuator, the fourth slot its ID and the fifth slot its state, then the pattern
repeats


Removing a node from the server.  
{"remove", "nodeId"}
Used to remove the node from the control panel if the connection is closed.

Sending reading data to the control panel.  
{"data", "nodeId", "sensorType=type, value=value, unit=unit",...., "sensorType=type, value=value, unit=unit"}   
This method of transmitting information works slightly differently, instead of each value being in its own string,
the entire string contains alle the information for a given sensor.

Changing the state of an actuator on the control panel.  
{"state", "nodeId:actuatorId", "actuatorType","actuatorState"}  
Used to reflect changes in the actuator state on the control panel so it matches the actual state on the node.

60 second aggregate sensor data
{"aggregate", "nodeId", "sensorType=type, value=value, unit=unit",...., "sensorType=type, value=value, unit=unit"}  
Node calculates and sends the average of all sensor data for the last 60 seconds to the control panels.

Transfer camera feed to control panel.  
{"camera", "nodeId", "imageData"}  
Used to transfer camera feed from the node to the control panel.

Setting the state of an actuator in a greenhouse:  
{"set", "nodeId:actuatorId","desiredState"}  
Sets given node to desired state

Toggling the state of an actuator in a greenhouse:  
{"toggle", "nodeId:actuatorId"}
Sets actuator to the opposite of its current state.

getting all information from all nodes (sent by the server whenever a control panel connects)  
{"info","broadcastCode"}  
The general purpose broadcast code is set to -1 as it is not possible for a socket connection to have a port number of
-1


**Command examples:**   
Setting the second actuator on node 35124 to be on.  
{"Set","35124:2","True"}

Getting all info of all nodes:  
{"info", "-1"}

Adding a node to the control panel:  
{"add", "23423","window", "1", "true", "fan", "2", "false"}

Multicast toggle actuators to specific node:  
{"toggle", "35124:-1"}

Broadcast toggle to all actuators:  
{"toggle", "-1:-1"}

### Error messages

<!--TODO - describe the possible error messages that nodes can send in your system. -->

If a command or state is from the greenhouse node to the server is incorrect, the server will create a log entry and
discard the message. If the command or state form the server to the greenhouse is incorrect, the greenhouse will create a
log entry and discard the message. If the command or state from the control panel to the server is incorrect, the server
will create a log entry and discard the message. If the command or state from the server to the control panel is
incorrect, the control panel will create a log entry and discard the message.


## An example scenario

<!-- TODO - describe a typical scenario. How would it look like from communication perspective? When 
are connections established? Which packets are sent? How do nodes react on the packets? An 
example scenario could be as follows:-->

1. Server is started
2. New node connects to the server. It has a temperature sensor, two humidity sensors and a window.
    1. The window actuator gets an id of 2
    2. This node gets the local socket port of 30000
    3. The local socket port is saved on the server as the unique identifier for the socket and node.
3. New node connects to the server. It has a temperature sensor, two fans and a heater.
    1. First fan gets id of 2
    2. Second fan gets id of 3
    3. First heater gets id of 4
    4. This node gets the local socket port of 30001
    5. The local socket port is saved on the server as the unique identifier for the socket and node
4. A control panel is connected to the server.
    1. The control panel gets socket 30002
    2. The control panel socket is saved on the server as the unique identifier.
    3. The server informs all currently connected nodes that a new control panel is connected.
    4. The node info is sent to the control panel and is displayed on the control panel for the user.
5. New node connects to server. It has two temperature sensors and no actuators.
    1. This node gets the local socket port of 30004
    2. Server notifies the node that a control panel exists and asks it to transfer node information.
6. After 5 seconds, all three nodes broadcast their sensor data
    1. Server parses data from all nodes and sends them to the control panel. Commands created on server:
        1. { "Update", "30000", { "Temp", 20, "deg" }, { "Humidity", 73, "%" }, { "Humidity", 63, "%" }, { "Window", 2,
           True } }
        2. { "Update", "30001", { "Temp", 21, "deg" }, { "Fan", 2, False }, { "Fan", 3, True }, { "Heater", 4, False } }
        3. { "Update", "30004", { "Temp", 23, "deg" }, { "Temp", 31, "deg" } }
    2. Control panel receives the data and parses the sensor data using the socket ID to map the data to different
       nodes.
7. User activates first fan on node 2 from the first control panel. Fan has id=2
    1. Control panel creates a command to send to the server
        1. { "Set", "30001:2", True }
    2. Server receives the command, checks the address of the node and uses the id 2 from the address field to send a
       new command to the correct node
        1. { "Set", "30001:2", True }
    3. Node receives the command and parses the data to actuate the actuator
8. User presses "turn off all actuators" on second control panel
    1. Control panel creates a command to send to server
        1. { "Set", "-1:-1", False }
    2. Server receives command and equates address "-1" to every node. Server now creates command to set every actuator
       to off.
        1. { "Set", "30000:-1", False }
        2. { "Set", "30001:-1", False }
    3. Each node receives command with "Set" command and equates "-1" to "set" all actuators to on this node to False
9. Sensor nodes broadcast their state and all control panels are updated from server.

## Reliability and security

<!-- TODO - describe the reliability and security mechanisms your solution supports. -->

All commands are queued up when they are created meaning no commands should be "forgotten" or skipped. There is a command
queue for each node and control panel to ensure all commands are processed. All messages are end-to-end encrypted using
a rudimentary RSA encryption the command and address headers are not encrypted. 
The server does not process the commands, it only forwards them to the correct node or control panel. This means that
the server is not vulnerable to any attacks that could be caused by processing the commands. As the server only forwards
the commands, it is not possible to read the contents of the commands. The server does not store any information about
the nodes or control panels, except for the socket port.
