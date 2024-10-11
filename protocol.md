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
* Graphical User Interface (GUI) - A graphical interface where users of the system can interact with
  it.

## The underlying transport protocol

TODO - what transport-layer protocol do you use? TCP? UDP? What port number(s)? Why did you 
choose this transport layer protocol?

TCP

## The architecture

TODO - show the general architecture of your network. Which part is a server? Who are clients? 
Do you have one or several servers? Perhaps include a picture here. 

Control Panel is the server and sensors are the Clients (Control Panels are the nodes whilst the sensors are the actors )

## The flow of information and events

TODO - describe what each network node does and when. Some periodic events? Some reaction on 
incoming packets? Perhaps split into several subsections, where each subsection describes one 
node type (For example: one subsection for sensor/actuator nodes, one for control panel nodes).

For sensor data the data should be buffered and sent in "bulks" to the server. If the temperature rises too fast the
server should be notified immediately. Sensor nodes periodically sends buffered sensor data every 30 seconds to reduce
congestion on the network. There should also be aggregate data like avg, etc. included in the sent data.

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

### Error messages

TODO - describe the possible error messages that nodes can send in your system.

If the command or state is incorrect, the server should be notified about the error. If the data is incorrectly formatted,
it should be rejected and an error should be thrown.

## An example scenario

TODO - describe a typical scenario. How would it look like from communication perspective? When 
are connections established? Which packets are sent? How do nodes react on the packets? An 
example scenario could be as follows:
1. A sensor node with ID=1 is started. It has a temperature sensor, two humidity sensors. It can
   also open a window.
2. A sensor node with ID=2 is started. It has a single temperature sensor and can control two fans
   and a heater.
3. A control panel node is started.
4. Another control panel node is started.
5. A sensor node with ID=3 is started. It has a two temperature sensors and no actuators.
6. After 5 seconds all three sensor/actuator nodes broadcast their sensor data.
7. The user of the first-control panel presses on the button "ON" for the first fan of
   sensor/actuator node with ID=2.
8. The user of the second control-panel node presses on the button "turn off all actuators".

## Reliability and security

TODO - describe the reliability and security mechanisms your solution supports.
