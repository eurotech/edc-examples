#include <time.h>
#include <stdio.h>
#include <iostream>
#include <math.h>
#include "EdcCloudClient.h"

// >>>>>> Set these variables according to your Cloud user account
#define TEST_ACCOUNT_NAME		"luca"												// Your Account name in Cloud
#define TEST_BROKER_URL			"tcp://broker-sandbox.everyware-cloud.com:1883/"	//https://console-sandbox.everyware-cloud.com/
//#define TEST_BROKER_URL     "tcp://localhost:1883"	//local

																				// URL address of broker 
#define TEST_CLIENT_ID			"001122DDEEFF"										// Unique Client ID of this client device
#define TEST_ASSET_ID			"334455AABBCC"										// Unique Asset ID of this client device
#define TEST_USERNAME			"luca_broker"									// Username in account, to use for publishing
#define TEST_PASSWORD			"We!come1"											// Password associated with Username
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
#define	DATA_SEMANTIC_TOPIC	"nativeclient/data"									// default publish topic
#define PUBLISH_PERIOD		2000												// time between published messages, in milliseconds
#define	MAX_PUBLISH			10													// number of times to publish
#define LATITUDE			38.836695											// default (simulated) GPS position
#define LONGITUDE			-99.671998
#define PUBLISH_TIMEOUT     50000L												

//helper function to create a sample payload
EdcPayload * createPayload();
//callback for message arrived
int EdcCloudClientMessageArrived(string topic, EdcPayload * payload){
	printf("Message arrived: topic=%s\r\n", topic.c_str());

	if(payload != 0){
		payload->SerializeToOstream(&cout);
	}

	return 0;
}
//callback for message delivered
int EdcCloudClientMessageDelivered(){
	return 0;
}

//callback for connection lost
int EdcCloudClientConnectionLost(char * cause){
	printf("Connection lost\r\n");
	return 0;
}

//the client instance
EdcCloudClient edcCloudClient;

int main(){

	int rc = EDCCLIENT_SUCCESS;
	string pubTSemanticTopic = DATA_SEMANTIC_TOPIC;
	string accountSemanticTopics = "#";
	string accountControlTopics = "#";

	//Create client configuration, and set its properties
    EdcConfiguration conf;    conf.setAccountName(TEST_ACCOUNT_NAME);
    conf.setBrokerUrl(TEST_BROKER_URL);
    conf.setClientId(TEST_CLIENT_ID);
    conf.setDefaultAssetId(TEST_ASSET_ID);
    conf.setUsername(TEST_USERNAME);
    conf.setPassword(TEST_PASSWORD);

	//Create device profile and set its properties
    EdcDeviceProfile prof;
	prof.setDisplayName(TEST_ASSET_ID);
	prof.setModelName("Native EDC Client");

	//set GPS position in device profile - this is sent only once, with the birth certificate
	prof.setLongitude(LONGITUDE);
	prof.setLatitude(LATITUDE);

	//Create cloud client instance
	edcCloudClient = EdcCloudClient(&conf, &prof, 
									EdcCloudClientMessageArrived, 
									EdcCloudClientMessageDelivered,
									//Don't pass a connection lost callback
									//so the client will reconnect automatically
									0);

	//Start the session.
	rc = edcCloudClient.startSession();

	if (rc != EDCCLIENT_SUCCESS){
		printf("startSession failed with error code %d\r\n", rc);
		edcCloudClient.terminate();
		return rc;
	}
	
	//subscribe to all data topics
	int qoss = 1;
	
	rc = edcCloudClient.subscribe(accountSemanticTopics, qoss);

	if (rc != EDCCLIENT_SUCCESS){
		printf("subscribe failed with error code %d\r\n", rc);
		goto exit;
	}

	//subscribe to all control topics
	
	rc = edcCloudClient.controlSubscribe(accountControlTopics, qoss);

	if (rc != EDCCLIENT_SUCCESS){
		printf("controlSubscribe failed with error code %d\r\n", rc);
		goto exit;
	}

	//publish data
	for (int i = 0; i < MAX_PUBLISH; i++) {
		rc = edcCloudClient.publish(pubTSemanticTopic, createPayload(), qoss, false, PUBLISH_TIMEOUT);	//call createPayload() each time

		if (rc != EDCCLIENT_SUCCESS){
			printf("publish #%d failed with error code %d\r\n", i, rc);
			goto exit;
		}
	}

	//sleep to allow receipt of more publishes, then terminate connection
	EdcCloudClientSleep(60 * 1000);

exit:
	rc = edcCloudClient.stopSession();
	
	if (rc != EDCCLIENT_SUCCESS){
		printf("stopSession with error code %d", rc);
	}

	edcCloudClient.terminate();

	return rc;
}

EdcPayload * createPayload(){

	static int counter = 0;
	//create payload with samples of different data types
	EdcPayload  * edcPayload = new EdcPayload();
	EdcPayload_EdcMetric * metric;

	//add a metric that changes with every loop
	counter++;

	metric = edcPayload->add_metric();
	metric->set_name("counter");
	metric->set_type(EdcPayload_EdcMetric_ValueType_INT32);
	metric->set_int_value(counter);	

	metric = edcPayload->add_metric();
	metric->set_name("str");
	metric->set_type(EdcPayload_EdcMetric_ValueType_STRING);
	metric->set_string_value("this is a string");	

	metric = edcPayload->add_metric();
	metric->set_name("int");
	metric->set_type(EdcPayload_EdcMetric_ValueType_INT32);
	metric->set_int_value(counter);	

	metric = edcPayload->add_metric();
	metric->set_name("flt");
	metric->set_type(EdcPayload_EdcMetric_ValueType_FLOAT);
	metric->set_float_value(exp((float)counter));	

	metric = edcPayload->add_metric();
	metric->set_name("dbl");
	metric->set_type(EdcPayload_EdcMetric_ValueType_DOUBLE);
	metric->set_double_value(sin((double)counter));	

	metric = edcPayload->add_metric();
	metric->set_name("long");
	metric->set_type(EdcPayload_EdcMetric_ValueType_INT64);
	metric->set_long_value(100000L);	

	metric = edcPayload->add_metric();
	metric->set_name("bool");
	metric->set_type(EdcPayload_EdcMetric_ValueType_BOOL);
	metric->set_bool_value(false);	


	unsigned char byteArray[3] = {0x31,	0x32,0x33};

	metric = edcPayload->add_metric();
	metric->set_name("arr");
	metric->set_type(EdcPayload_EdcMetric_ValueType_BYTES);
	metric->set_bytes_value(byteArray, sizeof(byteArray));	

	//use a simulated and changing GPS position

	EdcPayload_EdcPosition * position = edcPayload->mutable_position();

	//randomly vary the position
	position->set_longitude(LONGITUDE + rand() - 0.5);
	position->set_latitude(LATITUDE + rand() - 0.5);
	position->set_altitude(296);
	position->set_heading(2);
	position->set_precision(10);
	position->set_speed(60);
	position->set_satellites(3);
	position->set_status(1);

	time_t capturedOn;
	time(&capturedOn);

	position->set_timestamp(capturedOn);

	edcPayload->set_timestamp(capturedOn);

	return edcPayload;
}


