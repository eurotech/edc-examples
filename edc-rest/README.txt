--------------------------------------------------------------------------------
#
# Everyware Device Cloud (EDC): REST APIs Example v.2.0.0
#
--------------------------------------------------------------------------------

 Description
-------------

The Edc REST Example illustrates how to use the REST APIs exposed by the Everyware Device Cloud.

The example walks through several of the APIs offered by the platform including:
 - showing the account information
 - listing the devices connected under the account
 - listing all topic which received data under the account
 - listing the metrics which received data for a given topic
 - listing messages for a given topic
 - create a rule which sends email alters and stores messages in the data store
 - triggers the above rule through a message publishing
 - verifies the rules triggered
 
While any REST client can be used to invoke the Everyware Device Cloud APIs,
the code presented in this example leverages the Jersey REST Client library. 

More information on the Everyware Device Cloud REST APIs can be found at:
https://api-sandbox.everyware-cloud.com/


   Build
-----------
This example project uses maven and maven is the preferred build tool for the project.


For maven developers
--------------------
Please follow maven instructions to download and install maven in your system (http://maven.apache.org/).
The Edc REST Example project comes with a pre-configured pom.xml file to build the project.
As stated in the pom.xml, the only dependencies of the project are the Jersey Client Library and 
the generated model binding classes.

First configure the Eclipse workspace;
>> mvn -Declipse.workspace=/your/eclipse/workspace eclipse:configure-workspace

To build the project you can execute the following command from the project root directory;
>> mvn clean install

To open the project in eclipse, you can run the following command from the project root directory;
>> mvn eclipse:eclipse

and then import the directory as a new project in eclipse.

Note: with some Maven packages you might need to specify the Java compliance level.
This can be achieved by setting a couple of system properties on the command line, e.g.:
>> mvn -Dmaven.compiler.source=1.6 -Dmaven.compiler.target=1.6 clean install


For non-maven developers
------------------------
The project has three dependencies:

 a. the Jersey Client Library downloadable from http://jersey.java.net/
 b. the generated model binding classes downloadable from https://api-sandbox.everyware-cloud.com/
 
Please download the jar files for the libraries above and include them in the 
classpath of your favorite IDE/build tool to compile and run the sample code.
 