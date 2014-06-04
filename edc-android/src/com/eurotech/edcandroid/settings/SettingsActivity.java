package com.eurotech.edcandroid.settings;

import java.util.ArrayList;
import java.util.List;

import com.eurotech.edcandroid.EDCAndroid;
import com.eurotech.edcandroid.R;

import android.app.ActionBar;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.ListAdapter;


public class SettingsActivity extends PreferenceActivity {

	private List<Header> mHeaders;

	protected void onResume() {
		super.onResume();
		
		EDCAndroid.localLog("BEGIN");

		setTitle("Settings"); 
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		if (getListAdapter() instanceof PrefsHeaderAdapter)
			((PrefsHeaderAdapter) getListAdapter()).resume();
		
		EDCAndroid.localLog("END");
	}

	protected void onPause() {
		super.onPause();
		
		EDCAndroid.localLog("BEGIN");
		
		if (getListAdapter() instanceof PrefsHeaderAdapter)
			((PrefsHeaderAdapter) getListAdapter()).pause();
		
		EDCAndroid.localLog("END");
	}

	public void onBuildHeaders(List<Header> target) {
		
		EDCAndroid.localLog("BEGIN");
		
		loadHeadersFromResource(R.xml.preferences_headers, target);
		mHeaders = target;
		
		EDCAndroid.localLog("END");
	}

	public void setListAdapter(ListAdapter adapter) {

		int i, count;

		EDCAndroid.localLog("BEGIN");
		
		if (mHeaders == null) {
			mHeaders = new ArrayList<Header>();
			count = adapter.getCount();
			for (i = 0; i < count; ++i)
				mHeaders.add((Header) adapter.getItem(i));
		}

		super.setListAdapter(new PrefsHeaderAdapter(this, mHeaders));
		
		EDCAndroid.localLog("END");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		EDCAndroid.localLog("BEGIN");
		
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}