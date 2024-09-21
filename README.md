# Dividing-tasks-among-nodes-IoT

## Introduction
Project for major assignment 1.

+ Data is calculated at nodes B and C. Node A performs data retrieval after being calculated from the nodes and synthesizes the results and displays them on the monitor for users to grasp the situation according to real data.

## Processing

+ Node A acts as a Manager Node.

+ Nodes B and C act as Worker nodes.

Every time data is calculated by nodes B and C - simulated by generating random data after 5 seconds. Node B and C register with Node A so that every time there is data, a message will be sent to Node A and Node A will synthesize and calculate the data received from nodes B and C. 