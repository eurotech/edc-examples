package com.eurotech.cloud.examples;

import java.util.Date;

import com.eurotech.cloud.client.EdcCallbackHandler;
import com.eurotech.cloud.client.EdcClientFactory;
import com.eurotech.cloud.client.EdcCloudClient;
import com.eurotech.cloud.client.EdcConfiguration;
import com.eurotech.cloud.client.EdcConfigurationFactory;
import com.eurotech.cloud.client.EdcDeviceProfile;
import com.eurotech.cloud.client.EdcDeviceProfileFactory;
import com.eurotech.cloud.message.EdcBirthPayload;
import com.eurotech.cloud.message.EdcPayload;
import com.eurotech.cloud.message.EdcPosition;


/**
 * Sample Java client which connects to the Everyware Cloud platform.
 * It simulates a device whose connection logic is written in Java
 * and it illustrates how devices can connect to the Everyware Cloud 
 * and publish/received data.
 * 
 * The EdcJavaClient class performs the following actions:
 * 
 * 1. Prepares the configuration
 * 2. Connects to the broker and start a session
 * 3. Subscribes to the a couple of topic
 * 4. Starts publishing some data and verify that the messages are also received
 * 5. Disconnect
 * 
 */
public class EdcJavaClient implements EdcCallbackHandler 
{
    // >>>>>> Set these variables according to your Cloud user account
    //
    private static final String ACCOUNT_NAME = "myEdcAccount";                                    // Your Account name in Cloud
    private static final String ASSET_ID     = "my-Device";                                       // Unique Asset ID of this client device
    private static final String BROKER_URL   = "mqtt://broker-sandbox.everyware-cloud.com:1883";  // URL address of broker 
    private static final String CLIENT_ID    = "my-Device-client";                                // Unique Client ID of this client device
    private static final String USERNAME     = "myEdcUserName_broker";                            // Username in account, to use for publishing
    private static final String PASSWORD     = "myEdcPassword3#";                                 // Password associated with Username
    //
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    private static final int    MAX_PUBLISH    = 10;         // number of times to publish
    private static final int    PUBLISH_PERIOD = 10000;      // time between published messages, in milliseconds
    private static final double LATITUDE       = 46.369079;  // default (simulated) GPS position
    private static final double LONGITUDE      = 13.076729;  // default (simulated) GPS position
    
    

    public static void main(String[] args) 
        throws Exception
    {
        //
        // Configure: create client configuration, and set its properties
        //
        EdcConfigurationFactory confFact = EdcConfigurationFactory.getInstance();
        EdcConfiguration conf = confFact.newEdcConfiguration(ACCOUNT_NAME, 
                                                             ASSET_ID, 
                                                             BROKER_URL, 
                                                             CLIENT_ID, 
                                                             USERNAME, 
                                                             PASSWORD);
        
        EdcDeviceProfileFactory profFactory = EdcDeviceProfileFactory.getInstance();
        EdcDeviceProfile prof = profFactory.newEdcDeviceProfile();
        prof.setDisplayName("MyDisplayName");			// this might be set to the ASSET_ID, for display in the Cloud
        prof.setModelName("Eclipse Java Client");

        //set GPS position in device profile - this is sent only once, with the birth certificate
        prof.setLongitude(LONGITUDE); 
        prof.setLatitude(LATITUDE);

        //
        // Connect and start the session
        // 
        EdcCloudClient edcCloudClient = null;
        edcCloudClient = EdcClientFactory.newInstance(conf, prof, new EdcJavaClient());
        edcCloudClient.startSession();
        System.out.println("Session started");
        
        //
        // Subscribe
        //
        System.out.println("Subscribe to data topics of all assets in the account");
        edcCloudClient.subscribe("#", 1);

        System.out.println("Subscribe to control topics of all assets in the account");
        edcCloudClient.controlSubscribe("+", "#", 1);

        //
        // Publish messages
        //
        String pubTSemanticTopic = "sample/data";    //sample publish topic;
        System.out.println("publish on data semantic topic: " + pubTSemanticTopic);
        for (int i = 0; i < MAX_PUBLISH; i++) {
            
            edcCloudClient.publish(pubTSemanticTopic, createPayload(i), 1, false);   //call createPayload() each time
            Thread.sleep(PUBLISH_PERIOD);
        }
    
        //
        // Sleep to allow receipt of more publishes, then terminate connection
        int listenSeconds = 60;             //keep connection alive for listenSeconds
        Thread.sleep(listenSeconds * 1000); //sleep in milliseconds

        //
        // Stop the session and close the connection
        //
        edcCloudClient.stopSession();
        edcCloudClient.terminate();
        System.out.println("Terminating EDC Cloud Client");
    }

    
    
    private static EdcPayload createPayload(int counter) 
    {
        //create payload with samples of different data types
        EdcPayload edcPayload = new EdcPayload();

        // timestamp the payload
        Date capturedOn = new Date();
        edcPayload.setTimestamp(capturedOn);

        //add a metric that changes with every loop
        edcPayload.addMetric("counter", counter);
        edcPayload.addMetric("str",     new String("this is a string"));
        edcPayload.addMetric("int",     (int) 100);
        edcPayload.addMetric("flt",     (float) Math.random());
        edcPayload.addMetric("dbl",     (double) Math.random());
        edcPayload.addMetric("long",    (long) 200000);
        edcPayload.addMetric("bool",    true);

        byte[] byteArray = new byte[3];
        byteArray[0] = 0x31;
        byteArray[1] = 0x32;
        byteArray[2] = 0x33;
        edcPayload.addMetric("arr",     byteArray);

        //use a simulated and changing GPS position
        EdcPosition position = new EdcPosition();
        position.setLongitude(LONGITUDE + Math.random() - 0.5); //randomly vary the position
        position.setLatitude(LATITUDE + Math.random() - 0.5);   //randomly vary the position
        position.setAltitude(296);
        position.setHeading(2);
        position.setPrecision(10);
        position.setSpeed(60);
        position.setSatellites(3);
        position.setStatus(1);
        position.setTimestamp(capturedOn);

        //set position in payload, published with every message
        edcPayload.setPosition(position);

        return edcPayload;
    }

    
    // -----------------------------------------------------------------------
    //
    //    MQTT Callback methods
    //
    // -----------------------------------------------------------------------
    
    //display control messages received from broker 
    public void controlArrived(String assetId, String topic, EdcPayload msg, int qos, boolean retain) 
    {

        System.out.println("Control publish arrived on semantic topic: " + topic + " , qos: " + qos);

        // Print all the metrics
        for (String name : msg.metricNames()) {
            System.out.println(name + ":" + msg.getMetric(name));
        }

        if (topic.contains("BC")) {
            EdcBirthPayload edcBirthMessage;
            try {
                edcBirthMessage = new EdcBirthPayload(msg);
                System.out.println("Birth certificate arrived: " + edcBirthMessage.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //display data messages received from broker
    public void publishArrived(String assetId, String topic, EdcPayload msg, int qos, boolean retain) {

        System.out.println("Data publish arrived on semantic topic: " + topic + ", qos: " + qos + ", assetId: " + assetId);

        // Print all the metrics
        for (String name : msg.metricNames()) {
            String content = "";
            if (msg.getMetric(name).getClass().isArray()) {
                //display byte arrays as both hex characters and String
                byte[] contentArray = (byte[]) msg.getMetric(name);
                for (int i = 0; i < contentArray.length; i++) {
                    content = content + Integer.toHexString(0xFF & contentArray[i]) + " ";
                    
                }
                content = content + " (as String: '" + new String(contentArray) + "')";
            } else {
                content = msg.getMetric(name).toString();
            }
            System.out.println(name + ":" + content);
        }
    }

    public void connectionLost() {
        System.out.println("EDC client connection lost");
    }

    public void connectionRestored() {
        System.out.println("EDC client reconnected");
    }

    public void published(int messageId) {
        System.out.println("Publish message ID: " + messageId + " confirmed");
    }

    public void subscribed(int messageId) {
        System.out.println("Subscribe message ID: " + messageId + " confirmed");
    }

    public void unsubscribed(int messageId) {
        System.out.println("Unsubscribe message ID: " + messageId + " confirmed");
    }

    public void controlArrived(String assetId, String topic, byte[] payload,
            int qos, boolean retain) {
        // TODO Auto-generated method stub
    }

    public void publishArrived(String assetId, String topic, byte[] payload,
            int qos, boolean retain) {
        // TODO Auto-generated method stub
    }
}
