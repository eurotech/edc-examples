--------------------------------------------------------------------------------
#
# Everyware Device Cloud (EDC): REST Endpoint Example v.2.0.0
#
--------------------------------------------------------------------------------

 Description
-------------

The Edc REST Endpoint Example illustrates how to setup a REST Endpoint 
to receive callbacks from the rules in the Everyware Device Cloud.
The example is divided into two parts.

1. EdcRestEndpoint uses the Jersey implementation of the JAX-RS APIs to set up a 
REST end point that response to try calls:
 a. GET call on the /edc-rest-endpoint/notify
 b. POST call on the /edc-rest-endpoint/notify
 c. GET call on the /edc-rest-endpoint/report
 
The first two are used as target URLs for the REST Rule Action of the Everyware Cloud.
In the GET case, the rule parameters are supplied as parameters in the URL query string.
In the POST case, the rule parameters are posted in the payload as a serialized EdcPayload object.
Whenever a notification is received, either via GET or POST, it is stored in memory.
The /edc-rest-endpoint/report call can be used to report all the notification rceived so far.

2. EdcRestRuleExample is a standalone java program which, using the Everyware Cloud
REST APIs, connects to your sandbox account, creates a could of Rules - one
with a REST Rule Action using the GET method and one using the POST method -
and posts a message which will trigger both rules.

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
The Edc REST Endpoint Example project comes with a pre-configured pom.xml file to build the project.
As stated in the pom.xml, the only dependencies of the project are the Jersey Client Library and 
the generated model binding classes.
To build the project you can execute the following command from the project root directory;
>> mvn clean install

To open the project in eclipse, you can run the following command from the project root directory;
>> mvn eclipse:eclipse

and then import the directory as a new project in eclipse.


For non-maven developers
------------------------
The project has three dependencies:

 a. the Jersey Client Library downloadable from http://jersey.java.net/
 b. the generated model binding classes downloadable from https://api-sandbox.everyware-cloud.com/
 c. the Everyware Device Cloud Java Client Library and dependencies
 d. Simple Logging Façade for Java
 e. Google Protocol Buffers Runtime Library
 
Please download the jar files for the libraries above and include them in the 
classpath of your favorite IDE/build tool to compile and run the sample code.
 
 
To run the project you need to do the following:

1. Run maven install to produce the edc-rest-endpoint.war file and deploy to your app server
2. Configure the EdcRestRuleExample.java with your account information and the URL of your deployed endpoint
3. Compile and run EdcRestRuleExample
4. Check the notification received at the following URL:
http://<your-host>/edc-rest-endpoint/notify/report


