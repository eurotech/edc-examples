package com.eurotech.edcandroid.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.preference.PreferenceManager;

public class SensorSelection {

	public final static int PSEUDO_TYPE_LOCATION = -1;

	public final static String[] SENSORS_NAMES = { 
		"Location",									//	Ordinal:  0
		"Accelerometer",							//	Ordinal:  1
		"Ambient Temperature",						//	Ordinal:  2
		"Gravity",									//	Ordinal:  3
		"Gyroscope",								//	Ordinal:  4
		"Light",									//	Ordinal:  5
		"Linear Acceleration",						//	Ordinal:  6
		"Magnetic Field",							//	Ordinal:  7
		"Orientation",								//	Ordinal:  8
		"Pressure",									//	Ordinal:  9
		"Proximity",								//	Ordinal: 10
		"Relative Humidity",						//	Ordinal: 11
	    "Rotation Vector",							//	Ordinal: 12
	    "ERROR"										//	Ordinal: 13
	    };

	public final static String[] SENSORS_CODES = {
		"location",
		"accelerometer",
		"temperature",
		"gravity",
		"gyroscope",
		"light",
		"linear_acceleration",
		"magnetic_field",
		"orientation",
		"pressure",
		"proximity",
		"relative_humidity",
	    "rotation_vector" };

	@SuppressWarnings("deprecation")
	public final static int[] SENSORS_TYPES = {
		PSEUDO_TYPE_LOCATION,						//		Constant Value:		-1
		Sensor.TYPE_ACCELEROMETER,					//		Constant Value:		 1
		Sensor.TYPE_AMBIENT_TEMPERATURE,			//		Constant Value:		13
		Sensor.TYPE_GRAVITY,						//		Constant Value:		 9
		Sensor.TYPE_GYROSCOPE,						//		Constant Value:		 4
		Sensor.TYPE_LIGHT,							//		Constant Value:		 5
		Sensor.TYPE_LINEAR_ACCELERATION,			//		Constant Value:		10
		Sensor.TYPE_MAGNETIC_FIELD,					//		Constant Value:		 2
		Sensor.TYPE_ORIENTATION,					//		Constant Value:		 3
		Sensor.TYPE_PRESSURE,						//		Constant Value:		 6
		Sensor.TYPE_PROXIMITY,						//		Constant Value:		 8
		Sensor.TYPE_RELATIVE_HUMIDITY,				//		Constant Value:		12
		Sensor.TYPE_ROTATION_VECTOR					//		Constant Value:		11
		};
	
	public final static int[] SENSORS_CROSS_REFERENCE = {
		13,											//	0	NON EXISTENT					-->		13	"ERROR"
		 1,											//	1	TYPE_ACCELEROMETER				-->		 1	"Accelerometer"
		 7,											//	2	TYPE_MAGNETIC_FIELD				-->		 7	"Magnetic Field"
		 8,											//	3	TYPE_ORIENTATION				-->		 8	"Orientation"
		 4,											//	4	TYPE_GYROSCOPE					-->		 4	"Gyroscope"
		 5,											//	5	TYPE_LIGHT						-->		 5	"Light"
		 9,											//	6	TYPE_PRESSURE					-->		 9	"Pressure"
		 2,											//	7	TYPE_TEMPERATURE				-->		 2	"Ambient Temperature"
		10,											//	8	TYPE_PROXIMITY					-->		10	"Proximity"
		 3,											//	9	TYPE_GRAVITY					-->		 3	"Gravity"
		 6,											//	10	TYPE_LINEAR_ACCELERATION		-->		 6	"Linear Acceleration"
		12,											//	11	TYPE_ROTATION_VECTOR			-->		12	"Rotation Vector"
		11,											//	12	TYPE_RELATIVE_HUMIDITY			-->		11	"Relative Humidity"
		 2											//	13	TYPE_AMBIENT_TEMPERATURE		-->		 2	"Ambient Temperature"
		 };

	public boolean[] selected = new boolean[SENSORS_TYPES.length];

	public SensorSelection (Context ctx) {

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

		for (int i = 0; i < SENSORS_TYPES.length; i++)
			if (sp.contains("sensor_" + SENSORS_CODES[i])) {
				selected[i] = sp.getBoolean("sensor_"+SENSORS_CODES[i], false);
			} else {
				selected[i] = false;
			}
	}

	public void setStatusByName(String name, boolean status) {
		for (int i = 0; i < SENSORS_NAMES.length; i++)
			if (SENSORS_NAMES[i].equals(name))
				selected[i] = status;
	}

	public void setStatusByType(int type, boolean status) {
		for (int i = 0; i < SENSORS_TYPES.length; i++)
			if (SENSORS_TYPES[i] == type)
				selected[i] = status;
	}

	public void setStatus(int id, boolean status) {
		if (id < selected.length && id >= 0)
			selected[id] = status;
	}

	public boolean getStatus(String name) {
		for (int i = 0; i < SENSORS_NAMES.length; i++)
			if (SENSORS_NAMES[i].equals(name))
				return selected[i];
		return false;
	}

	public boolean getStatusByType(int type) {
		for (int i = 0; i < SENSORS_TYPES.length; i++)
			if (SENSORS_TYPES[i] == type)
				return selected[i];
		return false;
	}

	public boolean getStatus(int id) {
		if (id < selected.length && id >= 0)
			return selected[id];
		return false;
	}

	public int countActive() {
		int count = 0;
		for (int i = 0; i < selected.length; i++)
			if (selected[i]) count++;
		return count;
	}
}
