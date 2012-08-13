/*
* Copyright (c) 2011 Eurotech Inc. All rights reserved.
*/

package com.eurotech.cloud.examples.gps;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class GpsXmlHandler extends DefaultHandler {
	private static final String LABEL = "com.esf.app.demo.esf.training.console.GpsXmlHandler: ";	
	
	//Valid Elements in the xml file
	private static final String TAG_TRACK_POINT = "trkpt";
	private static final String TAG_ELEVATION = "ele";
	private static final String TAG_TIME = "time";
	
	private ArrayList gpsPoints;
	private String latitude;
	private String longitude;
	private String elevation;
	private String time;
	
	private boolean foundTrackPoint = false;
	private boolean foundElevation = false;
	private boolean foundTime = false;
	
	public GpsXmlHandler() {
		gpsPoints = new ArrayList();
		latitude = null;
		longitude = null;
		elevation = null;
		time = null;
	}
	
	public void startElement(String uri, String localName, String elementName, Attributes attributes) {
		System.out.println(LABEL + "Got a new startElement: " + elementName);
		
		if (TAG_TRACK_POINT.equals(elementName)) {
			foundTrackPoint = true;
			
			if(attributes.getLength() == 2) {
				for(int i=0; i<attributes.getLength(); i++) {
					if(attributes.getQName(i).compareTo("lat") == 0) {
						latitude = attributes.getValue(i);
					} else if(attributes.getQName(i).compareTo("lon") == 0) {
						longitude = attributes.getValue(i);
					} else {
						System.out.println(LABEL + "invalid attribute in trkpt element: " + attributes.getQName(i));
					}
				}
			} else {
				System.out.println(LABEL + "there must be two attributes (lat and lon) in the trkpt element");
			}
			
			elevation = null;
			time = null;
		} else if(TAG_ELEVATION.equals(elementName)) {
			foundElevation = true;
		} else if(TAG_TIME.equals(elementName)) {
			foundTime = true;
		} else {
			System.out.println(LABEL + "Found start tag: " + elementName);
		}
	}
	
	public void endElement(String uri, String localName, String elementName) {
		System.out.println(LABEL + "Got a new endElement: " + elementName);
		
		if (TAG_TRACK_POINT.equals(elementName)) {
			foundTrackPoint = false;
			
			if(latitude != null && longitude != null && elevation != null && time != null) {
				this.gpsPoints.add(new GpsPoint(latitude, longitude, Float.parseFloat(elevation), time));
			} else {
				System.out.println(LABEL + "the XML file is malformted");
			}
		} else if(TAG_ELEVATION.equals(elementName)) {
			foundElevation = false;
		} else if(TAG_TIME.equals(elementName)) {
			foundTime = false;
		} else {
			System.out.println(LABEL + "Found end tag: " + elementName);
		}
	}
	
	public void characters(char[] buf, int offset, int length) {
		String tag = (new String(buf).substring(offset, offset+length)).trim();
		System.out.println(LABEL + "currently identifying:  " + tag + " from: " + offset + " to "+(length + offset));
		System.out.println(LABEL + "currently identifying:  " + tag);
		
		if (!foundTrackPoint && !foundElevation && !foundTime) {
			return;
		}
		
		if(foundElevation) {
			if(elevation == null) {
				this.elevation = new String(buf, offset, length);
				System.out.println(LABEL + "found elevation: " + time);
				return;
			} else {
				this.elevation = elevation + new String(buf, offset, length);
				System.out.println(LABEL + "appending to elevation: " + elevation);
				return;
			}
		}
		
		if(foundTime) {
			if(time == null) {
				this.time = new String(buf, offset, length);
				System.out.println(LABEL + "found log level: " + time);
				return;
			} else {
				this.time = time + new String(buf, offset, length);
				System.out.println(LABEL + "appending to logLevel: " + time);
				return;
			}
		}
		
		System.out.println(LABEL + "found some odd data in services.xml");
		logDump(tag.getBytes());
	}
	
	public GpsPoint[] getGpsPoints() {
		GpsPoint[] data = new GpsPoint[gpsPoints.size()];

		for(int i=0; i<gpsPoints.size(); i++) {
			data[i] = (GpsPoint) gpsPoints.get(i);
		}
		
		return data;
	}
	
	private void logDump(byte[] message) {
		for (int i = 0; i < message.length; i++) {
			if ((i % 16) == 0) {
				if (i > 0) {
					System.out.println();
				}
				System.out.print('\t');
			}
			if (message[i] < 0x10) {
				System.out.print("0x0"
						+ Integer.toHexString(message[i] & 0x0ff) + " ");
			} else {
				System.out.print("0x"
						+ Integer.toHexString(message[i] & 0x0ff) + " ");
			}
		}
	}
}
