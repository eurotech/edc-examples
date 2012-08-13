/*
 * Copyright (c) 2011 Eurotech Inc. All rights reserved.
 */

package com.eurotech.cloud.examples.gps;

public class GpsPoint {

	private String latitude;
	private String longitude;
	private double altitude;
	private String time;

	public GpsPoint(String latitude, String longitude, float altitude, String time) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.time = time;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public String getTime() {
		return time;
	}
}

