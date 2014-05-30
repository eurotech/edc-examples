package com.eurotech.edcdroid;

public class Tile {
	
	public int thumb;
	public String title;
	public String text;
	public boolean active;
	public int hSize;
	public int vSize;
	
	public Tile (int thumb, String title, String text, boolean active) {
		super();
		this.thumb = thumb;
		this.title = title;
		this.text = text;
		this.active = active;
	}
}
