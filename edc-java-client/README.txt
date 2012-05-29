--------------------------------------------------------------------------------
#
# Everyware Device Cloud (EDC): Java Client Example v.2.0.0
#
--------------------------------------------------------------------------------

 Description
-------------

The Edc Java Client example simulates a device whose connection logic is written in Java
and it illustrates how devices can connect to the Everyware Cloud and publish/received data.
The client depends on the Everyware Device Cloud Java Client Library and uses the APIs
provided by that library. 

The EdcJavaClient class performs the following actions:
 
 1. Prepares the configuration
 2. Connects to the broker and start a session
 3. Subscribes to the a couple of topic
 4. Starts publishing some data and verify that the messages are also received
 5. Disconnect


   Build
-----------
This example project uses maven and maven is the preferred build tool for the project.


For maven developers
--------------------
Please follow maven instructions to download and install maven in your system (http://maven.apache.org/).
The Edc Java Client Example project comes with a pre-configured pom.xml file to build the project.
The only dependency of the project is the Everyware Device Cloud Java Client Library as stated in the pom.xml.
To build the project you can execute the following command from the project root directory;
>> mvn clean install

To open the project in eclipse, you can run the following command from the project root directory;
>> mvn eclipse:eclipse

and then import the directory as a new project in eclipse.


For non-maven developers
------------------------
The project has three dependencies:

 a. the Everyware Device Cloud Java Client Library
 b. Simple Logging Façade for Java
 c. Google Protocol Buffers Runtime Library
 
Please download the jar files for the libraries above and include them in the 
classpath of your favorite IDE/build tool to compile and run the sample code.
 