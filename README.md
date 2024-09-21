# Dividing-tasks-among-nodes-IoT

## Project for major assignment 1 + 2.

+ Data is calculated at nodes B and C. Node A performs data retrieval after being calculated from the nodes and synthesizes the results and displays them on the monitor for users to grasp the situation according to real data. => In real IoT devices, sensors can be used in child nodes to collect data and a manager node will process the results to take action or monitor events in real time.

To get more information in the this pages: https://recipes.onem2m.org/introduction/Introduction-to-oneM2M-Architecture/ 

## Sum nodes in project 1
+ System including: Node A - Manager Node and Node B, C - Worker nodes.

+ Every time data is calculated by nodes B and C - simulated by generating random data after 5 seconds. Node B and C register with Node A so that every time there is data, a message will be sent to Node A and Node A will synthesize and calculate the data received from nodes B and C. 

## Processing task and monitor IOT devices in project 2

+ System including: Manager node, Pumactuator, Humunity sensor and temperature sensor.

+ whenever the temperature sensor detects a temperature greater than 22 degrees and huminity sensor lower than 70, the manager node will sent a message to activate the pump actuator turn on the pump.