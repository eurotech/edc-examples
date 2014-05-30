package com.eurotech.edcdroid.settings;

import java.util.HashMap;
import java.util.List;

import com.eurotech.edcdroid.R;

import android.content.Context;
import android.preference.PreferenceActivity.Header;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class PrefsHeaderAdapter extends ArrayAdapter<Header> {

	static final int HEADER_TYPE_CATEGORY = 0;
	static final int HEADER_TYPE_NORMAL = 1;
	static final int HEADER_TYPE_SWITCH = 2;

	private LayoutInflater mInflater;
	private HashMap<Long,SwitchPreferenceEnabler> mAllEnabler;

	public PrefsHeaderAdapter(Context context, List<Header> objects) {
		super(context, 0, objects);

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mAllEnabler = new HashMap<Long,SwitchPreferenceEnabler>();
		
		mAllEnabler.put(Long.valueOf(R.id.pref_connectivity_connection_header), 
				new SwitchPreferenceEnabler(context, new Switch(context), context.getString(R.string.pref_connectivity_connection)));
		
		mAllEnabler.put(Long.valueOf(R.id.pref_connectivity_autopublish_header), 
				new SwitchPreferenceEnabler(context, new Switch(context), context.getString(R.string.pref_connectivity_autopublish)));
		
		mAllEnabler.put(Long.valueOf(R.id.pref_gateway_commands_header), 
				new SwitchPreferenceEnabler(context, new Switch(context), context.getString(R.string.pref_gateway_commands)));
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		Header header = getItem(position);
		int headerType = getHeaderType(header);
		View view = null;

		switch (headerType) {
		case HEADER_TYPE_CATEGORY:
			view = mInflater.inflate(android.R.layout.preference_category, parent, false);
			((TextView) view.findViewById(android.R.id.title)).setText(header.getTitle(getContext()
					.getResources()));
			break;

		case HEADER_TYPE_SWITCH:
			
			view = mInflater.inflate(R.layout.preference_header_switch_item, parent, false);

			((ImageView) view.findViewById(android.R.id.icon)).setImageResource(header.iconRes);
			((TextView) view.findViewById(android.R.id.title)).setText(header.getTitle(getContext().getResources()));
			((TextView) view.findViewById(android.R.id.summary)).setText(header.getSummary(getContext().getResources()));

			if (header.id == R.id.pref_connectivity_connection_header ||
					header.id == R.id.pref_connectivity_autopublish_header ||
					header.id == R.id.pref_gateway_commands_header)
				mAllEnabler.get(header.id).setSwitch((Switch) view.findViewById(R.id.switchWidget));
			break;

		case HEADER_TYPE_NORMAL:
			view = mInflater.inflate(R.layout.preference_header_item, parent, false);
			((ImageView) view.findViewById(android.R.id.icon)).setImageResource(header.iconRes);
			((TextView) view.findViewById(android.R.id.title)).setText(header.getTitle(getContext()
					.getResources()));
			((TextView) view.findViewById(android.R.id.summary)).setText(header
					.getSummary(getContext().getResources()));
			break;
		}

		return view;
	}

	public static int getHeaderType(Header header) {
		if ((header.fragment == null) && (header.intent == null)) {
			return HEADER_TYPE_CATEGORY;
		} else if (header.id == R.id.pref_connectivity_connection_header ||
				header.id == R.id.pref_connectivity_autopublish_header ||
				header.id == R.id.pref_gateway_commands_header) {
			return HEADER_TYPE_SWITCH;
		} else {
			return HEADER_TYPE_NORMAL;
		}
	}

	public void resume() {
		for (Long s:mAllEnabler.keySet())
			mAllEnabler.get(s).resume();
	}

	public void pause() {
		for (Long s : mAllEnabler.keySet())
			mAllEnabler.get(s).pause();
	}
}
