package fr.seeks;

import java.net.URLEncoder;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;

public class Search extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
				
		Intent intent = getIntent();
		String queryAction = intent.getAction();

		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			String searchKeywords = intent.getStringExtra(SearchManager.QUERY);
			startActivity(new Intent(Intent.ACTION_VIEW,
						   Uri.parse(getUrlFromKeywords(searchKeywords))));
		} else {
			onSearchRequested();
		}
	}
	
	public String getUrlFromKeywords(String keywords){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String nodeurl = prefs.getString("nodelist", "http://seeks.fr");
		String url = nodeurl+"/search?q="+URLEncoder.encode(keywords)+"&expansion=1&action=expand";
		return url;

	}
}
