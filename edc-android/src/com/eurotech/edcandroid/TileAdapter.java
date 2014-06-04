package com.eurotech.edcandroid;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TileAdapter extends BaseAdapter {

	private Context mContext;
	private float dimValue = (float) 0.2;
	private float normalValue = (float) 1;

	public ArrayList<Tile> tiles;

	public TileAdapter(Context c) {
		mContext = c;
		tiles = new  ArrayList<Tile>();
	}

	public void addTile(Tile t) {
		this.tiles.add(t);
	}

	@Override
	public int getCount() {
		return tiles.size();
	}

	@Override
	public Object getItem(int position) {
		return tiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Tile t = tiles.get(position);
		View view = View.inflate(mContext, R.layout.tile_with_label, null);
		RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.tile_layout);

		ImageView image = (ImageView) rl.findViewById(R.id.tile_image);
		image.setImageResource(t.thumb);
		float alpha = dimValue;
		if (t.active) alpha = normalValue;
		image.setAlpha(alpha);
		t.hSize = image.getWidth();
		t.vSize = image.getHeight();
		

		TextView title = (TextView) rl.findViewById(R.id.tile_tile);       
		title.setText(t.title);

		TextView text = (TextView) rl.findViewById(R.id.tile_text);       
		text.setText(t.text);

		return rl;
	}
}
