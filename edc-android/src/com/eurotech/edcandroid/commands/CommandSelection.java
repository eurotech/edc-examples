package com.eurotech.edcandroid.commands;

import com.eurotech.edcandroid.gateway.EdcConnectionService;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CommandSelection {

	public static Command[] COMMAND_LIST = {
		new CommandBrightness(),
		new CommandNotification(),
		new CommandRing(),
		new CommandSpeak(),
		new CommandVibrate()
	};

	public static boolean[] selected = new boolean[COMMAND_LIST.length];

	public CommandSelection (Context ctx) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		for (int i = 0; i < COMMAND_LIST.length; i++)
			if (sp.contains(EdcConnectionService.commandPrefix + COMMAND_LIST[i].getTitle()))
				selected[i] = sp.getBoolean(EdcConnectionService.commandPrefix + COMMAND_LIST[i].getTitle(), false);
	}

	public void setStatusByName(String name, boolean status) {
		for (int i = 0; i < COMMAND_LIST.length; i++)
			if (COMMAND_LIST[i].getTitle().equals(name))
				selected[i] = status;
	}

	public void setStatus(int id, boolean status) {
		if (id < selected.length && id >= 0)
			selected[id] = status;
	}

	public boolean getStatus(String name) {
		for (int i = 0; i < COMMAND_LIST.length; i++)
			if (COMMAND_LIST[i].getTitle().equals(name))
				return selected[i];
		return false;
	}

	public boolean getStatus(int id) {
		if (id < selected.length && id >= 0)
			return selected[id];
		return false;
	}

	public int countActive() {
		int count = 0;
		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) count++;
		}
		return count;
	}

	public Command get(String name) {
		for (int i = 0; i < COMMAND_LIST.length; i++)
			if (COMMAND_LIST[i].getTitle().equals(name))
				return COMMAND_LIST[i];
		return null;
	}
}