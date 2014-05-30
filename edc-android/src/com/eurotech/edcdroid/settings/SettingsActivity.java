package com.eurotech.edcdroid.settings;
import java.util.ArrayList;
import java.util.List;

import com.eurotech.edcdroid.R;

import android.app.ActionBar;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.ListAdapter;


public class SettingsActivity extends PreferenceActivity {

	private List<Header> mHeaders;

	protected void onResume() {
		super.onResume();

		setTitle("Settings"); 
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		if (getListAdapter() instanceof PrefsHeaderAdapter)
			((PrefsHeaderAdapter) getListAdapter()).resume();
	}

	protected void onPause() {
		super.onPause();
		if (getListAdapter() instanceof PrefsHeaderAdapter)
			((PrefsHeaderAdapter) getListAdapter()).pause();
	}

	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preferences_headers, target);
		mHeaders = target;
	}

	public void setListAdapter(ListAdapter adapter) {
		int i, count;

		if (mHeaders == null) {
			mHeaders = new ArrayList<Header>();
			count = adapter.getCount();
			for (i = 0; i < count; ++i)
				mHeaders.add((Header) adapter.getItem(i));
		}

		super.setListAdapter(new PrefsHeaderAdapter(this, mHeaders));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}