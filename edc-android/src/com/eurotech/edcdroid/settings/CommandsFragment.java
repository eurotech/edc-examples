package com.eurotech.edcdroid.settings;

import com.eurotech.edcdroid.R;
import com.eurotech.edcdroid.commands.CommandSelection;
import com.eurotech.edcdroid.gateway.EdcConnectionService;

import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;


public class CommandsFragment extends EdcPreferenceFragment {

	public void setPreferenceValues() {
		this.resourceId = R.xml.preferences_gateway_commands;
		this.resourceString = R.string.pref_gateway_commands;
	}

	@SuppressWarnings("unused")
	@Override
	public void onActivityCreatedInit() {
		if (this.getActivity() == null) return;
		PreferenceCategory notificationsCategory = (PreferenceCategory) findPreference("pref_gateway_commands_list");
		CommandSelection c = new CommandSelection(getActivity());

		for (int i = 0; i < CommandSelection.COMMAND_LIST.length; i++) {
			CheckBoxPreference togglePref = new CheckBoxPreference(this.getActivity());
			togglePref.setKey(EdcConnectionService.commandPrefix + CommandSelection.COMMAND_LIST[i].getTitle());
			togglePref.setTitle(CommandSelection.COMMAND_LIST[i].getTitle());
			togglePref.setSummary("Topic: " + CommandSelection.COMMAND_LIST[i].getTopic());
			notificationsCategory.addPreference(togglePref);
		}
	}
}