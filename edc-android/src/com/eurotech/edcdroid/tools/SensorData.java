package com.eurotech.edcdroid.tools;

import java.util.HashMap;
import java.util.Set;

public class SensorData {

	public final static String LAT = "lat";
	public final static String LNG = "lng";
	public final static String SPD = "spd";
	public final static String ALT = "alt";
	public final static String HDG = "hdg";
	public final static String SAT = "sat";
	public final static String PRE = "pre";
	
	
	HashMap<String, Double>     map = new HashMap<String, Double>();
	HashMap<String, Boolean> changed = new HashMap<String, Boolean>();
	
	public SensorData() {
	}
	
	public int length() {
		return map.size();
	}
	
	public void set(String sensor, double val) {
		map.put(sensor, val);
	}
	
	public void unset(String sensor) {
		map.remove(sensor);
	}
	
	public void empty() {
		map.clear();
	}
	
	public void hasChanged(String sensor, Boolean value) {
		changed.put(sensor, value);
	}
	public Boolean hasItChanged(String sensor) {
		return changed.get(sensor);
	}
	
	public boolean hasGps() {
		return map.containsKey(LAT) && map.containsKey(LNG);
	}
	
	public double getLat() {
		return map.get(LAT);
	}
	
	public double getLng() {
		return map.get(LNG);
	}

	public double getSpd() {
		return map.get(SPD);
	}

	public double getAlt() {
		return map.get(ALT);
	}

	public double getHdg() {
		return map.get(HDG);
	}

	public double getSat() {
		return map.get(SAT);
	}
	
	public double getPre() {
		return map.get(PRE);
	}
	
	public double get(String name) {
		return map.get(name);
	}
		
	public Set<String> getKeys() {
		return map.keySet();
	}
}
