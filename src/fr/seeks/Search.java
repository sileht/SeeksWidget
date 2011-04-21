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

import java.net.URLEncoder;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class Search extends Activity {
	final static String TAG = "SeeksSearch";

	private SharedPreferences mPrefs;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		Intent intent = getIntent();
		String queryAction = intent.getAction();

		/****
		 * 
		 * 
		 Log.v(TAG, "Search start with: " + queryAction); Uri u =
		 * intent.getData(); Log.v(TAG, "Search uri: " + u); Bundle b =
		 * intent.getExtras(); if (b != null) { for (String s : b.keySet()) {
		 * Log.v(TAG, "Search extra key: " + s); Log.v(TAG,
		 * "Search extra value: " + b.getString(s)); } }
		 */

		if (Intent.ACTION_SEND.equals(queryAction)) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intent
					.getExtras().getString("intent_extra_data_key"))));
			finish();
		} else if (Intent.ACTION_SEARCH.equals(queryAction)) {
			String searchKeywords = intent.getStringExtra(SearchManager.QUERY);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri
					.parse(getUrlFromKeywords(searchKeywords))));
			finish();
		} else {
			onSearchRequested();
			SearchManager sm = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			sm.setOnDismissListener(new SearchManager.OnDismissListener() {
				@Override
				public void onDismiss() {
					finish();
				}
			});
		}
	}

	public String getUrlFromKeywords(String keywords) {
		String seeksPath = "";
		if ("Custom URL".equals(mPrefs.getString("nodelist", ""))){
			seeksPath = mPrefs.getString("custom_url", "");
		} else {
			String nodeurl = mPrefs.getString("nodelist", "seeks.fr");
			String proto = (mPrefs.getBoolean("use_https", false) ? "https"
				: "http");
			seeksPath = proto + "://" + nodeurl ;
		}
		String url = seeksPath + "/search?q="
				+ URLEncoder.encode(keywords) + "&expansion=1&action=expand";
		return url;
	}
}
