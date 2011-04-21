/*
 SeeksWidget is the legal property of mehdi abaakouk <theli48@gmail.com>
 Copyright (c) 2010 Mehdi Abaakouk

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 */
package fr.seeks;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Window;

public class Settings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		if ("Custom URL".equals(settings.getString("nodelist", ""))) {
			findPreference("use_https").setEnabled(false);
			findPreference("custom_url").setEnabled(true);
		} else {
			findPreference("use_https").setEnabled(true);
			findPreference("custom_url").setEnabled(false);
		}

		findPreference("custom_url").setSummary(
				settings.getString("custom_url", "None"));
		findPreference("custom_url").setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						preference.setSummary((String) newValue);

						return true;
					}
				});

		findPreference("nodelist").setSummary(
				settings.getString("nodelist", "None"));
		findPreference("nodelist").setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						preference.setSummary((String) newValue);
						if ("Custom URL".equals((String) newValue)) {
							findPreference("use_https").setEnabled(false);
							findPreference("custom_url").setEnabled(true);
						} else {
							findPreference("use_https").setEnabled(true);
							findPreference("custom_url").setEnabled(false);
						}
						return true;
					}
				});

		findPreference("about").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						showDialog(0);
						return true;
					}
				});

		Intent intent = getIntent();
		if (!"android.intent.action.MAIN".equals(intent.getAction())) {
			((PreferenceCategory) findPreference("cat_title"))
					.removePreference(findPreference("start_search"));
		}
	}
	
	protected Dialog onCreateDialog (int id, Bundle args){
		Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.about);
		dialog.setTitle(R.string.pref_about);
		return dialog;
	}

}