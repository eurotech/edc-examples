// MQttTest.cpp : Defines the entry point for the console application.
//

#include <windows.h>
#include <commctrl.h>

#include "stdio.h"
#include "stdlib.h"
#include "string.h"

extern "C" {
#include "MQTTClient.h"
}

/*
// EXAMPLE #1

//#define ADDRESS     "tcp://localhost:1883"
//#define ADDRESS     "tcp://broker-sandbox.everyware-cloud.com:1883/"
#define ADDRESS     "tcp://192.168.200.176:1883/"
#define CLIENTID    "ExampleClientPub"
#define TOPIC       "MQTTExamples"
#define PAYLOAD     "HelloWorld!"
#define QOS         1
#define TIMEOUT     10000L

int _tmain(int argc, _TCHAR* argv[])
{
	//printf ("MQttTest start\r\n");

    MQTTClient client;
    MQTTClient_connectOptions conn_opts;
	conn_opts.struct_id[0] = 'M';
	conn_opts.struct_id[1] = 'Q';
	conn_opts.struct_id[2] = 'T';
	conn_opts.struct_id[3] = 'C';
	conn_opts.struct_version = 0;
	conn_opts.keepAliveInterval = 60;
	conn_opts.cleansession = 1;
	conn_opts.reliable = 1;		
	conn_opts.will = NULL;
	conn_opts.username = NULL;	
	conn_opts.password = NULL;
	conn_opts.connectTimeout = 30;
	conn_opts.retryInterval = 20;

    MQTTClient_message pubmsg; 
	pubmsg.struct_id[0] = 'M';
	pubmsg.struct_id[1] = 'Q';
	pubmsg.struct_id[2] = 'T';
	pubmsg.struct_id[3] = 'M';
	pubmsg.struct_version = 0;
	pubmsg.payloadlen = 0;
	pubmsg.payload = NULL;
	pubmsg.qos = 0;
	pubmsg.retained = 0;
	pubmsg.dup = 0;
	pubmsg.msgid = 0;

    MQTTClient_deliveryToken token;
    int rc;

	//printf ("MQTTClient_create...\r\n");

    MQTTClient_create(&client, ADDRESS, CLIENTID,
    MQTTCLIENT_PERSISTENCE_NONE, NULL);
    conn_opts.keepAliveInterval = 20;
    conn_opts.cleansession = 1;

	//printf ("MQTTClient_connect...\r\n");

    if ((rc = MQTTClient_connect(client, &conn_opts)) != MQTTCLIENT_SUCCESS)
    {
        printf("Failed to connect, return code %d\n", rc);
        exit(-1);
    }
    pubmsg.payload = PAYLOAD;
    pubmsg.payloadlen = strlen(PAYLOAD);
    pubmsg.qos = QOS;
    pubmsg.retained = 0;

	//printf ("MQTTClient_publishMessage...\r\n");

    MQTTClient_publishMessage(client, TOPIC, &pubmsg, &token);
    printf("Waiting for up to %d seconds for publication of %s\n"
            "on topic %s for client with ClientID: %s\n",
            (int)(TIMEOUT/1000), PAYLOAD, TOPIC, CLIENTID);

	//printf ("MQTTClient_waitForCompletion...\r\n");
    rc = MQTTClient_waitForCompletion(client, token, TIMEOUT);
    printf("Message with delivery token %d delivered\n", token);
	//printf ("MQTTClient_disconnect...\r\n");
    MQTTClient_disconnect(client, 10000);
	//printf ("MQTTClient_destroy...\r\n");
    MQTTClient_destroy(&client);

	printf ("MQttTest completed\r\n");
    return rc;
}
*/

/*
// EXAMPLE #2

#include "stdio.h"
#include "stdlib.h"
#include "string.h"
#include "MQTTClient.h"

//#define ADDRESS     "tcp://localhost:1883"
//#define ADDRESS     "tcp://broker-sandbox.everyware-cloud.com:1883/"
#define ADDRESS     "tcp://192.168.200.176:1883/"
#define CLIENTID    "ExampleClientPub"
#define TOPIC       "MQTTExamples"
#define PAYLOAD     "HelloWorld!"
#define QOS         1
#define TIMEOUT     10000L

volatile MQTTClient_deliveryToken deliveredtoken;

void delivered(void *context, MQTTClient_deliveryToken dt)
{
    printf("Message with token value %d delivery confirmed\n", dt);
    deliveredtoken = dt;
}

int msgarrvd(void *context, char *topicName, int topicLen, MQTTClient_message *message)
{
    int i;
    char* payloadptr;

    printf("Message arrived\n");
    printf("     topic: %s\n", topicName);
    printf("   message: ");

    payloadptr = (char *)(message->payload);
    for(i=0; i<message->payloadlen; i++)
    {
        putchar(*payloadptr++);
    }
    putchar('\n');
    MQTTClient_freeMessage(&message);
    MQTTClient_free(topicName);
    return 1;
}

void connlost(void *context, char *cause)
{
    printf("\nConnection lost\n");
    printf("     cause: %s\n", cause);
}

int _tmain(int argc, _TCHAR* argv[])
{
    MQTTClient client;

    //MQTTClient_connectOptions conn_opts = MQTTClient_connectOptions_initializer;

	MQTTClient_connectOptions conn_opts;
	conn_opts.struct_id[0] = 'M';
	conn_opts.struct_id[1] = 'Q';
	conn_opts.struct_id[2] = 'T';
	conn_opts.struct_id[3] = 'C';
	conn_opts.struct_version = 0;
	conn_opts.keepAliveInterval = 60;
	conn_opts.cleansession = 1;
	conn_opts.reliable = 1;		
	conn_opts.will = NULL;
	conn_opts.username = NULL;	
	conn_opts.password = NULL;
	conn_opts.connectTimeout = 30;
	conn_opts.retryInterval = 20;

    //MQTTClient_message pubmsg = MQTTClient_message_initializer;

	MQTTClient_message pubmsg; 
	pubmsg.struct_id[0] = 'M';
	pubmsg.struct_id[1] = 'Q';
	pubmsg.struct_id[2] = 'T';
	pubmsg.struct_id[3] = 'M';
	pubmsg.struct_version = 0;
	pubmsg.payloadlen = 0;
	pubmsg.payload = NULL;
	pubmsg.qos = 0;
	pubmsg.retained = 0;
	pubmsg.dup = 0;
	pubmsg.msgid = 0;

    MQTTClient_deliveryToken token;
    int rc;

    MQTTClient_create(&client, ADDRESS, CLIENTID,
        MQTTCLIENT_PERSISTENCE_NONE, NULL);
    conn_opts.keepAliveInterval = 20;
    conn_opts.cleansession = 1;

    MQTTClient_setCallbacks(client, NULL, connlost, msgarrvd, delivered);

    if ((rc = MQTTClient_connect(client, &conn_opts)) != MQTTCLIENT_SUCCESS)
    {
        printf("Failed to connect, return code %d\n", rc);
        exit(-1);	
    }
    pubmsg.payload = PAYLOAD;
    pubmsg.payloadlen = strlen(PAYLOAD);
    pubmsg.qos = QOS;
    pubmsg.retained = 0;
    deliveredtoken = 0;
    MQTTClient_publishMessage(client, TOPIC, &pubmsg, &token);
    printf("Waiting for publication of %s\n"
            "on topic %s for client with ClientID: %s\n",
            PAYLOAD, TOPIC, CLIENTID);
    while(deliveredtoken != token);
    MQTTClient_disconnect(client, 10000);
    MQTTClient_destroy(&client);
    return rc;
}
*/

/*
// EXAMPLE #3

#include "stdio.h"
#include "stdlib.h"
#include "string.h"
#include "MQTTClient.h"

//#define ADDRESS     "tcp://localhost:1883"
//#define ADDRESS     "tcp://broker-sandbox.everyware-cloud.com:1883/"
#define ADDRESS     "tcp://192.168.200.176:1883/"
#define CLIENTID    "ExampleClientSub"
#define TOPIC       "MQTTExamples"
#define PAYLOAD     "HelloWorld!"
#define QOS         1
#define TIMEOUT     10000L

volatile MQTTClient_deliveryToken deliveredtoken;

void delivered(void *context, MQTTClient_deliveryToken dt)
{
    printf("Message with token value %d delivery confirmed\n", dt);
    deliveredtoken = dt;
}

int msgarrvd(void *context, char *topicName, int topicLen, MQTTClient_message *message)
{
    int i;
    char* payloadptr;

    printf("Message arrived\n");
    printf("     topic: %s\n", topicName);
    printf("   message: ");

    payloadptr = (char *)(message->payload);
    for(i=0; i<message->payloadlen; i++)
    {
        putchar(*payloadptr++);
    }
    putchar('\n');
    MQTTClient_freeMessage(&message);
    MQTTClient_free(topicName);
    return 1;
}

void connlost(void *context, char *cause)
{
    printf("\nConnection lost\n");
    printf("     cause: %s\n", cause);
}

int _tmain(int argc, _TCHAR* argv[])
{
    MQTTClient client;
    
	//MQTTClient_connectOptions conn_opts = MQTTClient_connectOptions_initializer;
    
	MQTTClient_connectOptions conn_opts;
	conn_opts.struct_id[0] = 'M';
	conn_opts.struct_id[1] = 'Q';
	conn_opts.struct_id[2] = 'T';
	conn_opts.struct_id[3] = 'C';
	conn_opts.struct_version = 0;
	conn_opts.keepAliveInterval = 60;
	conn_opts.cleansession = 1;
	conn_opts.reliable = 1;		
	conn_opts.will = NULL;
	conn_opts.username = NULL;	
	conn_opts.password = NULL;
	conn_opts.connectTimeout = 30;
	conn_opts.retryInterval = 20;

	int rc;
    int ch;

    MQTTClient_create(&client, ADDRESS, CLIENTID,
        MQTTCLIENT_PERSISTENCE_NONE, NULL);
    conn_opts.keepAliveInterval = 20;
    conn_opts.cleansession = 1;

    MQTTClient_setCallbacks(client, NULL, connlost, msgarrvd, delivered);

    if ((rc = MQTTClient_connect(client, &conn_opts)) != MQTTCLIENT_SUCCESS)
    {
        printf("Failed to connect, return code %d\n", rc);
        exit(-1);	
    }
    printf("Subscribing to topic %s\nfor client %s using QoS%d\n\n"
           "Press Q<Enter> to quit\n\n", TOPIC, CLIENTID, QOS);
    MQTTClient_subscribe(client, TOPIC, QOS);

    do 
    {
        ch = getchar();
    } while(ch!='Q' && ch != 'q');

    MQTTClient_disconnect(client, 10000);
    MQTTClient_destroy(&client);
    return rc;
}
*/


// EXAMPLE #4

#include "stdio.h"
#include "stdlib.h"
#include "string.h"
#include "MQTTClient.h"

//#define ADDRESS     "tcp://localhost:1883"
#define ADDRESS     "tcp://broker-sandbox.everyware-cloud.com:1883/"
//#define ADDRESS     "tcp://192.168.200.176:1883/"
#define CLIENTID    "ExampleClientSub"
//#define TOPIC       "MQTTExamples"
#define TOPIC_SUB   "myEdcAccount/334455AABBCC/#"
#define TOPIC_PUB   "myEdcAccount/334455AABBCC/nativeclient/data"
//#define TOPIC_SUB   "edcguest/334455AABBCC/#"
//#define TOPIC_PUB   "edcguest/334455AABBCC/nativeclient/data"
#define PAYLOAD     "HelloWorld!"
#define QOS         1
#define TIMEOUT     10000L

volatile MQTTClient_deliveryToken deliveredtoken;

void delivered(void *context, MQTTClient_deliveryToken dt)
{
    printf("Message with token value %d delivery confirmed\n", dt);
    deliveredtoken = dt;
}

int msgarrvd(void *context, char *topicName, int topicLen, MQTTClient_message *message)
{
    int i;
    char* payloadptr;

    printf("Message arrived\n");
    printf("     topic: %s\n", topicName);
    printf("   message: ");

    payloadptr = (char *)(message->payload);
    for(i=0; i<message->payloadlen; i++)
    {
        putchar(*payloadptr++);
    }
    putchar('\n');
    MQTTClient_freeMessage(&message);
    MQTTClient_free(topicName);
    return 1;
}

void connlost(void *context, char *cause)
{
    printf("\nConnection lost\n");
    printf("     cause: %s\n", cause);
}

int _tmain(int argc, _TCHAR* argv[])
{
    MQTTClient client;
    
	//MQTTClient_connectOptions conn_opts = MQTTClient_connectOptions_initializer;

	MQTTClient_connectOptions conn_opts;
	conn_opts.struct_id[0] = 'M';
	conn_opts.struct_id[1] = 'Q';
	conn_opts.struct_id[2] = 'T';
	conn_opts.struct_id[3] = 'C';
	conn_opts.struct_version = 0;
	conn_opts.keepAliveInterval = 60;
	conn_opts.cleansession = 1;
	conn_opts.reliable = 1;		
	conn_opts.will = NULL;
	conn_opts.username = NULL;	
	conn_opts.password = NULL;
	conn_opts.connectTimeout = 30;
	conn_opts.retryInterval = 20;

	// connection data specific for sandbox broker
	conn_opts.keepAliveInterval = 20;
	conn_opts.cleansession = 1;
	conn_opts.username = new char [30];
	strcpy (conn_opts.username, "myEdcUserName_broker");
	//strcpy (conn_opts.username, "edcguest_broker");
	conn_opts.password = new char [9];
	strcpy (conn_opts.password, "myEdcPassword3#");
	//strcpy (conn_opts.password, "We!come1");

	int rc;

    MQTTClient_create(&client, ADDRESS, CLIENTID,
    //    MQTTCLIENT_PERSISTENCE_NONE, NULL);
	    MQTTCLIENT_PERSISTENCE_DEFAULT, NULL);
    conn_opts.keepAliveInterval = 20;
    conn_opts.cleansession = 1;

    MQTTClient_setCallbacks(client, NULL, connlost, msgarrvd, delivered);

    if ((rc = MQTTClient_connect(client, &conn_opts)) != MQTTCLIENT_SUCCESS)
    {
        printf("Failed to connect, return code %d\n", rc);
        exit(-1);	
    }

	printf("Subscribing to topic %s\nfor client %s using QoS%d\n", TOPIC_SUB, CLIENTID, QOS);
    MQTTClient_subscribe(client, TOPIC_SUB, QOS);

	MQTTClient_deliveryToken token;

	MQTTClient_message pubmsg; 
	pubmsg.struct_id[0] = 'M';
	pubmsg.struct_id[1] = 'Q';
	pubmsg.struct_id[2] = 'T';
	pubmsg.struct_id[3] = 'M';
	pubmsg.struct_version = 0;
	pubmsg.payloadlen = 0;
	pubmsg.payload = NULL;
	pubmsg.qos = 0;
	pubmsg.retained = 0;
	pubmsg.dup = 0;
	pubmsg.msgid = 0;
	
	pubmsg.payload = PAYLOAD;
    pubmsg.payloadlen = strlen(PAYLOAD);
    pubmsg.qos = QOS;
    pubmsg.retained = 0;
    deliveredtoken = 0;

	for (int i=0; i<10; i++)
	{
		MQTTClient_publishMessage(client, TOPIC_PUB, &pubmsg, &token);
		printf("Waiting for publication of %s\n"
			    "on topic %s for client with ClientID: %s\n",
				PAYLOAD, TOPIC_PUB, CLIENTID);
		while(deliveredtoken != token);

		Sleep(1000);
	}

    MQTTClient_disconnect(client, 10000);
    MQTTClient_destroy(&client);
    return rc;
}
