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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
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
	public static final String KEY_QUERY = SearchManager.SUGGEST_COLUMN_QUERY;
	public static final String KEY_ACTION = SearchManager.SUGGEST_COLUMN_INTENT_ACTION;
	public static final String KEY_URL = SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA;

	private Timer mTimer = null;
	private SharedPreferences mPrefs;

	@Override
	public boolean onCreate() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String query = uri.getLastPathSegment();

		if (query == null || query.equals("")
				|| query.equals("search_suggest_query"))
			return null;

		Log.v(TAG, "Request '" + query + "' for '" + uri + "'");

		MatrixCursor matrix = new MatrixCursor(new String[] { BaseColumns._ID,
				KEY_TITLE, KEY_DESCRIPTION, KEY_QUERY, KEY_ACTION, KEY_URL });

		Boolean instant_suggest = mPrefs.getBoolean("instant_suggest", false);
		if (instant_suggest) {
			setCursorOfQuery(uri, query, matrix);
		} else {
			perhapsSetCursorOfQuery(uri, query, matrix);
		}
		matrix.setNotificationUri(getContext().getContentResolver(), uri);

		return matrix;
	}

	public void perhapsSetCursorOfQuery(Uri uri, String query,
			MatrixCursor matrix) {

		if (query == "")
			return;

		if (mTimer != null) {
			mTimer.cancel();
		}
		mTimer = new Timer(true);
		mTimer.schedule(new SuggestionTimerTask(uri, query, matrix), 500);
	}

	private class SuggestionTimerTask extends TimerTask {
		private String mQuery;
		private MatrixCursor mMatrix;
		private Uri mUri;

		public SuggestionTimerTask(Uri uri, String query, MatrixCursor matrix) {
			super();
			mUri = uri;
			mQuery = query;
			mMatrix = matrix;

		}

		@Override
		public void run() {
			setCursorOfQuery(mUri, mQuery, mMatrix);
		}

	}

	public void setCursorOfQuery(Uri uri, String query, MatrixCursor matrix) {
		try {
			setCursorOfQueryThrow(uri, query, matrix);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setCursorOfQueryThrow(Uri uri, String query, MatrixCursor matrix)
			throws MalformedURLException, IOException {
		String url = getUrlFromKeywords(query);
		Log.v(TAG, "Query:" + url);

		String json = null;

		while (json == null) {
			HttpURLConnection connection = null;
			connection = (HttpURLConnection) (new URL(url)).openConnection();

			try {
				connection.setDoOutput(true);
				connection.setChunkedStreamingMode(0);
				connection.setInstanceFollowRedirects(true);

				connection.connect();
				int response = connection.getResponseCode();
				if (response == HttpURLConnection.HTTP_MOVED_PERM
						|| response == HttpURLConnection.HTTP_MOVED_TEMP) {
					Map<String, List<String>> list = connection
							.getHeaderFields();
					for (Entry<String, List<String>> entry : list.entrySet()) {
						String value = "";
						for (String s : entry.getValue()) {
							value = value + ";" + s;
						}
						Log.v(TAG, entry.getKey() + ":" + value);
					}
					// FIXME
					url = "";
					return;
				}
				InputStream in = connection.getInputStream();

				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				StringBuilder builder = new StringBuilder();

				String line;
				while ((line = r.readLine()) != null) {
					builder.append(line);
				}

				json = builder.toString();

				/*
				 * Log.v(TAG, "** JSON START **"); Log.v(TAG, json); Log.v(TAG,
				 * "** JSON END **");
				 */
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} finally {
				connection.disconnect();
			}
		}

		JSONArray snippets;
		JSONObject object;
		JSONArray suggestions;

		Boolean show_snippets = mPrefs.getBoolean("show_snippets", false);
		if (show_snippets) {
			try {
				object = (JSONObject) new JSONTokener(json).nextValue();
				snippets = object.getJSONArray("snippets");
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
			Log.v(TAG, "Snippets found: " + snippets.length());
			for (int i = 0; i < snippets.length(); i++) {
				JSONObject snip;
				try {
					snip = snippets.getJSONObject(i);
					matrix.newRow().add(i).add(snip.getString("title")).add(
							snip.getString("summary")).add(
							snip.getString("title")).add(Intent.ACTION_SEND)
							.add(snip.getString("url"));
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				}
			}
		} else {
			try {
				object = (JSONObject) new JSONTokener(json).nextValue();
				suggestions = object.getJSONArray("suggestions");
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
			Log.v(TAG, "Suggestions found: " + suggestions.length());
			for (int i = 0; i < suggestions.length(); i++) {
				try {
					matrix.newRow().add(i).add(suggestions.getString(i))
							.add("").add(suggestions.getString(i)).add(
									Intent.ACTION_SEARCH).add("");
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		getContext().getContentResolver().notifyChange(uri, null);

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
		String url = seeksPath + "/search?output=json&q="
				+ URLEncoder.encode(keywords) + "&expansion=1&action=expand";
		return url;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.v(TAG, "insert '" + values + "' for '" + uri + "'");
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.v(TAG, "delete '" + selection + "' for '" + uri + "'");
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.v(TAG, "update '" + values + "' for '" + uri + "'");
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

}