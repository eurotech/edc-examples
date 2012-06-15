#ifndef _EDCCLOUDCLIENT_H
#define _EDCCLOUDCLIENT_H

#include "EdcCloudHelpers.h"
#include "edcpayload.pb.h"
extern "C"{
#include "MQTTClient.h"
}
#include <list>

//For OS specific stuff
#ifdef WIN32
#include <windows.h>
#else
#include <unistd.h>
#ifndef _countof
#define _countof(a) (sizeof((a))/sizeof((a)[0]))
#endif /* _countof */
#endif

using namespace std;
using namespace edcdatatypes;

#define EDCCLIENT_SUCCESS MQTTCLIENT_SUCCESS

typedef int (pfnEdcCloudClientMessageArrived)(string topic, EdcPayload * payload);
typedef int (pfnEdcCloudClientMessageDelivered)();
typedef int (pfnEdcCloudClientConnectionLost)(char* cause);

//Currently the only OS specific function
void EdcCloudClientSleep(unsigned long milliseconds){
#if defined(WIN32)
	Sleep(milliseconds);
#else	
	usleep(milliseconds*1000);
#endif
}

//hard-coded params for now
#define EDCCLIENT_PUBLISH_TIMEOUT	50000L
#define EDCCLIENT_QOS				1
#define EDCCLIENT_RETAIN			false
#define EDCCLIENT_RECONNECT_TIMEOUT	1000

const string EdcBirthPayloadNames[] = {
	"uptime",
	"display_name",
	"model_name",
	"model_id",
	"part_number",
	"serial_number",
	"firmware_version",
	"bios_version",
	"os_version",
	"jvm_name",
	"jvm_version",
	"jvm_profile",
	"connection_interface",
	"connection_ip",
};

const string EdcBirthPayloadValues[] = {
	"123",
	"Windows client",
	"test model_name",
	"test model_id",
	"test part_number",
	"test serial_number",
	"test firmware_version",
	"test bios_version",
	"Windows XP SP3",
	"test jvm_name",
	"test jvm_version",
	"test jvm_profile",
	"test connection_interface",
	"test connection_ip",
};

class EdcCloudClient{

private:
	/**
	 * MQtt client handle
	 */
	MQTTClient							m_MQTTClient;
	/**
	 * EDC client configuration
	 */
	EdcConfiguration					* m_edcConfiguration;
	/**
	 * EDC device profile
	 */
	EdcDeviceProfile					* m_edcDeviceProfile;
	/**
	 * The application can provide a callback function
	 * that the EDC client will invoke if the broker 
	 * delivers a message
	 */
	pfnEdcCloudClientMessageArrived		* m_messageArrived;
	/**
	 * The application can provide a callback function
	 * that the EDC client will invoke if the client
	 * delivers a message
	 */
	pfnEdcCloudClientMessageDelivered	* m_messageDelivered;
	/**
	 * The application can provide a callback function
	 * that the EDC client will invoke if the connection to 
	 * the broker is lost
	 */
	pfnEdcCloudClientConnectionLost		* m_connectionLost;
	/**
	 * Flag representing the EDC client (the MQtt client actually)
	 * connection status to the broker
	 */
	bool							m_Connected;
	/**
	 * List of the subscriptions (fulltopic and qos) requested 
	 * to the client by the application.
	 */
	list<EdcSubscription>			m_subscriptions;
	/**
	 * The EDC client provides a callback function
	 * that the MQtt client will invoke if the broker 
	 * delivers a message. The EDC client will invoke the
	 * m_messageArrived callback if setup by the application
	 *
	 * The prototype of the callback is defined by the MQtt
	 * client code
	 *
	 *
	 * @param context Application-specific context, in this 
	 *                implementation we pass 'this' as a reference of the 
	 *                EDC client instance. 
	 * @param topicName The topic of the message received
	 * @param message A structure representing the message received
	 *        Only the payload is considered
	 * @return 
	 */
	 static int messageArrived(void* context, char* topicName, int topicLen, MQTTClient_message* message){
		
		EdcCloudClient *edcCloudClient = (EdcCloudClient *)context;

		EdcPayload * payload = new EdcPayload();

		if(!payload->ParseFromArray(message->payload, message->payloadlen)){
			delete payload;
			payload = NULL;
		}

		if(edcCloudClient->m_messageArrived){
			edcCloudClient->m_messageArrived(string(topicName), payload);
		}

		return 1;
	}
	/**
	 * The EDC client provides a callback function
	 * that the MQtt client will invoke when it 
	 * delivers a message. The EDC client will invoke the
	 * m_messageDelivered callback if setup by the application
	 *
	 * The prototype of the callback is defined by the MQtt
	 * client code
	 *
	 *
	 * @param context Application-specific context, in this 
	 *                implementation we pass 'this' as a reference of the 
	 *                EDC client instance. 
	 * @param topicName The topic of the message received
	 * @param dt
	 * @return 
	 */
	 static void messageDelivered(void* context, MQTTClient_deliveryToken dt){
		
		EdcCloudClient *edcCloudClient = (EdcCloudClient *)context;

		if(edcCloudClient->m_messageDelivered){
			edcCloudClient->m_messageDelivered();
		}
	}
	/**
	 * The EDC client provides a callback function
	 * that the MQtt client will invoke when the connection 
	 * to the broker is lost. The EDC client will invoke the
	 * m_connectionLost callback if setup by the application.
	 * If no callback is setupthe EDC client will attempt to 
	 * reconnect to the broker and, if it succeeds, it will
	 * subscribe to the same topics which were active before
	 * loosing the connection.
	 * The prototype of the callback is defined by the MQtt
	 * client code
	 *
	 *
	 * @param context Application-specific context, in this 
	 *                implementation we pass 'this' as a reference of the 
	 *                EDC client instance. 
	 * @param topicName The topic of the message received
	 * @param dt
	 * @return 
	 */
	static void connectionLost(void* context, char* cause){
		
		EdcCloudClient *edcCloudClient = (EdcCloudClient *)context;

		edcCloudClient->m_Connected = false;

		if(edcCloudClient->m_connectionLost){
			edcCloudClient->m_connectionLost(cause);
		}
		else{
			do{
				if(edcCloudClient->startSession() == MQTTCLIENT_SUCCESS){
					//re-subscribe to the topics we were subscribed before
					edcCloudClient->subscribeAll();
				}
				else{
					EdcCloudClientSleep(EDCCLIENT_RECONNECT_TIMEOUT);
				}
			
			}while(!edcCloudClient->m_Connected);
		}
	}
	/**
	 * Publishes to the MQtt broker. Used internally by the EDC client
	 *
	 * @param topic The topic of the publish
	 * @param payload The payload of the publish
	 * @param qos
	 * @param retain
	 * @param timeout
	 * @return MQTTCLIENT_SUCCESS or one of the MQtt cliene error codes
	 * @see MQTTClient.h
	 */
	int _publish(string topic, EdcPayload * payload, int qos, bool retain, unsigned long timeout){

		int rc;
		MQTTClient_deliveryToken deliveryToken;
		MQTTClient_message pubmsg = { "", 0, 0, NULL, 0, 0, 0, 0 };

		memcpy(&pubmsg.struct_id, "MQTM", strlen("MQTM"));

		int size = payload->ByteSize(); 
		char* data = new char[size]; 

		payload->SerializeToArray(data, size);

		pubmsg.payload = data;
		pubmsg.payloadlen = size;

		pubmsg.qos = qos;
		pubmsg.retained = retain;

		char * ctopic = new char [topic.size()+1];
  		strcpy (ctopic, topic.c_str());

		rc = MQTTClient_publishMessage(m_MQTTClient, ctopic, &pubmsg, &deliveryToken);

		rc = MQTTClient_waitForCompletion(m_MQTTClient, deliveryToken, timeout);	

		delete[] data;

		return rc;
	}
	/**
	 * Subscribes to all the topics in the m_subscriptions list.
	 * Called by connectionLost after a succedssful reconnect
	 */
	int subscribeAll(){
	
		list<EdcSubscription>::iterator it;

		for(it = m_subscriptions.begin(); it!=m_subscriptions.end(); it++){
			char * topic = new char [it->getSemanticTopic().size()+1];
  			strcpy (topic, it->getSemanticTopic().c_str());

			MQTTClient_subscribe(m_MQTTClient, topic, it->getQos());
		}

		return MQTTCLIENT_SUCCESS;
	}
	/**
	 * Builds a full topic prepending the account name and
	 * the asset ID to the application specific semantic topic
	 * 
	 * @param semanticTopic The application specific semantic topic
	 * @return A string representing the full topic
	 */
	string buildTopic(string semanticTopic){

		string topic = m_edcConfiguration->getAccountName();
		topic += "/";
		topic += m_edcConfiguration->getDefaultAssetId();
		topic += "/";
		topic += semanticTopic;

		return topic;
	}
	/**
	 * Builds the birth certificate EDC topic 
	 * 
	 * @return A string representing the birth certificate EDC topic 
	 */
	string buildBirthCertificateTopic(){

		string topic = "$EDC/";
		topic += m_edcConfiguration->getAccountName();
		topic += "/";
		topic += m_edcConfiguration->getDefaultAssetId();
		topic += "/MQTT/BIRTH";

		return topic;
	}
	/**
	 * Builds the disconnect certificate EDC topic 
	 * 
	 * @return A string representing disconnect certificate EDC topic 
	 */
	string buildDisconnectCertificateTopic(){

		string topic = "$EDC/";
		topic += m_edcConfiguration->getAccountName();
		topic += "/";
		topic += m_edcConfiguration->getDefaultAssetId();
		topic += "/MQTT/DC";

		return topic;
	}
	/**
	 * Builds the control EDC topic 
	 * 
	 * @return A string representing the control EDC topic 
	 */
	string buildControlTopic(string controlTopic){

		string topic = "$EDC/";
		topic += m_edcConfiguration->getAccountName();
		topic += "/";
		topic += m_edcConfiguration->getDefaultAssetId();
		topic += "/";
		topic += controlTopic;

		return topic;
	}
	/**
	 * Builds the birth certificate EDC payload 
	 * 
	 * @return The birth certificate EDC payload 
	 */
	EdcPayload * buildBirthCertificatePayload(){

		EdcPayload  * edcPayload = new EdcPayload();
		EdcPayload_EdcMetric * metric;

		for (size_t i=0; i<_countof(EdcBirthPayloadNames); i++)
		{
			metric = edcPayload->add_metric();
			metric->set_name(EdcBirthPayloadNames[i]);
			metric->set_type(EdcPayload_EdcMetric_ValueType_STRING);
			metric->set_string_value(EdcBirthPayloadValues[i]);	
		}

		return edcPayload;
	}
	/**
	 * Builds the disconnect certificate EDC payload 
	 * 
	 * @return The birth disconnect EDC payload 
	 */
	EdcPayload * buildDisconnectCertificatePayload(){

		EdcPayload  * edcPayload = new EdcPayload();
		EdcPayload_EdcMetric * metric;

		for (int i=0; i<2; i++)
		{
			metric = edcPayload->add_metric();
			metric->set_name(EdcBirthPayloadNames[i]);
			metric->set_type(EdcPayload_EdcMetric_ValueType_STRING);
			metric->set_string_value(EdcBirthPayloadValues[i]);	
		}

		return edcPayload;
	}
	/**
	 * Publishes the birth certificate to the MQtt broker
	 * 
	 * @return MQTTCLIENT_SUCCESS or one of the MQtt cliene error codes
	 * @see MQTTClient.h
	 */
	int publishBirthCertificate(){
		
		EdcPayload * payload = buildBirthCertificatePayload();

		return _publish(buildBirthCertificateTopic(), payload, EDCCLIENT_QOS, EDCCLIENT_RETAIN, EDCCLIENT_PUBLISH_TIMEOUT);
	}
	/**
	 * Publishes the disconnect certificate to the MQtt broker
	 * 
	 * @return MQTTCLIENT_SUCCESS or one of the MQtt cliene error codes
	 * @see MQTTClient.h
	 */
	int publishDisconnectCertificate(){

		EdcPayload * payload = buildDisconnectCertificatePayload();

		return _publish(buildDisconnectCertificateTopic(), payload, EDCCLIENT_QOS, EDCCLIENT_RETAIN, EDCCLIENT_PUBLISH_TIMEOUT);
	}


public:

	/**
	 * Default constructor for EDC client
	 */
	EdcCloudClient() : m_messageArrived(NULL), m_messageDelivered(NULL), 
		m_connectionLost(NULL), m_Connected(false){
		m_subscriptions.clear();
	}

	/**
	 * Default constructor for EDC client
	 *
	 * @param 
	 * @param 
	 * @param 
	 * @param 
	 * @param 
	 */
	EdcCloudClient(EdcConfiguration * edcConfiguration, 
					EdcDeviceProfile * edcDeviceProfile,
					pfnEdcCloudClientMessageArrived * messageArrived,
					pfnEdcCloudClientMessageDelivered	* messageDelivered,
					pfnEdcCloudClientConnectionLost	* connectionLost){

		m_edcConfiguration = edcConfiguration;
		m_edcDeviceProfile = edcDeviceProfile;	
		m_messageArrived = messageArrived;
		m_messageDelivered = messageDelivered;
		m_connectionLost = connectionLost;
		m_Connected = false;
		m_subscriptions.clear();
	}
	/**
	 * Sets the EdcConfiguration for EDC client
	 *
	 * @param edcConfiguration 
	 */
	void setEdcConfiguration(EdcConfiguration * edcConfiguration){

		 m_edcConfiguration = edcConfiguration;
	}
	/**
	 * Sets the EdcDeviceProfile for EDC client
	 *
	 * @param edcDeviceProfile 
	 */
	void setEdcProfile(EdcDeviceProfile	* edcDeviceProfile){

		 m_edcDeviceProfile = edcDeviceProfile;
	}
	/**
	 * Sets the m_messageArrived callback from the application
	 *
	 * @param messageArrived 
	 */
	void setEdcCloudClientMessageArrived(pfnEdcCloudClientMessageArrived * messageArrived){

		 m_messageArrived = messageArrived;
	}
	/**
	 * Sets the m_messageDelivered callback from the application
	 *
	 * @param messageDelivered 
	 */
	void setEdcCloudClientMessageDelivered(pfnEdcCloudClientMessageDelivered * messageDelivered){

		 m_messageDelivered = messageDelivered;
	}
	/**
	 * Sets the m_connectionLost callback from the application
	 *
	 * @param connectionLost 
	 */
	void setEdcCloudClientConnectionLost(pfnEdcCloudClientConnectionLost * connectionLost){

		 m_connectionLost = connectionLost;
	}
	/**
	 * Returns the EDC client connection status to the broker
	 */
	bool isConnected(){
	
		return m_Connected;
	}
	/**
	 * Starts a connection to EDC connecting to the broker and publishing
	 * the birth certificate
	 */
	int startSession(){
		
		char * BrokerUrl = new char [m_edcConfiguration->getBrokerUrl().size()+1];
  		strcpy (BrokerUrl, m_edcConfiguration->getBrokerUrl().c_str());
		char * ClientId = new char [m_edcConfiguration->getClientId().size()+1];
		strcpy (ClientId, m_edcConfiguration->getClientId().c_str());
		
		//setup MQtt connection parameters
		int rc = MQTTClient_create(&m_MQTTClient, 
			BrokerUrl, ClientId,
			MQTTCLIENT_PERSISTENCE_NONE, NULL);

		if(rc != MQTTCLIENT_SUCCESS){
			return rc;
		}

		//Setup internal callbacks for MQtt client
		rc = MQTTClient_setCallbacks(m_MQTTClient, this, connectionLost, messageArrived, messageDelivered);
		
		if(rc != MQTTCLIENT_SUCCESS){
			return rc;
		}

		//connect to broker
		MQTTClient_connectOptions conn_opts = { "", 0, 60, 1, 1, NULL, NULL, NULL, 30, 20 };

		memcpy(&conn_opts.struct_id, "MQTC", strlen("MQTC"));
		conn_opts.keepAliveInterval = 20;
		conn_opts.cleansession = 1;

		conn_opts.username = new char [m_edcConfiguration->getUsername().size()+1];
  		strcpy (conn_opts.username, m_edcConfiguration->getUsername().c_str());
			
		conn_opts.password = new char [m_edcConfiguration->getPassword().size()+1];
  		strcpy (conn_opts.password, m_edcConfiguration->getPassword().c_str());

		rc =  MQTTClient_connect(m_MQTTClient, &conn_opts);

		if(rc != MQTTCLIENT_SUCCESS){
			return rc;
		}

		m_Connected = true;
		
		//publish birth certificate
		rc = publishBirthCertificate();

		return rc;
	}
	/**
	 * Ends the connection to EDC disconnecting frin the broker and publishing
	 * the disconnect certificate
	 */
	int stopSession(){
		
		int	rc = publishDisconnectCertificate();

		if(rc != MQTTCLIENT_SUCCESS){
			;//return rc;
		}

		 rc = MQTTClient_disconnect(m_MQTTClient, EDCCLIENT_PUBLISH_TIMEOUT);

		 return rc;
	};
	/**
	 * Publishes to the MQtt broker
	 *
	 * @param topic The topic of the publish
	 * @param payload The payload of the publish
	 * @param qos
	 * @param retain
	 * @param timeout
	 * @return MQTTCLIENT_SUCCESS or one of the MQtt cliene error codes
	 * @see MQTTClient.h
	 */
	int publish(string topic, EdcPayload * payload, int qos, bool retain, unsigned long timeout){

		char * fulltopic = new char [buildTopic(topic).size()+1];
  		strcpy (fulltopic, buildTopic(topic).c_str());

		return _publish(fulltopic, payload, qos, retain, timeout);
	}
	/**
	 * Subscribes to a semantic topic. The topic is inserted in the 
	 * m_subscriptions list
	 *
	 * @param topic The semantic topic
	 * @param qos
	 * @return MQTTCLIENT_SUCCESS or one of the MQtt cliene error codes
	 * @see MQTTClient.h
	 */
	int subscribe(string topic, int qos){
		
		int rc;
		char * stopic = new char [buildTopic(topic).size()+1];
  		strcpy (stopic, buildTopic(topic).c_str());

		rc = MQTTClient_subscribe(m_MQTTClient, stopic, qos);

		if(rc != MQTTCLIENT_SUCCESS){
			return rc;
		}
		
		m_subscriptions.push_front(EdcSubscription(stopic,qos));

		return MQTTCLIENT_SUCCESS;
	}
	/**
	 * Subscribes to a control topic.The topic is inserted in the 
	 * m_subscriptions list
     *
	 *
	 * @param topic The control topic
	 * @param qos
	 * @return MQTTCLIENT_SUCCESS or one of the MQtt cliene error codes
	 * @see MQTTClient.h
	 */
	int controlSubscribe(string topic, int qos){
		
		int rc;
		char * ctopic = new char [buildControlTopic(topic).size()+1];
  		strcpy (ctopic, buildControlTopic(topic).c_str());

		rc = MQTTClient_subscribe(m_MQTTClient, ctopic, qos);

		if(rc != MQTTCLIENT_SUCCESS){
			return rc;
		}
		
		m_subscriptions.push_front(EdcSubscription(ctopic,qos));

		return MQTTCLIENT_SUCCESS;
	}
	/**
	 * Unsubscribes to a semantic topic. The topic is deleted from the 
	 * m_subscriptions list
	 *
	 * @param topic The semantic topic
	 * @param qos
	 * @return MQTTCLIENT_SUCCESS or one of the MQtt cliene error codes
	 * @see MQTTClient.h
	 */
	int unsubscribe(string topic){
		
		int rc;
		char * utopic = new char [buildTopic(topic).size()+1];
  		strcpy (utopic, buildTopic(topic).c_str());

		rc = MQTTClient_unsubscribe(m_MQTTClient, utopic);

		if(rc != MQTTCLIENT_SUCCESS){
			return rc;
		}

		EdcSubscription sub = EdcSubscription(utopic,0);

		list<EdcSubscription>::iterator it;

		for(it = m_subscriptions.begin(); it!=m_subscriptions.end(); it++){
			if(it->equals(sub)){
				m_subscriptions.erase(it);
				break;
			}
		}

		return MQTTCLIENT_SUCCESS;
	}
	/**
	 * Unsubscribes to a control topic. The topic is deleted from the 
	 * m_subscriptions list
	 *
	 * @param topic The control topic
	 * @param qos
	 * @return MQTTCLIENT_SUCCESS or one of the MQtt cliene error codes
	 * @see MQTTClient.h
	 */
	int controlUnsubscribe(string topic){

		int rc;
		char * utopic = new char [buildControlTopic(topic).size()+1];
  		strcpy (utopic, buildControlTopic(topic).c_str());

		rc = MQTTClient_unsubscribe(m_MQTTClient, utopic);

		if(rc != MQTTCLIENT_SUCCESS){
			return rc;
		}

		EdcSubscription sub = EdcSubscription(utopic,0);

		list<EdcSubscription>::iterator it;

		for(it = m_subscriptions.begin(); it!=m_subscriptions.end(); it++){
			if(it->equals(sub)){
				m_subscriptions.erase(it);
				break;
			}
		}

		return MQTTCLIENT_SUCCESS;

	}
	/**
	 * Unsubscribes to all the topics, semantic and control. 
	 * The topics are deleted from the m_subscriptions list
	 *
	 * @param topic The control topic
	 * @param qos
	 * @return MQTTCLIENT_SUCCESS or one of the MQtt cliene error codes
	 * @see MQTTClient.h
	 */
	int unsubscribeAll(){
	
		list<EdcSubscription>::iterator it;

		for(it = m_subscriptions.begin(); it!=m_subscriptions.end(); it++){
			char * utopic = new char [it->getSemanticTopic().size()+1];
  			strcpy (utopic, it->getSemanticTopic().c_str());

			if(MQTTClient_unsubscribe(m_MQTTClient, utopic) == MQTTCLIENT_SUCCESS){
				it = m_subscriptions.erase(it);
			}
		}

		return MQTTCLIENT_SUCCESS;
	}
	/**
	 * Terminate the EDC client. The MQtt client instance is
	 * destroyed and the protobuf library frees whatever it
	 * allocated
	 */
	void terminate(){

		MQTTClient_destroy(&m_MQTTClient);
		google::protobuf::ShutdownProtobufLibrary();
	}


};
#endif

