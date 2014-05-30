package com.eurotech.edcdroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.eurotech.edcdroid.commands.Command;
import com.eurotech.edcdroid.gateway.EdcConnectionIntents;
import com.eurotech.edcdroid.gateway.EdcConnectionService;
import com.eurotech.edcdroid.gateway.EdcReceiveActivity;
import com.eurotech.edcdroid.gateway.EdcSubscribeActivity;
import com.eurotech.edcdroid.gateway.LocalSendBroadcast;
import com.eurotech.edcdroid.settings.SettingsActivity;
import com.eurotech.edcdroid.tools.DeviceData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class Demo extends Activity {

	public static String ASSET_ID;
	private static int nMess = 1;
	public static ArrayList<String> semanticTopicList;
	public static Context startingActivityContext;
	public static GridView gridView;
	private final boolean active = true;
	private ControlMessageReceiver controlMessageReceiver;
	private static boolean firstStart = true;
	private static OnSharedPreferenceChangeListener connectionPreferenceListener;
	public static boolean usernameDone     = false;
	public static boolean passwordDone     = false;
	public static boolean brokerDone       = false;
	public static boolean accountDone      = false;
	public static boolean assetDone        = false;
	public static boolean allSetsDone      = false;
	public static boolean somethingChanged = false;
	public static final String controlString = "\\p{Cntrl}";
	public static String logFileName = "logFile";
	public static File  logFile = null;
	public static FileOutputStream logFileOutputStream;
	public static RandomAccessFile logFileWriter;
	public static FileChannel logFileChannel;
	public static int i = 0;
	
	//
	// Enable DEBUG output here!!
	//
	public static final Boolean ENABLE = false;

	private enum SELECTION {
		PUBLISH, RECEIVE, COMMAND, TOPICS
	};

	private static final ArrayList<String> keyList = new ArrayList<String>(
			Arrays.asList(
					"Cloud connection",
					"pref_connectivity_connection_username",
					"pref_connectivity_connection_password",
					"pref_connectivity_connection_broker",
					"pref_connectivity_connection_account",
					"pref_connectivity_connection_assetID"
					));

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		localLog("BEGIN", ENABLE);

		startingActivityContext = getApplicationContext();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(startingActivityContext);
		usernameDone = !prefs.getString("pref_connectivity_connection_username", "").isEmpty();
		passwordDone = !prefs.getString("pref_connectivity_connection_password", "").isEmpty();
		brokerDone   = !prefs.getString("pref_connectivity_connection_broker", "").isEmpty();
		accountDone  = !prefs.getString("pref_connectivity_connection_account", "").isEmpty();
		assetDone    = !prefs.getString("pref_connectivity_connection_assetID", "").isEmpty();
		allSetsDone  = usernameDone && passwordDone && brokerDone && accountDone && assetDone;

		semanticTopicList = new ArrayList<String>();
		EdcConnectionService.deviceData = new DeviceData(this);
		ASSET_ID = EdcConnectionService.deviceData.assetId;


		registerConnectionPreferenceListener();

		controlMessageReceiver = new ControlMessageReceiver();
		IntentFilter intentFilter = new IntentFilter(EdcConnectionIntents.CONTROL_PUBLISH_INTENT);
		registerReceiver(controlMessageReceiver, intentFilter);

		setContentView(R.layout.main);

		final TileAdapter ta = new TileAdapter(this);
		final Map<SELECTION, Tile> selectedTile = new HashMap<SELECTION, Tile>();
		int nTile = 0;

		ta.addTile(new Tile(R.drawable.send,    "Publish", "Send a message to EDC", active));
		selectedTile.put(SELECTION.values()[nTile], (Tile) ta.getItem(nTile)); nTile++;
		ta.addTile(new Tile(R.drawable.receive, "Receive", "Receive EDC messages", active));
		selectedTile.put(SELECTION.values()[nTile], (Tile) ta.getItem(nTile)); nTile++;
		ta.addTile(new Tile(R.drawable.controlsend,   "Command", "Send a command to EDC", active));
		selectedTile.put(SELECTION.values()[nTile], (Tile) ta.getItem(nTile)); nTile++;
		ta.addTile(new Tile(R.drawable.topic,  "Topics", "Subscribe/unsubscribe", active));
		selectedTile.put(SELECTION.values()[nTile], (Tile) ta.getItem(nTile)); nTile++;

		int tileVerticalSize = ta.tiles.get(0).vSize;

		gridView = (GridView) findViewById(R.id.grid_view);
		gridView.setAdapter(ta);

		Display mDisplay = this.getWindowManager().getDefaultDisplay();
		Point outSize = new Point();
		mDisplay.getSize(outSize);
		int displaySize = outSize.x;
		Rect rectgle = new Rect();
		Window window = getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
		int statusBarHeight = rectgle.top;

		int verticalPadding = (displaySize - (nTile / 2) * tileVerticalSize) / 4 + 2 * statusBarHeight;
		gridView.setPadding(0, verticalPadding, 0, 0);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				SELECTION sel = SELECTION.values()[position];
				switch (sel) {
				case PUBLISH:
					if (!selectedTile.get(sel).active) break;
					publishMessage();
					break;
				case RECEIVE:
					if (!selectedTile.get(sel).active) break;
					Context receiveContext = startingActivityContext;
					Intent receiveIntent = new Intent (receiveContext, EdcReceiveActivity.class );
					receiveIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					receiveContext.startActivity(receiveIntent);
					break;
				case COMMAND:
					if (!selectedTile.get(sel).active) break;
					controlPublishMessage();
					break;
				case TOPICS:
					if (!selectedTile.get(sel).active) break;
					LinearLayout layout = new LinearLayout(Demo.this);
					layout.setOrientation(LinearLayout.VERTICAL);
					Context subscribeContext = startingActivityContext;
					Intent subscribeIntent = new Intent (subscribeContext, EdcSubscribeActivity.class);
					subscribeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					subscribeContext.startActivity(subscribeIntent);
					break;
				}
			}
		});

		if (firstStart) {
			firstStart = false;
			int logo;
			String message = "";
			if (!EdcConnectionService.networkIsConnected(startingActivityContext)) {
				logo = R.drawable.redlogo;
				message = "Network is not connected.";
				EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
			} else {
				logo = R.drawable.yellowlogo;
				message = "Network is connected.";
				if (EdcConnectionService.deviceData.connectivity) {
					if (!EdcConnectionService.EdcRunning() && allSetsDone) {
						EdcStart();
					} else {
						logo = R.drawable.greenlogo;
						message += " " + "EDC service is running.";
						EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
					}
				} else {
					message += " " + "EDC service is not running.";
					EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
				}
			}
		} else if (allSetsDone) {
			EdcStart();
		}
		localLog("END", ENABLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		localLog("BEGIN", ENABLE);
		localLog("END", ENABLE);
	}

	protected void onDestroy() {
		super.onDestroy();
		localLog("BEGIN", ENABLE);
		unregisterReceiver(controlMessageReceiver);
		localLog("END", ENABLE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		localLog("BEGIN", ENABLE);
		localLog("END", ENABLE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		localLog("BEGIN", ENABLE);
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_about:
			// get the last build date of the App & report in a simple dialog
			String builddate = null;
			try{
				     ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
				     ZipFile zf = new ZipFile(ai.sourceDir);
				     ZipEntry ze = zf.getEntry("classes.dex");
				     long dextime = ze.getTime();
				     DateFormat df = new SimpleDateFormat("dd/MMM/yyyy - hh:mm:ss", Locale.getDefault());
				     builddate = df.format(new java.util.Date(dextime));
				     zf.close();
				  }catch(Exception e){
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("EDC Android");
			builder.setMessage("Build Date: " + builddate );
			builder.setPositiveButton("OK", null);
			AlertDialog dialog = builder.show();

			TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
			messageView.setGravity(Gravity.CENTER);
			
			return true;
		case R.id.menu_exit:
			localLog("END FINISH", ENABLE);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		localLog("BEGIN", ENABLE);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_home, menu);
		localLog("END", ENABLE);
		return true;
	}

	public void EdcStart() {
		localLog("BEGIN", ENABLE);
		if (!EdcConnectionService.EdcRunning()) {
			stopService(new Intent(this, EdcConnectionService.class));
			startService(new Intent(this, EdcConnectionService.class));
		}
		localLog("END", ENABLE);
	}

	private void registerConnectionPreferenceListener() {
		
		localLog("BEGIN", ENABLE);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		connectionPreferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				
				localLog("BEGIN", ENABLE);

				if (keyList.contains(key)) {

					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(startingActivityContext);
					usernameDone = !pref.getString("pref_connectivity_connection_username", "").isEmpty();
					passwordDone = !pref.getString("pref_connectivity_connection_password", "").isEmpty();
					brokerDone   = !pref.getString("pref_connectivity_connection_broker", "").isEmpty();
					accountDone  = !pref.getString("pref_connectivity_connection_account", "").isEmpty();
					assetDone    = !pref.getString("pref_connectivity_connection_assetID", "").isEmpty();
					allSetsDone  = usernameDone && passwordDone && brokerDone && accountDone && assetDone;

					PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
					WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EDC");
					wl.acquire();

					if (key.equals("Cloud connection")) {
						EdcConnectionService.deviceData = new DeviceData(Demo.this);
						if (EdcConnectionService.deviceData.connectivity) {
							startService(new Intent(Demo.this, EdcConnectionService.class));				
						}
						if (!EdcConnectionService.deviceData.connectivity) {
							stopService(new Intent(Demo.this, EdcConnectionService.class));
							int logo;
							String message;
							if (EdcConnectionService.networkIsConnected(startingActivityContext)) {
								logo = R.drawable.yellowlogo;
								message = "Network is connected.";
							} else {
								logo = R.drawable.redlogo;
								message = "Network is not connected.";
							}
							message += " " + "EDC service is not running.";
							EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
						}
					}
					if (somethingChanged && allSetsDone) {
						somethingChanged = false;
						stopService(new Intent(Demo.this, EdcConnectionService.class));
						EdcConnectionService.deviceData = new DeviceData(Demo.this);
						startService(new Intent(Demo.this, EdcConnectionService.class));
					}
				}
				localLog("END", ENABLE);
			}
		};

		prefs.registerOnSharedPreferenceChangeListener(connectionPreferenceListener);
		localLog("END", ENABLE);
	}

	private void publishMessage() {
		
		localLog("BEGIN", ENABLE);

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		final EditText inputAssets = new EditText(this);
		inputAssets.setHint("<Comma-separated list of assets>");
		final EditText inputTopics = new EditText(this);
		inputTopics.setHint("Comma-separated list of topics");
		final EditText inputMessage = new EditText(this);
		inputMessage.setHint("Message");
		alert.setTitle("EDC Message Sender");
		layout.addView(inputAssets);
		layout.addView(inputTopics);
		layout.addView(inputMessage);
		alert.setView(layout);

		alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String assets = inputAssets.getText().toString().trim().replaceAll("\\s","");
				if (assets.isEmpty()) assets = EdcConnectionService.ASSET_ID;
				String semanticTopics = inputTopics.getText().toString().trim().replaceAll("\\s","");
				String message = inputMessage.getText().toString().trim();
				broadcastPublishMessage(assets, semanticTopics, createPayload(nMess++, message));
			}});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener () {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}});
		alert.show();
		localLog("END", ENABLE);
	}

	private void controlPublishMessage() {
		
		localLog("BEGIN", ENABLE);

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		final EditText inputControlAssets = new EditText(this);
		inputControlAssets.setHint("<Comma-separated list of assets>");
		final EditText inputControlTopics = new EditText(this);
		inputControlTopics.setHint("Comma-separated list of control topics");
		final EditText inputControlMessage = new EditText(this);
		inputControlMessage.setHint("Message");
		alert.setTitle("EDC Control Message Sender");
		layout.addView(inputControlAssets);
		layout.addView(inputControlTopics);
		layout.addView(inputControlMessage);
		alert.setView(layout);

		alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String controlAssets = inputControlAssets.getText().toString().trim().replaceAll("\\s","");
				if (controlAssets.isEmpty()) controlAssets = EdcConnectionService.ASSET_ID;
				String controlSemanticTopics = "$EDC/" + inputControlTopics.getText().toString().trim().replaceAll("\\s","");
				String controlMessage = inputControlMessage.getText().toString().trim();
				broadcastPublishMessage(controlAssets, controlSemanticTopics, createPayload(nMess++, controlMessage));
			}});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener () {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}});
		alert.show();
		localLog("END", ENABLE);
	}

	private void broadcastPublishMessage(String assetList, String topicList, Map<String,Object> metrics) {
		
		localLog("BEGIN", ENABLE);

		Intent edcIntent = new Intent();
		Iterator<Entry<String, Object>> kvPairs = metrics.entrySet().iterator();
		while (kvPairs.hasNext()) {
			Entry<String, Object> element = kvPairs.next();
			String name = element.getKey(); Object value = element.getValue();
			if (value instanceof  Date) {
				edcIntent.putExtra(name, (Date) value);
			} else if (value instanceof  Integer) {
				edcIntent.putExtra(name, (Integer) value);
			} else if (value instanceof  String) {
				edcIntent.putExtra(name, (String) value);
			} else if (value instanceof  Float) {
				edcIntent.putExtra(name, (Float) value);
			} else if (value instanceof  Double) {
				edcIntent.putExtra(name, (Double) value);
			} else if (value instanceof  Long) {
				edcIntent.putExtra(name, (Long) value);
			} else if (value instanceof  Boolean) {
				edcIntent.putExtra(name, (Boolean) value);
			} else if (value instanceof  Byte[]) {
				edcIntent.putExtra(name, (Byte[]) value);
			} else if (value instanceof  Integer) {
				edcIntent.putExtra(name, (Integer) value);
			} else if (value instanceof  Integer) {
				edcIntent.putExtra(name, (Integer) value);
			} else if (value instanceof  Integer) {
				edcIntent.putExtra(name, (Integer) value);
			}
		}
		edcIntent.setAction(EdcConnectionIntents.PUBLISH_INTENT);
		edcIntent.putExtra(EdcConnectionIntents.PUBLISHED_ASSET, assetList);
		edcIntent.putExtra(EdcConnectionIntents.PUBLISHED_TOPIC, topicList);
		LocalSendBroadcast.sendBroadcast(startingActivityContext, edcIntent);
		
		localLog("END", ENABLE);
	}

	private Map<String,Object> createPayload(int counter, String message) {
		localLog("BEGIN", ENABLE);
		Map<String,Object> metrics = new HashMap<String,Object>();
		Date capturedOn = new Date();
		metrics.put("timestamp", (Date)    capturedOn);
		metrics.put("message",   (String)  message);
		localLog("END", ENABLE);
		return metrics;
	}

	public class ControlMessageReceiver extends BroadcastReceiver {

		public ControlMessageReceiver() {
			localLog("BEGIN", ENABLE);
			localLog("END", ENABLE);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			localLog("BEGIN", ENABLE);
			Bundle extras = intent.getExtras();
			String receivedCommand = extras.getString("ReceivedCommand");
			String receivedMessage = extras.getString("message");
			if (receivedCommand.contains(EdcConnectionService.commandString)) {
				Command command;
				String key = receivedCommand.substring((EdcConnectionService.commandString + "/").length(), (EdcConnectionService.commandString + "/").length() + 1).toUpperCase(Locale.getDefault()) + receivedCommand.substring((EdcConnectionService.commandString + "/").length() + 1);
				command = EdcConnectionService.commandSelection.get(key);
				if (EdcConnectionService.commandSelection.getStatus(key))
					command.execute(context, receivedMessage);
			}
			localLog("END", ENABLE);
		}
	}

	public static void localLog(String message, boolean enabled) {
		if (!enabled) return;
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[3];
		String methodName = e.getMethodName();
		String className = e.getClassName();
		Log.d(ASSET_ID, className.substring(className.lastIndexOf(".") + 1) + " " + methodName + ": " + message);
		if (logFile != null) {
			try {
				String messageToSend = ASSET_ID + ": " + className.substring(className.lastIndexOf(".") + 1) + " " + methodName + ": " + message + "\n";
				messageToSend.replaceAll(controlString, "");
				logFileWriter.writeChars(messageToSend);
			} catch (IOException e1) {
				int logo = R.drawable.redlogo;
				message = "Cannot write to logfile.";
				EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
			}
			try {
				logFileOutputStream.getFD().sync();
			} catch (SyncFailedException e1) {
				int logo = R.drawable.redlogo;
				message = "Cannot flush logfile output.";
				EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
			} catch (IOException e1) {
				int logo = R.drawable.redlogo;
				message = "Cannot flush logfile output.";
				EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
			}
		}
	}

	public static void localLog(String message) {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[3];
		String methodName = e.getMethodName();
		String className = e.getClassName();
		Log.d(ASSET_ID, className.substring(className.lastIndexOf(".") + 1) + " " + methodName + ": " + message);
		if (logFile != null) {
			try {
				String messageToSend = ASSET_ID + ": " + className.substring(className.lastIndexOf(".") + 1) + " " + methodName + ": " + message + "\n";
				messageToSend.replaceAll(controlString, "");
				logFileWriter.writeChars(messageToSend);
			} catch (IOException e1) {
				int logo = R.drawable.redlogo;
				message = "Cannot write to logfile.";
				EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
			}
			try {
				logFileOutputStream.getFD().sync();
			} catch (SyncFailedException e1) {
				int logo = R.drawable.redlogo;
				message = "Cannot flush logfile output.";
				EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
			} catch (IOException e1) {
				int logo = R.drawable.redlogo;
				message = "Cannot flush logfile output.";
				EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
			}
		}
	}
	public static void localLog() {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[3];
		String methodName = e.getMethodName();
		String className = e.getClassName();
		Log.d(ASSET_ID, className.substring(className.lastIndexOf(".") + 1) + " " + methodName);
		if (logFile != null) {
			try {
				String messageToSend = ASSET_ID + ": " + className.substring(className.lastIndexOf(".") + 1) + " " + methodName + "\n";
				messageToSend.replaceAll(controlString, "");
				logFileWriter.writeChars(messageToSend);
			} catch (IOException e1) {
				int logo = R.drawable.redlogo;
				String message = "Cannot write to logfile.";
				EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
			}
			try {
				logFileOutputStream.getFD().sync();
			} catch (SyncFailedException e1) {
				int logo = R.drawable.redlogo;
				String message = "Cannot flush logfile output.";
				EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
			} catch (IOException e1) {
				int logo = R.drawable.redlogo;
				String message = "Cannot flush logfile output.";
				EdcConnectionService.EdcNotification(startingActivityContext, logo, EdcConnectionService.messageTitle, message, Notification.FLAG_NO_CLEAR, Demo.class);
			}
		}
	}
}
