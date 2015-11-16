package com.example.eurotech.ble.sensortag;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.example.eurotech.ble.common.BluetoothLeService;
import com.example.eurotech.ble.common.GenericBluetoothProfile;

import java.util.List;

public class SensorTagLedAndBuzzerProfile extends GenericBluetoothProfile {
    public SensorTagLedAndBuzzerProfile(Context con,BluetoothDevice device,BluetoothGattService service,BluetoothLeService controller) {
        super(con,device,service,controller);

        List<BluetoothGattCharacteristic> characteristics = this.mBTService.getCharacteristics();

        for (BluetoothGattCharacteristic c : characteristics) {
            if (c.getUuid().toString().equals(SensorTagGatt.UUID_IO_DATA.toString())) {
                this.dataC = c;
            }
            if (c.getUuid().toString().equals(SensorTagGatt.UUID_IO_CONF.toString())) {
                this.configC = c;
            }
        }
    }

    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(SensorTagGatt.UUID_IO_SERV.toString())) == 0) {
            return true;
        }
        else return false;
    }

    @Override
    public void configureService() {
        int error = this.mBTLeService.writeCharacteristic(this.configC, (byte) 0x01);
        if (error != 0) {
            if (this.configC != null)
                printError("Led configuration set failed: ",this.configC,error);
        }
        this.isConfigured = true;
    }

    @Override
    public void deConfigureService() {
        int error = this.mBTLeService.writeCharacteristic(this.configC, (byte) 0x00);
        if (error != 0) {
            if (this.configC != null)
                printError("Led configuration unset failed: ",this.configC,error);
        }
        this.isConfigured = false;
    }

    @Override
    public void enableService () {
        // Switch on red led
        int error = mBTLeService.writeCharacteristic(this.dataC, (byte)0x01);
        if (error != 0) {
            if (this.dataC != null)
                printError("Led enable failed: ",this.dataC,error);
        }
        this.isEnabled = true;
    }

    @Override
    public void disableService () {
        // Switch off red led
        int error = mBTLeService.writeCharacteristic(this.dataC, (byte)0x00);
        if (error != 0) {
            if (this.dataC != null)
                printError("Sensor disable failed: ",this.dataC,error);
        }
        this.isConfigured = false;
    }
}
