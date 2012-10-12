
#include <time.h>
#include <stdio.h>
#include <iostream>
#include <math.h>
#include "EdcCloudClient.h"

#if defined _WIN32_WCE
#include "..\os\wince\wcelibcex-1.0\src\wce_time.h"
#undef time
#define time wceex_time
#endif

// >>>>>> Set these variables according to your Cloud user account
#define TEST_ACCOUNT_NAME		"myEdcAccount"	// Your Account name in Cloud
#define TEST_BROKER_URL		"tcp://broker-sandbox.everyware-cloud.com:1883/"		// URL address of broker
//#define TEST_BROKER_URL     	"tcp://localhost:1883"	//local

#define TEST_CLIENT_ID		"001122DDEEFF"		// Unique Client ID of this client device
#define TEST_ASSET_ID		"334455AABBCC"		// Unique Asset ID of this client device
#define TEST_USERNAME		"myEdcUserName_broker" // Username in account, to use for publishing
#define TEST_PASSWORD		"myEdcPassword3#"	// Password associated with Username
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
#define	DATA_SEMANTIC_TOPIC		"nativeclient/data"		// default publish topic
#define PUBLISH_PERIOD		10000			// time between published messages, in milliseconds
#define	MAX_PUBLISH		10			// number of times to publish
#define LATITUDE			46.369079			// default (simulated) GPS position
#define LONGITUDE			13.076729
#define PUBLISH_TIMEOUT		50000L			

#define DISPLAY_TX_MSG_PAYLOAD	
#define DISPLAY_RX_MSG_PAYLOAD	

#if defined _WIN32_WCE
CRITICAL_SECTION cs;
#endif

//helper function to create a sample payload
EdcPayload * createPayload();

//helper function to display a payload
bool displayPayload (EdcPayload * payload);

//callback for message arrived
int EdcCloudClientMessageArrived(string topic, EdcPayload * payload){
	static int rxMsgCount = 0;
	rxMsgCount ++;
#if defined _WIN32_WCE
	EnterCriticalSection(&cs);
#endif
	printf("Message #%d arrived: topic=%s\r\n", rxMsgCount, topic.c_str());
	
#ifdef DISPLAY_RX_MSG_PAYLOAD
	if (payload != 0){
		displayPayload(payload);
	}
#endif

#if defined _WIN32_WCE
	LeaveCriticalSection(&cs);
#endif
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

#if defined _WIN32_WCE
int _tmain(int argc, _TCHAR* argv[]){
#else
int main(){
#endif

	printf ("EDC test start\r\n");

#if defined _WIN32_WCE
	InitializeCriticalSection(&cs);
#endif

	int rc = EDCCLIENT_SUCCESS;
	string pubTSemanticTopic = DATA_SEMANTIC_TOPIC;
	string accountSemanticTopics = "#";
	string accountControlTopics = "#";

	//Create client configuration, and set its properties
    EdcConfiguration conf;    
	conf.setAccountName(TEST_ACCOUNT_NAME);
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
	printf ("Start session...\r\n");
	rc = edcCloudClient.startSession();

	if (rc != EDCCLIENT_SUCCESS) {

		printf("startSession failed with error code %d\r\n", rc);

		switch (rc)
		{
		// MQTT client errors (<0):
		case MQTTCLIENT_FAILURE:
			printf ("MQTT client, generic operation failure\r\n");
			break;
		case MQTTCLIENT_PERSISTENCE_ERROR:
			printf ("MQTT client, persistence error\r\n");
			break;
		case MQTTCLIENT_DISCONNECTED:
			printf ("MQTT client disconnected\r\n");
			break;
		case MQTTCLIENT_MAX_MESSAGES_INFLIGHT:
			printf ("MQTT client, maximum number of in-flight messages has been reached\r\n");
			break;
		case MQTTCLIENT_BAD_UTF8_STRING:
			printf ("MQTT client, invalid UTF-8 string\r\n");
			break;
		case MQTTCLIENT_NULL_PARAMETER:
			printf ("MQTT client, NULL parameter not allowed\r\n");
			break;
		case MQTTCLIENT_TOPICNAME_TRUNCATED:
			printf ("MQTT client, topic name truncated\r\n");
			break;
		case MQTTCLIENT_BAD_STRUCTURE:
			printf ("MQTT client, invalid structure\r\n");
			break;
		// CONNACK message errors:
		case 1:
			printf ("Connection to MQTT broker refused, unacceptable protocol version\r\n");
			break;
		case 2:
			printf ("Connection to MQTT broker refused, identifier rejected\r\n");
			break;
		case 3:
			printf ("Connection to MQTT broker refused, server unavailable\r\n");
			break;
		case 4:
			printf ("Connection to MQTT broker refused, bad username or password\r\n");
			break;
		case 5:
			printf ("Connection to MQTT broker refused, not authorized\r\n");
			break;
		default:
			printf ("Unknown error\r\n");
			break;
		}

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

	if (rc != EDCCLIENT_SUCCESS) {
		printf("controlSubscribe failed with error code %d\r\n", rc);
		goto exit;
	}

	//publish data
	for (int i = 0; i < MAX_PUBLISH; i++) {

#if defined _WIN32_WCE
		EnterCriticalSection(&cs);
#endif
		EdcPayload * payload = createPayload();

		rc = edcCloudClient.publish(pubTSemanticTopic, payload, qoss, false, PUBLISH_TIMEOUT);	//call createPayload() each time

		if (rc != EDCCLIENT_SUCCESS) {
			printf("Publish #%d failed with error code %d\r\n", i, rc);
			delete payload;
			goto exit;
		}

		printf("Publish #%d succeeded, semantic topic=%s\r\n", i+1, pubTSemanticTopic.c_str());

#ifdef DISPLAY_TX_MSG_PAYLOAD

		if (payload != 0){
			displayPayload(payload);
		}
#endif

#if defined _WIN32_WCE
		LeaveCriticalSection(&cs);
#endif

		delete payload;
		EdcCloudClientSleep(1 * 1000);
	}

	//sleep to allow receipt of more publishes, then terminate connection
	EdcCloudClientSleep(60 * 1000);

exit:
	rc = edcCloudClient.stopSession();
	
	if (rc != EDCCLIENT_SUCCESS){
		printf("stopSession with error code %d", rc);
	}

	edcCloudClient.terminate();
	printf ("EDC test completed\r\n");
#if defined _WIN32_WCE
	DeleteCriticalSection(&cs);
#endif
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
	metric->set_float_value((float)exp((float)counter));	

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

bool displayPayload (EdcPayload * payload)
{
	if (payload == 0)
		return false;

	if (payload->has_timestamp())
		printf ("  timestamp: %lld\r\n", payload->timestamp());

	if (payload->has_position()) {

		const EdcPayload_EdcPosition position = payload->position();

		printf ("  position latitude: %f\r\n", position.latitude());
		printf ("  position longitude: %f\r\n", position.longitude());

		if (position.has_altitude())
			printf ("  position altitude: %f\r\n", position.altitude());
	
		if (position.has_precision())
			printf ("  position precision: %f\r\n", position.precision());

		if (position.has_heading())
			printf ("  position heading: %f\r\n", position.heading());

		if (position.has_speed())
			printf ("  position speed: %f\r\n", position.speed());

		if (position.has_timestamp())
			printf ("  position timestamp: %lld\r\n", position.timestamp());

		if (position.has_satellites())
			printf ("  position satellites: %d\r\n", position.satellites());

		if (position.has_status())
			printf ("  position status: %d\r\n", position.status());
	}

	for (int i=0; i<payload->metric_size(); i++) {

		EdcPayload_EdcMetric metrictmp = payload->metric(i);

		printf ("  metric #%d name: %s\r\n", i, metrictmp.name().c_str());
		printf ("  metric #%d type: %d\r\n", i, metrictmp.type());

		if (metrictmp.has_double_value())
			printf ("  metric #%d double_value: %f\r\n", i, metrictmp.double_value());

		if (metrictmp.has_float_value())
				printf ("  metric #%d float_value: %f\r\n", i, metrictmp.float_value());

		if (metrictmp.has_long_value())
			printf ("  metric #%d long_value: %lld\r\n", i, metrictmp.long_value());

		if (metrictmp.has_int_value())
			printf ("  metric #%d int_value: %d\r\n", i, metrictmp.int_value());

		if (metrictmp.has_bool_value())
			printf ("  metric #%d bool_value: %d\r\n", i, metrictmp.bool_value());

		if (metrictmp.has_string_value())
			printf ("  metric #%d string_value: %s\r\n", i, metrictmp.string_value().c_str());

		if (metrictmp.has_bytes_value()) {
			printf ("  metric #%d bytes_value:", i);
			
			for (int j=0; j<(int)(metrictmp.bytes_value().length()); j++)
				printf (" 0x%02x", metrictmp.bytes_value().data()[j]);

			printf ("\r\n");
		}
	}
	
	if (payload->has_body()) {

		printf ("  body:");
			
		for (int i=0; i<(int)(payload->body().length()); i++)
			printf (" 0x%02x", payload->body().data()[i]);

		printf ("\n");
	}

	return true;
}

