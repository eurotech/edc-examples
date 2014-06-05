package com.eurotech.edcandroid.gateway;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.eurotech.cloud.client.EdcCallbackHandler;
import com.eurotech.cloud.client.EdcClientException;
import com.eurotech.cloud.client.EdcClientFactory;
import com.eurotech.cloud.client.EdcCloudClient;
import com.eurotech.cloud.client.EdcConfiguration;
import com.eurotech.cloud.client.EdcConfigurationFactory;
import com.eurotech.cloud.client.EdcDeviceProfile;
import com.eurotech.cloud.client.EdcDeviceProfileFactory;
import com.eurotech.cloud.message.EdcInvalidTopicException;
import com.eurotech.cloud.message.EdcPayload;
import com.eurotech.cloud.message.EdcPosition;
import com.eurotech.cloud.message.EdcSubscription;
import com.eurotech.edcandroid.EDCAndroid;
import com.eurotech.edcandroid.R;
import com.eurotech.edcandroid.commands.CommandSelection;
import com.eurotech.edcandroid.tools.DeviceData;
import com.eurotech.edcandroid.tools.EdcNotification;
import com.eurotech.edcandroid.tools.SensorData;
import com.eurotech.edcandroid.tools.SensorSelection;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class EdcConnectionService extends Service implements EdcCallbackHandler {

	private static String  USERNAME;
	private static String  PASSWORD;
	private static String  BROKER_URL;
	public static  String  ACCOUNT_NAME;
	public static  String  ASSET_ID;
	private static String  CLIENT_ID;

	private static Context localContext;
	private LocalBinder<EdcConnectionService> mBinder;
	public static EdcCloudClient edcClient = null;
	public static EdcServiceStopper serviceStopper;
	public static EdcMessagePublisher messagePublisher;
	public static EdcTopicSubscriber topicSubscriber;
	public static EdcMessageReceiverManager receiverManager;
	public static EdcMessageReceiver messageReceiver;
	private static OnSharedPreferenceChangeListener preferenceListener;
	private NetworkChangeReceiver networkChangeReceiver;
	private IntentFilter networkFilter;
	private boolean firstConnection = true;
	private static ArrayList<String> subTpc;
	private Intent localIntent;
	private int localStartId;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private SensorManager sensorManager;
	private SensorSelection sensorSelction;
	private SensorEventListener sensorListener;
	public static CommandSelection commandSelection;
	private static EdcDeviceProfileFactory profileFactory;
	private static EdcDeviceProfile profile;
	public static Boolean sensorsConnected = false;
	private SensorData sensorData = null;
	public static SensorSender sensorSender;
	public static final int millis = 1000;
	private static float minAccuracyMeters = 500;
	private static long minTimeMillis      = 1 * 10 * millis;
	private static long minDistanceMeters  = 5;
	public static boolean broadcastEnabled = false;
	public static  DeviceData deviceData;

	public static final String messageTitle = "EDC Notification";
	private final int NULL = -999;
	private static final short reconnectInterval  = 5;
	private static final int maxNetworkConnectionWaitSeconds  = 20;
	public static final String commandString = "command";
	public static final String commandPrefix = commandString + "_";
	private GpsStatus.Listener gpsStatusListener;
	private int VisibleSatellites = 0;
	
	private static final ArrayList<String> keyList = new ArrayList<String>(
    		Arrays.asList(
    				"Commands",
    				"Automatic publishing",
    				"pref_connectivity_autopublish_freq_time"
    				));


	public void onCreate() {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		deviceData = new DeviceData(this);
		USERNAME     = deviceData.userName;
		PASSWORD     = deviceData.password;
		BROKER_URL   = deviceData.brokerUrl;
		ACCOUNT_NAME = deviceData.accountName;
		ASSET_ID     = deviceData.assetId;

		networkChangeReceiver = new NetworkChangeReceiver();
		networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkChangeReceiver, networkFilter);

		registerPreferenceListener();

		if (!deviceData.connectivity) {
			stopSelf();
		}
		
		localContext = getBaseContext();

		if (!networkIsConnected(localContext)) {
			int logo = R.drawable.redlogo;
			String message = "Network is not connected.";
			new EdcNotification(localContext, logo, messageTitle, message, Notification.FLAG_NO_CLEAR, EDCAndroid.class);
			stopSelf();
		}

		subTpc = new ArrayList<String>();

		mBinder = new LocalBinder<EdcConnectionService>(this);

		commandSelection = new CommandSelection(this);
		sensorSelction = new SensorSelection(this);
		sensorData = new SensorData();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);

	}



	@Override
	public int onStartCommand(final Intent intent, int flags, final int startId) {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		localIntent = intent;
		localStartId = startId;

		if (!networkIsConnected(localContext)) {
			int logo = R.drawable.redlogo;
			String message = "Network is not connected.";
			new EdcNotification(localContext, logo, messageTitle, message, Notification.FLAG_NO_CLEAR, EDCAndroid.class);
			stopSelf();
		}

		ExecutorService exec = Executors.newFixedThreadPool(1);
		exec.submit(new Runnable() {
			public void run() {
				handleStart(localIntent, localStartId);
			}
		});

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);

		return START_STICKY;
	}



	private void registerPreferenceListener() {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		preferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

				if (keyList.contains(key) || key.contains("command_") || key.contains("sensor_")) {
					commandSensorSubscribe(key);
				}
			}
		};
		prefs.registerOnSharedPreferenceChangeListener(preferenceListener);
		
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	private void commandSensorSubscribe(String key) {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EDC");
		wl.acquire();

		boolean found = false;
		for (int i = 0; i < CommandSelection.COMMAND_LIST.length; i++)
			if ((found = key.contains(CommandSelection.COMMAND_LIST[i].getTitle()))) break;

		if (found) {
			String command = key.substring(EdcConnectionService.commandPrefix.length());
			commandSelection.setStatusByName(command, !commandSelection.getStatus(command));
		}

		if (key.equals("Commands") && edcClient != null) {
			deviceData = new DeviceData(EdcConnectionService.this);
			if (deviceData.receiveCommands) {
				boolean alreadySubscribed = false;
				EdcSubscription[] subs;
				subs = edcClient.getSubscriptions();
				if (subs != null) {
					for (int i = 0; i < subs.length; i++) {
						alreadySubscribed = subs[i].getSemanticTopic().contains(commandString);
					}
				}
				if (!alreadySubscribed) {
					String topic = commandString + "/#";
					try {
						edcClient.controlSubscribe(CLIENT_ID, topic, 1);
					} catch (EdcClientException e) {
						e.printStackTrace();
					}
				}
			} 
			if (!deviceData.receiveCommands) {
				String topic = "$EDC/" + ACCOUNT_NAME + "/" + CLIENT_ID + "/" + commandString + "/#";
				try {
					edcClient.unsubscribe(topic);
				} catch (EdcClientException e) {
					e.printStackTrace();
				}
			}
		}

		if (key.equals("Automatic publishing")) {
			deviceData = new DeviceData(EdcConnectionService.this);
			if (deviceData.autopublish && !sensorsConnected) {
				connectToSensors();
				scheduleNextSensorRead();
			}
			if (!deviceData.autopublish && sensorsConnected) {
				disconnectFromSensors();
			}
		}
		if (key.startsWith("sensor_")) {
			disconnectFromSensors();
			sensorSelction = new SensorSelection(EdcConnectionService.this);
			connectToSensors();
			scheduleNextSensorRead();
		}
		if (key.equals("pref_connectivity_autopublish_freq_time")) {
			deviceData = new DeviceData(EdcConnectionService.this);
			scheduleNextSensorRead();
		}
		wl.release();

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);

		return;
	}



	void handleStart(Intent intent, int startId) {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		EdcConfigurationFactory configurationFactory = EdcConfigurationFactory.getInstance();

		CLIENT_ID = deviceData.uid;
		EdcConfiguration configuration = configurationFactory.newEdcConfiguration(ACCOUNT_NAME, CLIENT_ID, BROKER_URL, CLIENT_ID, USERNAME, PASSWORD);
		configuration.setWillMessage("Device " + CLIENT_ID + " disconnected.");
		configuration.setReconnectInterval((short) reconnectInterval);
		profileFactory = EdcDeviceProfileFactory.getInstance();
		profile = profileFactory.newEdcDeviceProfile();
		updateBirthCertificate(getBaseContext());

		try {
			edcClient = EdcClientFactory.newInstance(configuration, profile, this);
		} catch (EdcClientException e1) {
			int logo = R.drawable.yellowlogo;
			String message = "Unable to instantiate EDC client.\nCheck EDC settings.";
			new EdcNotification(localContext, logo, messageTitle, message, Notification.FLAG_NO_CLEAR, EDCAndroid.class);
			stopSelf();
			return;
		}

		int logo = R.drawable.yellowlogo;
		String message = "Network is connected.";
		if (EdcRunning()) {
			logo = R.drawable.greenlogo;
			message += "\n" + "EDC Service is running.";
		} else {
			EdcConnect();
			if (EdcRunning()) {

				commandSensorSubscribe("Commands");
				commandSensorSubscribe("Automatic publishing");

				messagePublisher = new EdcMessagePublisher();
				IntentFilter intentFilter = new IntentFilter(EdcConnectionIntents.PUBLISH_INTENT);
				registerReceiver(messagePublisher, intentFilter);

				serviceStopper = new EdcServiceStopper();
				IntentFilter stopFilter = new IntentFilter(EdcConnectionIntents.STOP_SERVICE_INTENT);
				registerReceiver(serviceStopper, stopFilter);

				topicSubscriber = new EdcTopicSubscriber();
				IntentFilter topicFilter = new IntentFilter(EdcConnectionIntents.SUBSCRIBE_INTENT);
				topicFilter.addAction(EdcConnectionIntents.TOPIC_LIST_INTENT);
				topicFilter.addAction(EdcConnectionIntents.TOPIC_UNSUBSCRIBE_INTENT);
				registerReceiver(topicSubscriber, topicFilter);

				receiverManager = new EdcMessageReceiverManager();
				IntentFilter managerFilter = new IntentFilter(EdcConnectionIntents.REGISTER_MESSAGE_RECEIVER_INTENT);
				managerFilter.addAction(EdcConnectionIntents.UNREGISTER_MESSAGE_RECEIVER_INTENT);
				registerReceiver(receiverManager, managerFilter);

				logo = R.drawable.greenlogo;
				message += "\n" + "EDC Service is running.";
				connectToSensors();
				scheduleNextSensorRead();
			} else {
				message += "\n" + "EDC Service is not running.\nCheck EDC Settings";
			}
		}
		new EdcNotification(localContext, logo, messageTitle, message, Notification.FLAG_NO_CLEAR, EDCAndroid.class);

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	private boolean EdcConnect() {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		try {
			edcClient.startSession();
			EDCAndroid.localLog("END START", EDCAndroid.ENABLE);
			return true;
		} catch (EdcClientException e) {
			EDCAndroid.localLog("END EXCEPTION", EDCAndroid.ENABLE);
			return false;
		}
	}



	public class EdcServiceStopper extends BroadcastReceiver {

		public EdcServiceStopper() {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}

		@Override
		public void onReceive(Context context, Intent intent) {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EdcDisconnect();
		}



		public void EdcDisconnect() {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

			try {
				unregisterReceiver(messagePublisher);
				unregisterReceiver(serviceStopper);
				unregisterReceiver(topicSubscriber);
				unregisterReceiver(messageReceiver);
				unregisterReceiver(receiverManager);
			}
			catch(IllegalArgumentException e) {
			}

			disconnectFromSensors();

			subTpc.clear();
			if (edcClient != null) {
				try {
					EdcSubscription[] subs;
					subs = edcClient.getSubscriptions();
					if (subs != null) {
						for (int i = 0; i < subs.length; i++) {
							try {
								edcClient.unsubscribe(subs[i]);
							} catch (EdcClientException e) {
								e.printStackTrace();
							}
						}
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}

			try {
				if (edcClient != null) {
					edcClient.stopSession();
					Thread.sleep(100);
					edcClient.terminate();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (mBinder != null) {
				mBinder.close();
				mBinder = null;
			}

			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
	}



	private void connectToSensors() {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		
		if (deviceData.autopublish) {
			if (sensorSender == null) {
				sensorSender = new SensorSender();
				registerReceiver(sensorSender, new IntentFilter(EdcConnectionIntents.EDC_SENSOR_ACTION));
			}
			gpsStatusListener = new gpsStatusListener();
			locationListener = new locationListener();
			sensorListener = new sensorEventListener();
			sensorData = new SensorData();
			if (sensorSelction.getStatusByType(SensorSelection.PSEUDO_TYPE_LOCATION))
				new Thread(new Runnable() {
					public void run() {
						Looper.prepare();
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMillis, minDistanceMeters, locationListener, Looper.myLooper());
						locationManager.addGpsStatusListener(gpsStatusListener);

						Looper.loop();
					}
				}, "LocationService").start();
			for (int i = 0; i < SensorSelection.SENSORS_TYPES.length; i++) {
				if (sensorSelction.getStatus(i) && SensorSelection.SENSORS_TYPES[i] != SensorSelection.PSEUDO_TYPE_LOCATION) {
					sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(SensorSelection.SENSORS_TYPES[i]), SensorManager.SENSOR_DELAY_NORMAL);
				}
			}
			sensorsConnected = true;
		}

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}


	private void disconnectFromSensors() {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		try {
			unregisterReceiver(sensorSender);
			sensorSender = null;
			sensorManager.unregisterListener(sensorListener);
			sensorListener = null;
			locationManager.removeUpdates(locationListener);
			locationListener = null;
			locationManager.removeGpsStatusListener(gpsStatusListener);
			gpsStatusListener = null;
		}
		catch(IllegalArgumentException e) {
		}
		sensorsConnected = false;

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	@Override
	public void onDestroy() {
		super.onDestroy();

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		try {
			unregisterReceiver(networkChangeReceiver);
			unregisterReceiver(messagePublisher);
			unregisterReceiver(serviceStopper);
			unregisterReceiver(topicSubscriber);
			unregisterReceiver(messageReceiver);
			unregisterReceiver(receiverManager);
		}
		catch(IllegalArgumentException e) {
		}
		disconnectFromSensors();
		subTpc.clear();
		EdcSubscription[] subs = null;
		if (edcClient != null)
			subs = edcClient.getSubscriptions();
		if (subs != null) {
			for (int i = 0; i < subs.length; i++)
				try {
					edcClient.unsubscribe(subs[i]);
				} catch (EdcClientException e) {
					e.printStackTrace();
				}
		}
		if (networkIsConnected(localContext)) {
			if (edcClient != null) {
				try {
					edcClient.stopSession();
					edcClient.terminate();
				} catch (EdcClientException e) {
					e.printStackTrace();
				}
			}
		}
		edcClient = null;
		if (mBinder != null) {
			mBinder.close();
			mBinder = null;
		}

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public void controlArrived(String assetId, String receivedTopic, EdcPayload payload, int qos, boolean retain) {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		if (deviceData.receiveCommands) {
			Intent controlBroadcastIntent = new Intent();
			EdcPosition position = new EdcPosition();
			position = payload.getPosition();
			Iterator<Entry<String, Object>> kvPairs = payload.metrics().entrySet().iterator();
			controlBroadcastIntent.setAction(EdcConnectionIntents.CONTROL_PUBLISH_INTENT);
			controlBroadcastIntent.putExtra("ReceivedCommand", receivedTopic);
			controlBroadcastIntent.putExtra("timestamp", payload.getTimestamp());
			while (kvPairs.hasNext()) {
				Entry<String, Object> element = kvPairs.next();
				String name = element.getKey(); Object value = element.getValue();
				if (value instanceof  Date)
					controlBroadcastIntent.putExtra(name, (Date) value);
				else if (value instanceof  Integer && value != (Integer) NULL)
					controlBroadcastIntent.putExtra(name, (Integer) value);
				else if (value instanceof  String)
					controlBroadcastIntent.putExtra(name, (String) value);
				else if (value instanceof  Float)
					controlBroadcastIntent.putExtra(name, (Float) value);
				else if (value instanceof  Double)
					controlBroadcastIntent.putExtra(name, (Double) value);
				else if (value instanceof  Long)
					controlBroadcastIntent.putExtra(name, (Long) value);
				else if (value instanceof  Boolean)
					controlBroadcastIntent.putExtra(name, (Boolean) value);
				else if (value instanceof  byte[])
					controlBroadcastIntent.putExtra(name, (byte[]) value);
				else if (value instanceof  Integer)
					controlBroadcastIntent.putExtra(name, (Integer) value);
				else if (value instanceof  Integer)
					controlBroadcastIntent.putExtra(name, (Integer) value);
				else if (value instanceof  Integer)
					controlBroadcastIntent.putExtra(name, (Integer) value);
			}
			if (position != null) {
				Double Dvalue;
				if ((Dvalue = position.getLongitude()) != null)
					controlBroadcastIntent.putExtra("longitude",  Dvalue);
				if ((Dvalue = position.getLatitude()) != null)
					controlBroadcastIntent.putExtra("latitude",   Dvalue);
				if ((Dvalue = position.getAltitude()) != null)
					controlBroadcastIntent.putExtra("altitude",   Dvalue);
				if ((Dvalue = position.getHeading()) != null)
					controlBroadcastIntent.putExtra("heading",    Dvalue);
				if ((Dvalue = position.getPrecision()) != null)
					controlBroadcastIntent.putExtra("precision",  Dvalue);
				if ((Dvalue = position.getSpeed()) != null)
					controlBroadcastIntent.putExtra("speed",      Dvalue);
				Integer Ivalue;
				if ((Ivalue = position.getSatellites()) != null)
					controlBroadcastIntent.putExtra("satellites", Ivalue);
				if ((Ivalue = position.getStatus()) != null)
					controlBroadcastIntent.putExtra("status",     Ivalue);
				Date Tvalue;
				if ((Tvalue = position.getTimestamp()) != null)
					controlBroadcastIntent.putExtra("timestamp",  Tvalue);
			}
			LocalSendBroadcast.sendBroadcast(localContext, controlBroadcastIntent);
		}

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public void publishArrived(String assetId, String receivedTopic, EdcPayload payload, int qos, boolean retain) {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		Intent broadcastIntent = new Intent();
		EdcPosition position = new EdcPosition();
		position = payload.getPosition();
		EdcTopicPattern edcSubscribedTopic;
		String assetTopic = assetId + "/" + receivedTopic;
		for (String subAssetTopic : subTpc) {
			try {
				edcSubscribedTopic = EdcTopicPattern.compile(subAssetTopic);
				if (edcSubscribedTopic.matches(assetTopic)) {
					Iterator<Entry<String, Object>> kvPairs = payload.metrics().entrySet().iterator();
					broadcastIntent.setAction(subAssetTopic);
					broadcastIntent.putExtra("AssetId", assetId);
					broadcastIntent.putExtra("ReceivedTopic", receivedTopic);
					broadcastIntent.putExtra("timestamp", payload.getTimestamp());
					while (kvPairs.hasNext()) {
						Entry<String, Object> element = kvPairs.next();
						String name = element.getKey(); Object value = element.getValue();
						if (value instanceof  Date)
							broadcastIntent.putExtra(name, (Date) value);
						else if (value instanceof  Integer && value != (Integer) NULL)
							broadcastIntent.putExtra(name, (Integer) value);
						else if (value instanceof  String)
							broadcastIntent.putExtra(name, (String) value);
						else if (value instanceof  Float)
							broadcastIntent.putExtra(name, (Float) value);
						else if (value instanceof  Double)
							broadcastIntent.putExtra(name, (Double) value);
						else if (value instanceof  Long)
							broadcastIntent.putExtra(name, (Long) value);
						else if (value instanceof  Boolean)
							broadcastIntent.putExtra(name, (Boolean) value);
						else if (value instanceof  byte[])
							broadcastIntent.putExtra(name, (byte[]) value);
						else if (value instanceof  Integer)
							broadcastIntent.putExtra(name, (Integer) value);
						else if (value instanceof  Integer)
							broadcastIntent.putExtra(name, (Integer) value);
						else if (value instanceof  Integer)
							broadcastIntent.putExtra(name, (Integer) value);
					}
					if (position != null) {
						Double Dvalue;
						if ((Dvalue = position.getLongitude()) != null)
							broadcastIntent.putExtra("longitude",  Dvalue);
						if ((Dvalue = position.getLatitude()) != null)
							broadcastIntent.putExtra("latitude",   Dvalue);
						if ((Dvalue = position.getAltitude()) != null)
							broadcastIntent.putExtra("altitude",   Dvalue);
						if ((Dvalue = position.getHeading()) != null)
							broadcastIntent.putExtra("heading",    Dvalue);
						if ((Dvalue = position.getPrecision()) != null)
							broadcastIntent.putExtra("precision",  Dvalue);
						if ((Dvalue = position.getSpeed()) != null)
							broadcastIntent.putExtra("speed",      Dvalue);
						Integer Ivalue;
						if ((Ivalue = position.getSatellites()) != null)
							broadcastIntent.putExtra("satellites", Ivalue);
						if ((Ivalue = position.getStatus()) != null)
							broadcastIntent.putExtra("status",     Ivalue);
						Date Tvalue;
						if ((Tvalue = position.getTimestamp()) != null)
							broadcastIntent.putExtra("timestamp",  Tvalue);
					}
					LocalSendBroadcast.sendBroadcast(localContext, broadcastIntent);
				}
			} catch (EdcInvalidTopicException e) {
				e.printStackTrace();
			}
		}

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public void connectionLost() {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public void connectionRestored() {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public void published(int messageId) {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public void subscribed(int messageId) {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public void unsubscribed(int messageId) {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public void controlArrived(String assetId, String topic, byte[] payload, int qos, boolean retain) {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public void publishArrived(String assetId, String topic, byte[] payload, int qos, boolean retain) {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public IBinder onBind(Intent intent) {
		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		return mBinder;
	}



	public class LocalBinder<S> extends Binder {
		private WeakReference<S> mService;

		public LocalBinder(S service) {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
			mService = new WeakReference<S>(service);
		}
		public S getService() {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
			return mService.get();
		}
		public void close() {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
			mService = null;
		}
	}



	private void updateBirthCertificate(Context context) {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		if (profile == null) return;
		profile.setDisplayName(ASSET_ID);
		profile.setModelName(deviceData.modelName);
		profile.setModelId(deviceData.modelId);
		profile.setSerialNumber(deviceData.uid);
		profile.setPartNumber(deviceData.partNumber);
		String[] net = getLocalAddresses(context);
		if (net != null && net.length > 1) {
			profile.setConnectionInterface(net[0]);
			profile.setConnectionIp(net[1]);
		}
		profile.setFirmwareVersion(android.os.Build.VERSION.RELEASE);
		profile.setBiosVersion(android.os.Build.BOOTLOADER);
		profile.setOs("Android "+android.os.Build.VERSION.CODENAME);
		profile.setOsVersion(android.os.Build.VERSION.RELEASE);
		if (sensorData != null && sensorData.hasGps()) {
			profile.setLatitude(sensorData.getLat());
			profile.setLongitude(sensorData.getLng());
		}

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}



	public static String[] getLocalAddresses(Context context) {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		String MAC = "";
		int ip = 0;
		String ADDR = "";
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		TelephonyManager mobile = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int wifiState = wifi.getWifiState();
		int dataState = mobile.getDataState();
		if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
			try {
				Enumeration<NetworkInterface> networkIfaces = NetworkInterface.getNetworkInterfaces();
				if (networkIfaces != null) {
					while (networkIfaces.hasMoreElements()) {
						NetworkInterface iface = networkIfaces.nextElement();
						byte[] mac;
						mac = iface.getHardwareAddress();
						if (mac != null) {
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < mac.length; i++) {
								sb.append(String.format("%02X%s", mac[i], (i<mac.length-1) ? ":" : ""));        
							}
							MAC = iface.getDisplayName() +" ("+sb.toString()+")";
							break;
						}
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
			ip = wifi.getConnectionInfo().getIpAddress();
			ADDR = String.format(Locale.getDefault(), "%d.%d.%d.%d",(ip & 0xff), (ip >> 8 & 0xff),(ip >> 16 & 0xff), (ip >> 24 & 0xff));
		} else if (dataState == TelephonyManager.DATA_CONNECTED) {
			MAC  = "3G - " + mobile.getNetworkOperatorName() + " (MAC address not available)";
			try {
				Enumeration<NetworkInterface> networkIfaces = NetworkInterface.getNetworkInterfaces();
				if (networkIfaces != null) {
					while (networkIfaces.hasMoreElements()) {
						NetworkInterface iface = networkIfaces.nextElement();
						byte[] mac;
						mac = iface.getHardwareAddress();
						Enumeration<InetAddress> IpAddrList = iface.getInetAddresses();
						while (IpAddrList.hasMoreElements()) {
							if (mac == null) {
								InetAddress IpAddr = IpAddrList.nextElement();
								if (!IpAddr.isLoopbackAddress()) {
									ADDR = IpAddr.getHostAddress().toString();
								}
							}
						}
					}
				}
			} catch (SocketException ex) {
				ex.printStackTrace();
			}
		}

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);

		return new String[] { MAC, ADDR };
	}



	public static boolean EdcRunning() {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		boolean returnValue = false;
		if (edcClient != null)
			returnValue = edcClient.isConnected();

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);

		return returnValue;
	}



	public static boolean networkIsConnected(Context context) {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
				return true;
			}
		}

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);

		return false;
	}



	public static String networkType() {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		ConnectivityManager cm = (ConnectivityManager) EDCAndroid.startingActivityContext.getSystemService(Context.CONNECTIVITY_SERVICE);

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);

		return cm.getActiveNetworkInfo().getTypeName();
	}



	public class EdcMessagePublisher extends BroadcastReceiver {

		public EdcMessagePublisher() {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}

		@Override
		public void onReceive(Context context, Intent intent) {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

			EdcPayload payload = new EdcPayload();
			String topics = null;
			String assets = null;
			Bundle notificationData = intent.getExtras();
			Iterator<String> keys = notificationData.keySet().iterator();
			while (keys.hasNext()) {
				String name = keys.next(); Object value = notificationData.get(name);
				if (name.contentEquals(EdcConnectionIntents.PUBLISHED_ASSET))
					assets = notificationData.getString(EdcConnectionIntents.PUBLISHED_ASSET);
				if (name.contentEquals(EdcConnectionIntents.PUBLISHED_TOPIC))
					topics = notificationData.getString(EdcConnectionIntents.PUBLISHED_TOPIC);
				if (value instanceof  Date) {
					payload.setTimestamp((Date) value);
				} else if (value instanceof  Integer && value != (Integer) NULL) {
					payload.addMetric(name, (Integer) value);
				} else if (value instanceof  String && !name.contentEquals(EdcConnectionIntents.PUBLISHED_TOPIC) && !name.contentEquals(EdcConnectionIntents.PUBLISHED_ASSET)) {
					payload.addMetric(name, (String) value);
				} else if (value instanceof  Float) {
					payload.addMetric(name, (Float) value);
				} else if (value instanceof  Double) {
					payload.addMetric(name, (Double) value);
				} else if (value instanceof  Long) {
					payload.addMetric(name, (Long) value);
				} else if (value instanceof  Boolean) {
					payload.addMetric(name, (Boolean) value);
				} else if (value instanceof  byte[]) {
					payload.addMetric(name, (byte[]) value);
				} else if (value instanceof  Integer) {
					payload.addMetric(name, (Integer) value);
				} else if (value instanceof  Integer) {
					payload.addMetric(name, (Integer) value);
				} else if (value instanceof  Integer) {
					payload.addMetric(name, (Integer) value);
				} else if (value instanceof  EdcPosition) {
					payload.setPosition((EdcPosition) value);
				}
			}
			StringTokenizer assetTokenizer = new StringTokenizer(assets, ",");
			while (assetTokenizer.hasMoreTokens()) {
				StringTokenizer topicTokenizer = new StringTokenizer(topics, ",");
				String asset = assetTokenizer.nextToken();
				while (topicTokenizer.hasMoreTokens()) {
					String topic = topicTokenizer.nextToken();
					if (topic.contains("$EDC/")) {
						topic = topic.substring("$EDC/".length());
						if (edcClient != null && networkIsConnected(context) && EdcRunning()) {
							try {
								edcClient.controlPublish(asset, topic, payload, 1, false);
							} catch (EdcClientException e) {
								e.printStackTrace();
							}
						}
					}
					else {
						if (edcClient != null && networkIsConnected(context) && EdcRunning()) {
							try { edcClient.publish(asset, topic, payload, 1, false); }
							catch (EdcClientException e) { e.printStackTrace(); }
						}
					}
				}
			}

			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
	}



	public class EdcTopicSubscriber extends BroadcastReceiver {

		public EdcTopicSubscriber() {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}

		@Override
		public void onReceive(Context context, Intent intent) {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

			if (intent.getAction().matches(EdcConnectionIntents.SUBSCRIBE_INTENT)) {
				Bundle extras = intent.getExtras();
				String subscribedAssets = extras.getString(EdcConnectionIntents.SUBSCRIBED_ASSETS);
				String subscribedTopics = extras.getString(EdcConnectionIntents.SUBSCRIBED_TOPICS);
				StringTokenizer assetTokenizer = new StringTokenizer(subscribedAssets, ",");
				while (assetTokenizer.hasMoreTokens()) {
					StringTokenizer topicTokenizer = new StringTokenizer(subscribedTopics, ",");
					String asset = assetTokenizer.nextToken();
					while (topicTokenizer.hasMoreTokens()) {
						String topic = topicTokenizer.nextToken();
						String assetTopic = asset + "/" + topic;
						if (!subTpc.contains(assetTopic)) {
							try {
								edcClient.subscribe(asset, topic, 1);
								subTpc.add(assetTopic);
							} catch (EdcClientException e) {
								e.printStackTrace();
							}
						}
					}
				}
			} else if (intent.getAction().matches(EdcConnectionIntents.TOPIC_LIST_INTENT)) {
				String subscribedTopicList = "";
				try {
					EdcSubscription[] subs;
					subs = edcClient.getSubscriptions();
					for (int i = 0; i < subs.length; i++) {
						if (subs[i].getSemanticTopic().contains("$EDC")) continue;
						subscribedTopicList += subs[i].getSemanticTopic().substring(new String(USERNAME + "/").length()) + ",";
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				Intent topicListIntent = new Intent();
				topicListIntent.setAction(EdcConnectionIntents.TOPIC_LIST_REPLY);
				topicListIntent.putExtra(EdcConnectionIntents.SUBSCRIPTION_LIST, subscribedTopicList);
				LocalSendBroadcast.sendBroadcast(context, topicListIntent);
			} else if (intent.getAction().matches(EdcConnectionIntents.TOPIC_UNSUBSCRIBE_INTENT)) {
				Bundle extras = intent.getExtras();
				String unsubscribedTopicList = extras.getString(EdcConnectionIntents.UNSUBSCRIBED_TOPICS);
				StringTokenizer topicTokenizer = new StringTokenizer(unsubscribedTopicList, ",");
				while (topicTokenizer.hasMoreTokens()) {
					String assetTopic = topicTokenizer.nextToken();
					String asset = assetTopic.substring(0, assetTopic.indexOf("/"));
					String semanticTopic = assetTopic.substring(asset.length() + 1, assetTopic.length());
					if (subTpc.contains(assetTopic)) {
						try {
							edcClient.unsubscribe(asset, semanticTopic);
							subTpc.remove(asset + "/" + semanticTopic);
						} catch (EdcClientException e) {
							e.printStackTrace();
						}
					}
				}
			}

			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
	}



	private class NetworkChangeReceiver extends BroadcastReceiver {

		Thread t;

		public NetworkChangeReceiver() {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}

		@SuppressLint("Wakelock")
		@Override
		public void onReceive(final Context context, Intent intent) {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EDC");
			wl.acquire();

			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo currentNetworkInfo = cm.getActiveNetworkInfo();
			if (currentNetworkInfo != null) {
				State state = currentNetworkInfo.getState();
				String connection = currentNetworkInfo.getTypeName();
				switch (state) {
				case CONNECTED:
					if (firstConnection) {
						updateBirthCertificate(context);
						firstConnection = false;
					} else {
						updateBirthCertificate(context);
						int logo;
						t = new Thread() {
							public void run() {
								int n = 0;
								while(!EdcRunning() && n < maxNetworkConnectionWaitSeconds) {
									try {
										Thread.sleep(1 * millis);
										n++;
									}
									catch (InterruptedException e) { }
								}
								t.interrupt();
							}
						};
						t.start(); 
						try {
							t.join();
						} catch (InterruptedException e) { }
						String message;
						if (networkIsConnected(context) && EdcRunning())
							logo = R.drawable.greenlogo;
						else if (networkIsConnected(context))
							logo = R.drawable.yellowlogo;
						else
							logo = R.drawable.redlogo;
						message = "Network connected to " + connection + " network.";
						new EdcNotification(context, logo, messageTitle, message, Notification.FLAG_NO_CLEAR, EDCAndroid.class);
					}
					break;
				case DISCONNECTED:
					int logo = R.drawable.redlogo;
					String message = "Network disconnected.";
					new EdcNotification(context, logo, messageTitle, message, Notification.FLAG_NO_CLEAR, EDCAndroid.class);
					break;
				default:
					break;
				}
			} else {
				int logo = R.drawable.redlogo;
				String message = "Network is not connected.";
				new EdcNotification(context, logo, messageTitle, message, Notification.FLAG_NO_CLEAR, EDCAndroid.class);
			}
			wl.release();

			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
	}



	public class SensorSender extends BroadcastReceiver {

		public SensorSender() {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}

		@Override
		public void onReceive(Context context, Intent intent) {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

			if (sensorData != null && sensorData.length() > 0) {
				EdcPayload edcPayload = new EdcPayload();
				Date capturedOn = new Date();
				edcPayload.setTimestamp(capturedOn);
				boolean doPublish = false;

				for (String name: sensorData.getKeys()) {
					if (sensorData.hasItChanged(name)) doPublish = true;
					if (name != SensorData.LAT 
							&& name != SensorData.LNG 
							&& name != SensorData.ALT
							&& name != SensorData.HDG
							&& name != SensorData.SPD 							
							&& name != SensorData.SAT
							&& name != SensorData.PRE 								
							&& doPublish) {
						edcPayload.addMetric(name, sensorData.get(name));
						sensorData.hasChanged(name, false);
					}
				}

				if (doPublish && sensorData.hasGps()) {
					EdcPosition position = new EdcPosition();
					position.setLatitude(sensorData.getLat());
					sensorData.hasChanged(SensorData.LAT, false);
					position.setLongitude(sensorData.getLng());
					sensorData.hasChanged(SensorData.LNG, false);
					position.setAltitude(sensorData.getAlt());
					sensorData.hasChanged(SensorData.ALT, false);
					position.setSpeed(sensorData.getSpd());
					sensorData.hasChanged(SensorData.SPD, false);
					position.setHeading(sensorData.getHdg());
					sensorData.hasChanged(SensorData.HDG, false);
					position.setPrecision(sensorData.getPre());
					sensorData.hasChanged(SensorData.PRE, false);
					position.setSatellites((int)sensorData.getSat());
					sensorData.hasChanged(SensorData.SAT, false);
					position.setStatus(NULL);
					position.setTimestamp(capturedOn);
					edcPayload.setPosition(position);
				}
				if (edcClient != null && networkIsConnected(context) && EdcRunning() && doPublish) {
					try { edcClient.publish("sensors/", edcPayload, 0, false); }
					catch (EdcClientException e) {
						e.printStackTrace();
					}
				}
			}
			if (deviceData.autopublish) scheduleNextSensorRead();

			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
	}



	private void scheduleNextSensorRead() {

		EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(EdcConnectionIntents.EDC_SENSOR_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
		Calendar wakeUpTime = Calendar.getInstance();
		wakeUpTime.add(Calendar.SECOND, deviceData.sensorInterval);
		AlarmManager aMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
		aMgr.set(AlarmManager.RTC_WAKEUP, wakeUpTime.getTimeInMillis(), pendingIntent);

		EDCAndroid.localLog("END", EDCAndroid.ENABLE);
	}




	public class sensorEventListener implements SensorEventListener {

		@Override
		public final void onAccuracyChanged(Sensor sensor, int accuracy) {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}

		@Override
		public final void onSensorChanged(SensorEvent event) {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EDC");
			wl.acquire();

			if (event.values.length == 1) {
				String name = SensorSelection.SENSORS_NAMES[SensorSelection.SENSORS_CROSS_REFERENCE[event.sensor.getType()]].replaceAll("\\s+", "_") + "_";
				sensorData.set(name, event.values[0]);
				sensorData.hasChanged(name, true);
			} else {
				for (int i = 0; i < event.values.length; i++) {
					String name = SensorSelection.SENSORS_NAMES[SensorSelection.SENSORS_CROSS_REFERENCE[event.sensor.getType()]].replaceAll("\\s+", "_") + "_" + (i+1);
					sensorData.set(name, event.values[i]);
					sensorData.hasChanged(name, true);
				}
			}
			wl.release();

			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
	}



	public class locationListener implements LocationListener {

		public locationListener() {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}



		public void onLocationChanged(Location loc) {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EDC");
			wl.acquire();


			if (loc != null) {
				try {
					if (loc.hasAccuracy() && loc.getAccuracy() <= minAccuracyMeters) {
						sensorData.set(SensorData.LAT, loc.getLatitude());
						sensorData.hasChanged(SensorData.LAT, true);
						sensorData.set(SensorData.LNG, loc.getLongitude());
						sensorData.hasChanged(SensorData.LNG, true);
						sensorData.set(SensorData.ALT, loc.getAltitude());
						sensorData.hasChanged(SensorData.ALT, true);
						sensorData.set(SensorData.SPD, loc.getSpeed());
						sensorData.hasChanged(SensorData.SPD, true);
						sensorData.set(SensorData.HDG, loc.getBearing());
						sensorData.hasChanged(SensorData.HDG, true);
						sensorData.set(SensorData.PRE, loc.getAccuracy());
						sensorData.hasChanged(SensorData.PRE, true);
						sensorData.set(SensorData.SAT, VisibleSatellites);
						sensorData.hasChanged(SensorData.SAT, true);
					} 
				} catch (Exception e) {
				}
			}
			wl.release();

			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}

		public void onProviderDisabled(String provider) {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
		public void onProviderEnabled(String provider) {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
		public void onStatusChanged(String provider, int status, Bundle extras) {
			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);
			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
	}



	private class gpsStatusListener implements GpsStatus.Listener {

		public void onGpsStatusChanged(int event) {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

			LocationManager locationManager = (LocationManager) localContext.getSystemService(
					Context.LOCATION_SERVICE);

			GpsStatus status = locationManager.getGpsStatus(null);
			VisibleSatellites = 0;
			Iterator<GpsSatellite> it = status.getSatellites().iterator();
			while (it.hasNext()) {
				it.next();
				VisibleSatellites++;
			}

			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
	}




	private class EdcMessageReceiverManager extends BroadcastReceiver {

		public EdcMessageReceiverManager() {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

			messageReceiver = new EdcMessageReceiver();
			IntentFilter messageFilter = new IntentFilter();
			for (String topic : EDCAndroid.semanticTopicList) {
				messageFilter.addAction(topic);
			}
			registerReceiver(messageReceiver, messageFilter);

			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}

		@Override
		public void onReceive(Context context, Intent intent) {

			EDCAndroid.localLog("BEGIN", EDCAndroid.ENABLE);

			if (intent.getAction().matches(EdcConnectionIntents.UNREGISTER_MESSAGE_RECEIVER_INTENT)) {
				try {
					unregisterReceiver(messageReceiver);
				}
				catch(IllegalArgumentException e) {
				}
			} else if (intent.getAction().matches(EdcConnectionIntents.REGISTER_MESSAGE_RECEIVER_INTENT)) {
				IntentFilter messageFilter = new IntentFilter();
				for (String topic : EDCAndroid.semanticTopicList)
					messageFilter.addAction(topic);
				registerReceiver(messageReceiver, messageFilter);
			}

			EDCAndroid.localLog("END", EDCAndroid.ENABLE);
		}
	}

}