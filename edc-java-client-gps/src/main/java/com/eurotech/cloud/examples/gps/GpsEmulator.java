/*
 * Copyright (c) 2011 Eurotech Inc. All rights reserved.
 */

package com.eurotech.cloud.examples.gps;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GpsEmulator {

	private Thread gpsThread;
	private GpsPoint[] gpsPoints;
	private int index = 0;

	private Object lock;
	private double latitude;
	private double longitude;
	private double altitude;
	private long time;

	public void bind() {
		lock = new Object();
		synchronized(lock) {
			this.latitude = -1;
			this.longitude = -1;
			this.altitude = -1;
			this.time = -1;

			this.start("paris");
		}
	}

	public void unbind() {
		synchronized(lock) {
			this.latitude = -1;
			this.longitude = -1;
			this.altitude = -1;
			this.time = -1;
		}
		lock = null;

		gpsPoints = null;
	}

	public void start(String location) {
		index = 0;

		if(location.compareTo("paris") == 0 || location.compareTo("denver") == 0 || location.compareTo("test") == 0) {
			String fileName = null;
			if(location.compareTo("paris") == 0) {
				fileName = "paris.gpx";
			} else if(location.compareTo("denver") == 0) {
				fileName = "denver.gpx";
			} else if(location.compareTo("test") == 0) {
				fileName = "test.gpx";
			}

			GpsXmlHandler handler = new GpsXmlHandler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			try {
				// Create the builder and parse the file
				SAXParser parser = factory.newSAXParser();

				InputStream is = this.getClass().getResourceAsStream("/resources/" + fileName);
				File tmpFile = new File("/tmp/" + fileName);

				if(is != null) {
					copyFile(is, tmpFile);

					try {
						is.close();
						is = null;
					} catch(Exception e) {
						e.printStackTrace();
					}
				}

				parser.parse(getByteArrayInputStream("/tmp/" + fileName), handler);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//finally get the list from services.xml and load them
			gpsPoints = handler.getGpsPoints();
		}

		// Create a worker thread, to poll for GPS data
		gpsThread = new Thread(new Worker());
		gpsThread.start();
	}

	public double getLatitude() {
		synchronized(lock) {
			return latitude;
		}
	}

	public double getLongitude() {
		synchronized(lock) {
			return longitude;
		}
	}

	public double getAltitude() {
		synchronized(lock) {
			return altitude;
		}
	}

	public long getTime() {
		synchronized(lock) {
			return time;
		}
	}

	private class Worker implements Runnable {

		public void run() {			
			while(true) {
				if((index+1) == gpsPoints.length) {
					index = 0;
				}

				synchronized(lock) {
					latitude = Float.parseFloat(gpsPoints[index].getLatitude());
					longitude = Float.parseFloat(gpsPoints[index].getLongitude());
					altitude = gpsPoints[index].getAltitude();
					time = convertTime(gpsPoints[index].getTime());
				}

				++index;
				long sleepTime = getMillisecondsDiff(gpsPoints[index].getTime());

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private long getMillisecondsDiff(String newTime) {
		if(time != -1) {
			long secondsDiff = (convertTime(newTime) - time);
			return secondsDiff;
		} else {
			//return an arbitrary sleep delay
			return 2000;
		}
	}

	private long convertTime(String time) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-ddhh:mm:ss");

		try {
			time = time.replace("T","");
			time = time.replace("Z","");
			Date timeDate = df.parse(time);

			Calendar calTime = Calendar.getInstance();
			calTime.setTime(timeDate);

			return calTime.getTimeInMillis();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return -1;
	}

	private ByteArrayInputStream getByteArrayInputStream(String file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuffer buffer = new StringBuffer();
			String temp = null;

			while((temp = br.readLine()) != null) {

				buffer.append(temp);
			}
			br.close();

			String string = new String(buffer);
			byte[] data = string.getBytes();

			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			return bais;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void copyFile(InputStream is, File destination) {
		try {
			int bytesAvailable = is.available();
			int bufferSize = Math.min(bytesAvailable, 1024);
			byte[] buffer = new byte[bufferSize];

			if(destination.exists()) {
				destination.delete();
			}

			DataOutputStream dos = new DataOutputStream(new FileOutputStream(destination));
			int bytesRead = is.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = is.available();
				bufferSize = Math.min(bytesAvailable, 1024);				
				bytesRead = is.read(buffer, 0, bufferSize);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

