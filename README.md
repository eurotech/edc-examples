edc-examples
============

##EDC Example Projects##

###cleanweb-dashboard###
This is an example 'dashboard' that shows how to use the REST APIs of Everyware Cloud to retrive information and display it in a meaningful and specific way.  It shows how to include simple maps, widgets, and other features and can be used as a starting point for a custom portal view.

###edc-c-client###
This is the Everyware Cloud 'C' client reference implementation.  It is written in both C and C++.  Internally it utilizes the Eclipse Paho C MQTT client and includes some additional features to make it more compatible with Everyware Cloud such as payload abstraction.

###edc-comet###
This example shows how to utilize the Everyware Cloud comet or 'real time channel' for real time updates of data as they flow into Everyware Cloud and are then streamed over this comet channel.

###edc-comet-gps###
This is very similar to the edc-comet example but shows data via the real time channel being updated on a map.

###edc-google-charts-dashboard###
This example shows some simple things you can do with Google Charts in terms of rendering data that is retrieved from Everyware Cloud.

###edc-java-client###
This is an example that shows how to use the Everware Cloud Java client.  It establishes a connection to Everyware Cloud, publishes emulated data, and also subscribes to some specific MQTT topics.  It has modifiable parameters that can be adjusted based on your specific EC credentials.

###edc-java-client-gps###
This example is similar to the edc-java-client but includes publishing of GPS data.

###edc-rest###
This is a Java based example for utilizing the Everyware Cloud REST APIs.  It includes both fetching data from Everyware Cloud as well as pushing data into Everyware Cloud.

###edc-rest-endpoint###
This is an example for creating a REST endpoint that can be triggered via Everyware Cloud Rule Actions.  So, this REST endpoint gets 'called' when a rule is triggered in Everyware Cloud based on the rule definition.  See the README file in the edc-rest-endpoint project for addtional information.
