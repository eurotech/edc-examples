package com.example.eurotech.ble.common;
/**************************************************************************************************
 Filename:       CloudProfileConfigurationDialogFragment.java
 Revised:        $Date: Wed Apr 22 13:01:34 2015 +0200$
 Revision:       $Revision: 599e5650a33a4a142d060c959561f9e9b0d88146$

 Copyright (c) 2013 - 2015 Texas Instruments Incorporated

 All rights reserved not granted herein.
 Limited License.

 Texas Instruments Incorporated grants a world-wide, royalty-free,
 non-exclusive license under copyrights and patents it now or hereafter
 owns or controls to make, have made, use, import, offer to sell and sell ("Utilize")
 this software subject to the terms herein.  With respect to the foregoing patent
 license, such license is granted  solely to the extent that any such patent is necessary
 to Utilize the software alone.  The patent license shall not apply to any combinations which
 include this software, other than combinations with devices manufactured by or for TI ('TI Devices').
 No hardware patent is licensed hereunder.

 Redistributions must preserve existing copyright notices and reproduce this license (including the
 above copyright notice and the disclaimer and (if applicable) source code license limitations below)
 in the documentation and/or other materials provided with the distribution

 Redistribution and use in binary form, without modification, are permitted provided that the following
 conditions are met:

 * No reverse engineering, decompilation, or disassembly of this software is permitted with respect to any
 software provided in binary form.
 * any redistribution and use are licensed by TI for use only with TI Devices.
 * Nothing shall obligate TI to provide you with source code for the software licensed and provided to you in object code.

 If software source code is provided to you, modification and redistribution of the source code are permitted
 provided that the following conditions are met:

 * any redistribution and use of the source code, including any resulting derivative works, are licensed by
 TI for use only with TI Devices.
 * any redistribution and use of any object code compiled from the source code and any resulting derivative
 works, are licensed by TI for use only with TI Devices.

 Neither the name of Texas Instruments Incorporated nor the names of its suppliers may be used to endorse or
 promote products derived from this software without specific prior written permission.

 DISCLAIMER.

 THIS SOFTWARE IS PROVIDED BY TI AND TI'S LICENSORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL TI AND TI'S LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.


 **************************************************************************************************/

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eurotech.ble.sensortag.R;

import java.util.Map;

/**
 * Created by ole on 15/04/15.
 */
public class CloudProfileConfigurationDialogFragment extends DialogFragment  {

    public final static String PREF_CLOUD_ACCOUNT = "cloud_account";
    public final static String PREF_CLOUD_USERNAME = "cloud_username";
    public final static String PREF_CLOUD_PASSWORD = "cloud_password";
    public final static String PREF_CLOUD_CLIENT_ID = "cloud_client_id";
    public final static String PREF_CLOUD_ASSET_ID = "cloud_asset_id";
    public final static String PREF_CLOUD_BROKER_ADDR = "cloud_broker_address";
    public final static String PREF_CLOUD_BROKER_PORT = "cloud_broker_port";
    public final static String PREF_CLOUD_PUBLISH_TOPIC = "cloud_publish_topic";
    public final static String ACTION_CLOUD_CONFIG_WAS_UPDATED = "com.example.ti.ble.common.CloudProfileConfigurationDialogFragment.UPDATE";
    public final static String DEF_CLOUD_EDC_ACCOUNT = "";
    public final static String DEF_CLOUD_EDC_USERNAME = "";
    public final static String DEF_CLOUD_EDC_PASSWORD = "";
    public final static String DEF_CLOUD_EDC_CLIENT_ID = "";
    public final static String DEF_CLOUD_EDC_ASSET_ID = "";
    public final static String DEF_CLOUD_EDC_BROKER_ADDR = "mqtts://broker-sandbox.everyware-cloud.com";
    public final static String DEF_CLOUD_EDC_BROKER_PORT = "8883";
    public final static String DEF_CLOUD_EDC_PUBLISH_TOPIC = "BLESensorTag/sensors";

    private String deviceId = "";
    private View v;

    SharedPreferences prefs = null;

    public CloudProfileConfigurationDialogFragment() {}

    public CloudProfileConfigurationDialogFragment(String devId) {
        deviceId = devId;
    }
    public static CloudProfileConfigurationDialogFragment newInstance(String devId) {
        CloudProfileConfigurationDialogFragment frag = new CloudProfileConfigurationDialogFragment(devId);
        Bundle args = new Bundle();
        return frag;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder cloudDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Cloud configuration")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        CloudProfileConfigurationDialogFragment.setCloudPref(PREF_CLOUD_ACCOUNT,((EditText)v.findViewById(R.id.cloud_account)).getText().toString(),getActivity());
                        CloudProfileConfigurationDialogFragment.setCloudPref(PREF_CLOUD_USERNAME,((EditText)v.findViewById(R.id.cloud_username)).getText().toString(),getActivity());
                        CloudProfileConfigurationDialogFragment.setCloudPref(PREF_CLOUD_PASSWORD,((EditText)v.findViewById(R.id.cloud_password)).getText().toString(),getActivity());
                        CloudProfileConfigurationDialogFragment.setCloudPref(PREF_CLOUD_CLIENT_ID,((EditText)v.findViewById(R.id.cloud_client_id)).getText().toString(),getActivity());
                        CloudProfileConfigurationDialogFragment.setCloudPref(PREF_CLOUD_ASSET_ID,((EditText)v.findViewById(R.id.cloud_asset_id)).getText().toString(),getActivity());
                        CloudProfileConfigurationDialogFragment.setCloudPref(PREF_CLOUD_BROKER_ADDR,((EditText)v.findViewById(R.id.cloud_broker_address)).getText().toString(),getActivity());
                        CloudProfileConfigurationDialogFragment.setCloudPref(PREF_CLOUD_BROKER_PORT,((EditText)v.findViewById(R.id.cloud_broker_port)).getText().toString(),getActivity());
                        CloudProfileConfigurationDialogFragment.setCloudPref(PREF_CLOUD_PUBLISH_TOPIC,((EditText)v.findViewById(R.id.cloud_publish_topic)).getText().toString(),getActivity());

                        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        Map <String, ?> keys = prefs.getAll();
                        for (Map.Entry<String,?> entry : keys.entrySet()){
                            Log.d("CloudProfileConfigurationDialogFragment",entry.getKey() + ":" + entry.getValue().toString());
                        }

                        final Intent intent = new Intent(ACTION_CLOUD_CONFIG_WAS_UPDATED);
                        getActivity().sendBroadcast(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),"No values changed",Toast.LENGTH_LONG);
                    }
                });


        LayoutInflater i = getActivity().getLayoutInflater();

        v = i.inflate(R.layout.cloud_config_dialog, null);
        cloudDialog.setTitle("Cloud Setup");
        cloudDialog.setView(v);

        setAccount(true, DEF_CLOUD_EDC_ACCOUNT);
        setUsername(true, DEF_CLOUD_EDC_USERNAME);
        setPassword(true, DEF_CLOUD_EDC_PASSWORD);
        setClientId(true, deviceId);
        setAssetId(true, deviceId);
        setBrokerAddress(true, CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_BROKER_ADDR, CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_BROKER_PORT);
        setTopic(true, CloudProfileConfigurationDialogFragment.DEF_CLOUD_EDC_PUBLISH_TOPIC);

        return cloudDialog.create();


    }

    public void setAccount (boolean enable, String account) {
        TextView t = (TextView) v.findViewById(R.id.cloud_account_label);
        EditText e = (EditText) v.findViewById(R.id.cloud_account);
        e.setEnabled(enable);
        e.setText(account);
        if (enable) {
            t.setAlpha(1.0f);
            e.setAlpha(1.0f);
        }
        else {
            t.setAlpha(0.4f);
            e.setAlpha(0.4f);
        }
    }

    public void setUsername (boolean enable,String username) {
        TextView t = (TextView) v.findViewById(R.id.cloud_username_label);
        EditText e = (EditText) v.findViewById(R.id.cloud_username);
        e.setEnabled(enable);
        e.setText(username);
        if (enable) {
            t.setAlpha(1.0f);
            e.setAlpha(1.0f);
        }
        else {
            t.setAlpha(0.4f);
            e.setAlpha(0.4f);
        }
    }
    public void setPassword (boolean enable,String password) {
        TextView t = (TextView) v.findViewById(R.id.cloud_password_label);
        EditText e = (EditText) v.findViewById(R.id.cloud_password);
        e.setEnabled(enable);
        e.setText(password);
        if (enable) {
            t.setAlpha(1.0f);
            e.setAlpha(1.0f);
        }
        else {
            t.setAlpha(0.4f);
            e.setAlpha(0.4f);
        }

    }
    public void setClientId (boolean enable, String clientId) {
        TextView t = (TextView) v.findViewById(R.id.cloud_client_id_label);
        EditText e = (EditText)v.findViewById(R.id.cloud_client_id);
        e.setText(clientId);
        e.setEnabled(enable);
        if (enable) {
            t.setAlpha(1.0f);
            e.setAlpha(1.0f);
        }
        else {
            t.setAlpha(0.4f);
            e.setAlpha(0.4f);
        }
    }

    public void setAssetId (boolean enable, String assetId) {
        TextView t = (TextView) v.findViewById(R.id.cloud_asset_id_label);
        EditText e = (EditText)v.findViewById(R.id.cloud_asset_id);
        e.setText(assetId);
        e.setEnabled(enable);
        if (enable) {
            t.setAlpha(1.0f);
            e.setAlpha(1.0f);
        }
        else {
            t.setAlpha(0.4f);
            e.setAlpha(0.4f);
        }
    }

    public void setBrokerAddress(boolean en,String brokerAddress, String brokerPort) {
        TextView t = (TextView)v.findViewById(R.id.cloud_broker_address_label);
        EditText e = (EditText)v.findViewById(R.id.cloud_broker_address);
        TextView tP = (TextView)v.findViewById(R.id.cloud_broker_port_label);
        EditText eP = (EditText)v.findViewById(R.id.cloud_broker_port);

        e.setEnabled(en);
        eP.setEnabled(en);
        e.setText(brokerAddress);
        eP.setText(brokerPort);
        if (en) {
            t.setAlpha(1.0f);
            e.setAlpha(1.0f);
            tP.setAlpha(1.0f);
            eP.setAlpha(1.0f);
        }
        else {
            t.setAlpha(0.4f);
            tP.setAlpha(0.4f);
            e.setAlpha(0.4f);
            eP.setAlpha(0.4f);
        }
    }
    public void setTopic(boolean en, String topic) {
        TextView t = (TextView)v.findViewById(R.id.cloud_publish_topic_label);
        EditText e = (EditText)v.findViewById(R.id.cloud_publish_topic);

        e.setEnabled(en);
        e.setText(topic);

        if (en) {
            t.setAlpha(1.0f);
            e.setAlpha(1.0f);
        }
        else {
            t.setAlpha(0.4f);
            e.setAlpha(0.4f);
        }
    }

    public static String retrieveCloudPref(String prefName,Context con) {
        String preferenceKeyString = "pref_cloud_config_" + prefName;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);

        String defaultValue = "NS";
        return prefs.getString(preferenceKeyString, defaultValue);
    }
    public static boolean setCloudPref(String prefName, String prefValue, Context con) {
        String preferenceKeyString = "pref_cloud_config_" + prefName;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);

        String defaultValue = "NS";

        SharedPreferences.Editor ed = prefs.edit();
        ed.putString(preferenceKeyString, prefValue);
        return ed.commit();
    }

}
