package com.example.eurotech.ble.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.eurotech.ble.sensortag.R;
import com.example.eurotech.util.GenericCharacteristicTableRow;

/**
 * Created by pierantonio.merlino on 04/11/15.
 */
public class EDCTableRow extends GenericCharacteristicTableRow {
    Switch pushToCloud;
    TextView pushToCloudCaption;
    TextView cloudURL;
    Button configureCloud;
    ImageView cloudConnectionStatus;


    public EDCTableRow(Context con) {
        super(con);

        this.pushToCloud = new Switch(con);
        this.pushToCloud.setId(100);
        this.pushToCloudCaption = new TextView(con);
        this.pushToCloudCaption.setId(101);
        this.pushToCloudCaption.setText("Push to Cloud :");
        this.cloudURL = new TextView(con);
        this.cloudURL.setTextSize(30);
        this.cloudURL.setTextColor(Color.BLUE);
        this.cloudURL.setId(102);
        this.configureCloud = new Button(con);
        this.configureCloud.setId(103);
        this.configureCloud.setText("Advanced");
        this.cloudConnectionStatus = new ImageView(con);
        this.cloudConnectionStatus.setId(104);
        this.cloudConnectionStatus.setImageDrawable(getResources().getDrawable(R.mipmap.cloud_disconnected));




        RelativeLayout.LayoutParams txtItemParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        txtItemParams.addRule(RelativeLayout.BELOW,
                this.value.getId());
        txtItemParams.topMargin = 15;
        txtItemParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        this.pushToCloudCaption.setLayoutParams(txtItemParams);

        txtItemParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        txtItemParams.addRule(RelativeLayout.BELOW,
                this.value.getId());
        txtItemParams.topMargin = 15;
        txtItemParams.leftMargin = 10;
        txtItemParams.addRule(RelativeLayout.RIGHT_OF,pushToCloudCaption.getId());
        pushToCloud.setLayoutParams(txtItemParams);

        txtItemParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        txtItemParams.addRule(RelativeLayout.BELOW,
                this.pushToCloudCaption.getId());
        txtItemParams.topMargin = 30;
        txtItemParams.leftMargin = 0;
        txtItemParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        cloudURL.setLayoutParams(txtItemParams);

        txtItemParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        txtItemParams.addRule(RelativeLayout.BELOW,
                this.cloudURL.getId());
        txtItemParams.topMargin = 30;
        txtItemParams.leftMargin = 0;
        txtItemParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        configureCloud.setLayoutParams(txtItemParams);

        txtItemParams = new RelativeLayout.LayoutParams(
                150,
                150);
        txtItemParams.addRule(RelativeLayout.BELOW,
                this.cloudURL.getId());
        txtItemParams.topMargin = 0;
        txtItemParams.leftMargin = 30;
        txtItemParams.addRule(RelativeLayout.RIGHT_OF,configureCloud.getId());
        cloudConnectionStatus.setLayoutParams(txtItemParams);

        this.rowLayout.addView(this.pushToCloudCaption);
        this.rowLayout.addView(this.pushToCloud);
        this.rowLayout.addView(this.cloudURL);
        this.rowLayout.addView(this.configureCloud);
        this.rowLayout.addView(this.cloudConnectionStatus);

    }
    @Override
    public void onClick(View v) {
    }
    public void setCloudConnectionStatusImage(Drawable drawable) {
        this.cloudConnectionStatus.setImageDrawable(drawable);
    }
}



