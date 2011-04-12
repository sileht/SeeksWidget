/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.seeks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Provides access to the dictionary database.
 */
public class SuggestionProvider extends ContentProvider {
	String TAG = "SeeksSuggestionProvider";

	public static String AUTHORITY = "fr.seeks.SuggestionProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/query");

	public static final String KEY_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_DESCRIPTION = SearchManager.SUGGEST_COLUMN_TEXT_2;
	public static final String KEY_URL = "URL";

	public static final String TITLE_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.seeks.android.query";
	public static final String DESCRIPTION_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.seeks.android.query";

	@Override
	public boolean onCreate() {
		return true;
	}

	/**
	 * Handles all the dictionary searches and suggestion queries from the
	 * Search Manager. When requesting a specific word, the uri alone is
	 * required. When searching all of the dictionary for matches, the
	 * selectionArgs argument must carry the search query as the first element.
	 * All other arguments are ignored.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String url = getUrlFromKeywords(selectionArgs[0]);
		Log.v(TAG, "Query:" + url);

		String json = null;

		try {
			URL u = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			connection.connect();
			InputStream in = connection.getInputStream();

			byte[] bArr = null;
			in.read(bArr);
			json =  new String(bArr);
			Log.v(TAG,"JSON:");
			Log.v(TAG,json);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		MatrixCursor m = new MatrixCursor(new String[] { KEY_TITLE,
				KEY_DESCRIPTION, KEY_URL });

		JSONArray snippets;
		JSONObject object;

		try {
			object = (JSONObject) new JSONTokener(json).nextValue();
			snippets = object.getJSONArray("snippets");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		for (int i = 0; i < snippets.length(); i++) {
			JSONObject snip;
			try {
				snip = snippets.getJSONObject(i);
				m.newRow().add(snip.getString("title")).add(
						snip.getString("summary")).add(snip.getString("url"));
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}
		}

		return m;

	}

	public String getUrlFromKeywords(String keywords) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		String nodeurl = prefs.getString("nodelist", "seeks.fr");
		String proto = (prefs.getBoolean("use_https", false) ? "https" : "http");
		String url = proto + "://" + nodeurl + "/search?output=json&q="
				+ URLEncoder.encode(keywords) + "&expansion=1&action=expand";
		return url;
	}

	/**
	 * This method is required in order to query the supported types. It's also
	 * useful in our own query() method to determine the type of Uri received.
	 */

	// Other required implementations...

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

}