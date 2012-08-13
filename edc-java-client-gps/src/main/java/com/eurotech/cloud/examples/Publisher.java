package com.eurotech.cloud.examples;

import java.util.Date;

import com.eurotech.cloud.client.EdcClientException;
import com.eurotech.cloud.client.EdcClientFactory;
import com.eurotech.cloud.client.EdcCloudClient;
import com.eurotech.cloud.client.EdcConfiguration;
import com.eurotech.cloud.client.EdcConfigurationFactory;
import com.eurotech.cloud.client.EdcDeviceProfile;
import com.eurotech.cloud.client.EdcDeviceProfileFactory;
import com.eurotech.cloud.examples.gps.GpsEmulator;
import com.eurotech.cloud.message.EdcPayload;
import com.eurotech.cloud.message.EdcPosition;



public class Publisher {

	private static final String ACCOUNT_NAME = "myEdcAccount";					// Your Account name in Cloud
	private static final String ASSET_ID = "my-device";						// Unique Asset ID of this client device
	private static final String BROKER_URL = "mqtt://broker-sandbox.everyware-cloud.com:1883/";	// URL address of broker
	private static final String CLIENT_ID = "my-Device-client";					// Unique Client ID of this client device
	private static final String BROKER_USERNAME = "myEdcUserName_broker";				// Username in account, to use for publishing
	private static final String PASSWORD = "myEdcPassword";						// Password associated with Username

	private static final int    MAX_PUBLISH    = 20000;         // number of times to publish
	private static final int    PUBLISH_PERIOD = 1000;       // time between published messages, in milliseconds

	public static void main(String[] args) {

		GpsEmulator gpsEmulator = new GpsEmulator();
		gpsEmulator.bind();

		// Configure: create client configuration, and set its properties
		EdcConfigurationFactory confFact = EdcConfigurationFactory.getInstance();
		EdcConfiguration conf = confFact.newEdcConfiguration(ACCOUNT_NAME, 
				ASSET_ID, 
				BROKER_URL, 
				CLIENT_ID, 
				BROKER_USERNAME, 
				PASSWORD);

		EdcDeviceProfileFactory profFactory = EdcDeviceProfileFactory.getInstance();
		EdcDeviceProfile prof = profFactory.newEdcDeviceProfile();
		prof.setDisplayName("GPS Client");           // friendly name for this CLIENT_ID, for display in the Cloud
		prof.setModelName("GPS Client");

		//set GPS position in device profile - this is sent only once, with the birth certificate
		prof.setLongitude(new Double(gpsEmulator.getLongitude())); 
		prof.setLatitude(new Double(gpsEmulator.getLatitude()));

		// Connect and start the session
		String semanticTopic = "gps/data";

		EdcCloudClient edcCloudClient = null;
		try {
			edcCloudClient = EdcClientFactory.newInstance(conf, prof, null);
			edcCloudClient.startSession();
			System.out.println("Session started");

			//
			// Publish messages
			System.out.println("publish on topic: " + semanticTopic);
			for (int i = 0; i < MAX_PUBLISH; i++) {
				edcCloudClient.publish(semanticTopic, createPayload(gpsEmulator, i).toByteArray(), 1, false);   //call createPayload() each time
				Thread.sleep(PUBLISH_PERIOD);
			}
		} catch (EdcClientException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {

			// Stop the session and close the connection
			if (edcCloudClient != null) {
				try {
					edcCloudClient.stopSession();
					edcCloudClient.terminate();
					System.out.println("Terminated test GPS Client");
				} catch (EdcClientException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static EdcPayload createPayload(GpsEmulator gpsEmulator, int counter)  {
		//create payload with samples of different data types
		EdcPayload edcPayload = new EdcPayload();

		// timestamp the payload
		Date capturedOn = new Date();
		edcPayload.setTimestamp(capturedOn);

		//use a simulated and changing GPS position
		EdcPosition position = new EdcPosition();
		position.setLatitude(gpsEmulator.getLatitude());
		position.setLongitude(gpsEmulator.getLongitude());
		position.setSpeed(60);
		position.setHeading(0);
		position.setAltitude(gpsEmulator.getAltitude());
		position.setPrecision(10);
		position.setSatellites(3);
		position.setStatus(1);
		position.setTimestamp(new Date(gpsEmulator.getTime()));

		//set position in payload, published with every message
		edcPayload.setPosition(position);

		return edcPayload;
	}
}

