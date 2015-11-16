package com.example.eurotech.ble.sensortag;

import android.bluetooth.BluetoothGattCharacteristic;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.example.eurotech.util.Point3D;

/**
 * Created by pierantonio.merlino on 04/11/15.
 */
public class LuxometerView extends Fragment {

    public static LuxometerView mInstance = null;

    // GUI
    private TableLayout table;
    public boolean first = true;

    // House-keeping
    private DeviceActivity mActivity;
    private boolean mBusy;

    // The last two arguments ensure LayoutParams are inflated properly.
    View view;

    final int luxPerBit = 4;
    double[] lightArray;
    int lightArrayIndex = 0;
    double lightMean = 0.0;

    public LuxometerView() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInstance = this;
        mActivity = (DeviceActivity) getActivity();

        view = inflater.inflate(R.layout.generic_services_browser, container,false);
//        table = (TableLayout) view.findViewById(R.id.generic_services_layout);

        // Notify activity that UI has been inflated
        mActivity.onViewInflated(view);

        lightArray = new double[8];
        for (int i = 0; i < lightArray.length; i++)
            lightArray[i] = 0.0;
        lightArrayIndex = 0;
        lightMean = 0.0;

        return view;
    }

    public void showProgressOverlay(String title) {

    }

//    public void addRowToTable(TableRow row) {
//        if (first) {
//            table.removeAllViews();
//            table.addView(row);
//            table.requestLayout();
//            first = false;
//        }
//        else {
//            table.addView(row);
//            table.requestLayout();
//        }
//    }
//    public void removeRowsFromTable() {
//        table.removeAllViews();
//    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    void setBusy(boolean f) {
        if (f != mBusy)
        {
            mActivity.showBusyIndicator(f);
            mBusy = f;
        }
    }

    public void setBackgroudColor(BluetoothGattCharacteristic c) {

        if (c.getUuid().toString().equals(SensorTagGatt.UUID_OPT_DATA.toString())) {
            // Get the value
            byte[] value = c.getValue();
            double lightValue = Sensor.LUXOMETER.convert(value).x;

            // Check range
            if (lightValue > luxPerBit * 255)
                lightValue = (double) luxPerBit * 255;

            lightArray[lightArrayIndex] = lightValue;
            if (lightArrayIndex == lightArray.length - 1)
                lightArrayIndex = 0;
            else
                lightArrayIndex++;

            lightMean = 0.0;
            for (int i = 0; i < lightArray.length; i++)
                lightMean += lightArray[i];
            lightMean = lightMean / lightArray.length;

            // Convert to hexadecimal color
            String color = Integer.toHexString((int) (lightMean / luxPerBit));
            if (color.length() == 1)
                color = "0" + color;

            Log.d("Color", color);

            view.setBackgroundColor(Color.parseColor("#" + color + color + color));
        }
    }
}
