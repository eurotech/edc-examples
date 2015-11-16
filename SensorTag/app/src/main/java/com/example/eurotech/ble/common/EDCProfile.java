package com.example.eurotech.ble.common;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.eurotech.cloud.client.EdcCallbackHandler;
import com.eurotech.cloud.client.EdcClientException;
import com.eurotech.cloud.client.EdcClientFactory;
import com.eurotech.cloud.client.EdcCloudClient;
import com.eurotech.cloud.client.EdcConfiguration;
import com.eurotech.cloud.client.EdcConfigurationFactory;
import com.eurotech.cloud.client.EdcDeviceProfile;
import com.eurotech.cloud.client.EdcDeviceProfileFactory;
import com.eurotech.cloud.message.EdcPayload;
import com.example.eurotech.ble.sensortag.R;
import com.example.eurotech.ble.sensortag.SensorTagLedAndBuzzerProfile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pierantonio.merlino on 04/11/15.
 */
public class EDCProfile extends GenericBluetoothProfile implements EdcCallbackHandler {
    private String addrShort;
    static EDCProfile mThis;
    Map<String, String> valueMap = new HashMap<String, String>();
    Timer publishTimer;
    public boolean ready;
    private PowerManager.WakeLock wakeLock;
    BroadcastReceiver cloudConfigUpdateReceiver;
    cloudConfig config;

    SensorTagLedAndBuzzerProfile leds;

    private static EdcDeviceProfileFactory profileFactory;
    private static EdcDeviceProfile profile;

    public static EdcCloudClient edcClient = null;

    public EDCProfile(final Context con, BluetoothDevice device, BluetoothGattService service, BluetoothLeService controller) {
        super(con, device, service, controller);
        this.tRow =  new EDCTableRow(con);
        this.tRow.setOnClickListener(null);

        config = readCloudConfigFromPrefs();

        if (config != null) {
            Log.d("EDCCloudProfile", "Stored cloud configuration" + "\r\n" + config.toString());
        }
        else {
            config = initPrefsWithEDC();
            Log.d("EDCCloudProfile", "Stored cloud configuration was corrupt, starting new based on EDC variables" + config.toString());
        }

        EdcConfigurationFactory configurationFactory = EdcConfigurationFactory.getInstance();

        short reconnectInterval = 5;

        EdcConfiguration configuration = configurationFactory.newEdcConfiguration(config.account, config.assetId, config.brokerAddress + ":" + config.brokerPort,
                config.clientId, config.username, config.password);
        configuration.setWillMessage("Device " + config.clientId + " disconnected.");
        configuration.setReconnectInterval((short) reconnectInterval);
        profileFactory = EdcDeviceProfileFactory.getInstance();
        profile = profileFactory.newEdcDeviceProfile();
        updateBirthCertificate();

        try {
            edcClient = EdcClientFactory.newInstance(configuration, profile, this);
        } catch (EdcClientException e1) {
            String message = "Unable to instantiate EDC client.\nCheck EDC settings.";
            return;
        }



        String addr = mBTDevice.getAddress();
        String[] addrSplit = addr.split(":");
        int[] addrBytes = new int[6];
        for (int ii = 0; ii < 6; ii++) {
            addrBytes[ii] = Integer.parseInt(addrSplit[ii], 16);
        }
        ready = false;
        this.addrShort = String.format("%02x%02x%02x%02x%02x%02x",addrBytes[0],addrBytes[1],addrBytes[2],addrBytes[3],addrBytes[4],addrBytes[5]);
        Log.d("EDCCloudProfile", "Device ID : " + addrShort);
        this.tRow.sl1.setVisibility(View.INVISIBLE);
        this.tRow.sl2.setVisibility(View.INVISIBLE);
        this.tRow.sl3.setVisibility(View.INVISIBLE);
        this.tRow.title.setText("Cloud View");
        this.tRow.setIcon("sensortag2cloudservice","","");
        this.tRow.value.setText("Device ID : " + addr);

        EDCTableRow tmpRow = (EDCTableRow) this.tRow;
        tmpRow.pushToCloud.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    connect();
                }
                else {
                    disconnect();
                }
            }
        });


        tmpRow.configureCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloudProfileConfigurationDialogFragment dF = CloudProfileConfigurationDialogFragment.newInstance(addrShort.toUpperCase());

                final Activity act = (Activity)context;
                dF.show(act.getFragmentManager(),"CloudConfig");


            }
        });

        ((EDCTableRow) this.tRow).cloudURL.setText("Open in browser");
        ((EDCTableRow) this.tRow).cloudURL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://console-sandbox.everyware-cloud.com/")));
                }
            });
        mThis = this;
        cloudConfigUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(CloudProfileConfigurationDialogFragment.ACTION_CLOUD_CONFIG_WAS_UPDATED)) {
                    Log.d("EDCCloudProfile","Cloud configuration was updated !");
                    if (edcClient != null) {
                        config = readCloudConfigFromPrefs();
                        if (edcClient.isConnected()) {
                            disconnect();
                            connect();
                        }
                    }
                }
            }
        };
        this.context.registerReceiver(cloudConfigUpdateReceiver,makeCloudConfigUpdateFilter());
    }

    public boolean disconnect() {

        try {
            if (edcClient != null) {
                ready = false;
                edcClient.stopSession();
                Thread.sleep(100);
                edcClient.terminate();
                leds.disableService();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean connect() {

        try {
            edcClient.startSession();
            ready = true;
            publishTimer = new Timer();
            MQTTTimerTask task = new MQTTTimerTask();
            publishTimer.schedule(task,1000,1000);
            leds.enableService();
            return true;
        } catch (EdcClientException e) {
            return false;
        }
    }

    public void addSensorValueToPendingMessage(String variableName, String Value) {
        this.valueMap.put(variableName,Value);
    }
    public void addSensorValueToPendingMessage(Map.Entry<String,String> e) {
        this.valueMap.put(e.getKey(),e.getValue());
    }
    @Override
    public void onPause() {
        super.onPause();
        this.context.unregisterReceiver(cloudConfigUpdateReceiver);
    }
    @Override
    public void onResume() {
        super.onResume();
        this.context.registerReceiver(cloudConfigUpdateReceiver,makeCloudConfigUpdateFilter());
    }
    @Override
    public void enableService () {

    }
    @Override
    public void disableService () {

    }
    @Override
    public void configureService() {

    }
    @Override
    public void deConfigureService() {

    }
    @Override
    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {
    }
    @Override
    public void didReadValueForCharacteristic(BluetoothGattCharacteristic c) {
    }
    public static EDCProfile getInstance() {
        return mThis;
    }
    class MQTTTimerTask extends TimerTask {
        @Override
        public void run() {
            if (ready) {
                leds.configureService();
                final Activity activity = (Activity) context;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((EDCTableRow) tRow).setCloudConnectionStatusImage(activity.getResources().getDrawable(R.mipmap.cloud_connected_tx));
                    }
                });

                EdcPayload payload = new EdcPayload();
                payload.setTimestamp(new Date(System.currentTimeMillis()));

                for (Map.Entry<String, String> entry : valueMap.entrySet()) {
                    String var = entry.getKey();
                    String val = entry.getValue();
                    if ("key_1".equals(var) || "key_2".equals(var) || "reed_relay".equals(var))
                        payload.addMetric(var, Integer.parseInt(val));
                    else
                        payload.addMetric(var, Float.parseFloat(val.replace(",",".")));
                }

                if (edcClient != null) {
                    try { edcClient.publish(config.assetId, config.publishTopic, payload, 1, false); }
                    catch (EdcClientException e) { e.printStackTrace(); }
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((EDCTableRow)tRow).setCloudConnectionStatusImage(activity.getResources().getDrawable(R.mipmap.cloud_connected));
                    }
                });
                leds.deConfigureService();
            }
            else {
                Log.d("EDCProfile", "MQTTTimerTask ran, but MQTT not ready");
            }
        }
    }

    private static IntentFilter makeCloudConfigUpdateFilter() {
        final IntentFilter fi = new IntentFilter();
        fi.addAction(CloudProfileConfigurationDialogFragment.ACTION_CLOUD_CONFIG_WAS_UPDATED);
        return fi;
    }

    class cloudConfig extends Object {

        public String account;
        public String username;
        public String password;
        public String clientId;
        public String assetId;
        public String brokerAddress;
        public int brokerPort;
        public String publishTopic;
        cloudConfig () {
        }

        @Override
        public String toString() {
            String s = new String();
            s = "Cloud configuration :\r\n";
            s += "Account : " + account + "\r\n";
            s += "Username : " + username + "\r\n";
            s += "Password : " + password + "\r\n";
            s += "Asset : " + assetId + "\r\n";
            s += "Client ID : " + clientId + "\r\n";
            s += "Broker Address : " + brokerAddress + "\r\n";
            s += "Proker Port : " + brokerPort + "\r\n";
            s += "Publish Topic : " + publishTopic + "\r\n";
            return s;
        }
    }
    public cloudConfig readCloudConfigFromPrefs() {
        cloudConfig c = new cloudConfig();
        try {
            c.account = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_ACCOUNT,this.context);
            c.username = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_USERNAME,this.context);
            c.password = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_PASSWORD,this.context);
            c.clientId = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_CLIENT_ID,this.context);
            c.assetId = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_ASSET_ID,this.context);
            c.brokerAddress = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_BROKER_ADDR,this.context);
            c.brokerPort = Integer.parseInt(CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_BROKER_PORT,this.context),10);
            c.publishTopic = CloudProfileConfigurationDialogFragment.retrieveCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_PUBLISH_TOPIC,this.context);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return c;
    }
    public cloudConfig initPrefsWithEDC() {
        cloudConfig c = new cloudConfig();
        c.account = CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_ACCOUNT;
        c.username = CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_USERNAME;
        c.password = CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_PASSWORD;
        c.clientId = CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_CLIENT_ID;
        c.assetId = CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_ASSET_ID;
        c.brokerAddress = CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_BROKER_ADDR;
        try {
            c.brokerPort = Integer.parseInt(CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_BROKER_PORT);
        }
        catch (Exception e) {
            c.brokerPort = 1883;
        }
        c.publishTopic = CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_PUBLISH_TOPIC;
        return c;
    }
    public void writeCloudConfigToPrefs(cloudConfig c) {
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_ACCOUNT,c.account,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_USERNAME,c.username,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_PASSWORD,c.password,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_CLIENT_ID,c.clientId,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_ASSET_ID,c.assetId,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_BROKER_ADDR,c.brokerAddress,this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_BROKER_PORT,((Integer)c.brokerPort).toString(),this.context);
        CloudProfileConfigurationDialogFragment.setCloudPref(CloudProfileConfigurationDialogFragment.PREF_CLOUD_PUBLISH_TOPIC,c.publishTopic,this.context);
    }

    private void updateBirthCertificate() {

        if (profile == null) return;
        profile.setDisplayName(config.assetId);
        profile.setFirmwareVersion(android.os.Build.VERSION.RELEASE);
        profile.setBiosVersion(android.os.Build.BOOTLOADER);
        profile.setOs("Android "+android.os.Build.VERSION.CODENAME);
        profile.setOsVersion(android.os.Build.VERSION.RELEASE);

    }


    public void connectionLost() {
    }

    public void connectionRestored() {
    }

    public void published(int messageId) {
    }

    public void subscribed(int messageId) {
    }

    public void unsubscribed(int messageId) {
    }

    public void controlArrived(String assetId, String topic, byte[] payload, int qos, boolean retain) {
    }

    public void publishArrived(String assetId, String topic, byte[] payload, int qos, boolean retain) {
    }

    public void controlArrived(String assetId, String receivedTopic, EdcPayload payload, int qos, boolean retain) {
    }

    public void publishArrived(String assetId, String receivedTopic, EdcPayload payload, int qos, boolean retain) {
    }

    public void setLedAndBuzzerProfile (SensorTagLedAndBuzzerProfile leds) {
        this.leds = leds;
    }
}

