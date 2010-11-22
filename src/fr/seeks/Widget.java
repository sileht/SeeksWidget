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

import fr.seeks.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
	public void onDeleted(Context context, int[] appWidgetIds) {
		// called when widgets are deleted
		// see that you get an array of widgetIds which are deleted
		// so handle the delete of multiple widgets in an iteration
		super.onDeleted(context, appWidgetIds);
	}

	public void onDisabled(Context context) {
		super.onDisabled(context);
		// runs when all of the instances of the widget are deleted from
		// the home screen
		// here you can do some setup
	}

	public void onEnabled(Context context) {
		super.onEnabled(context);
		// runs when all of the first instance of the widget are placed
		// on the home screen
	}

	public void onReceive(Context context, Intent intent) {
		// Android 1.5 fixes:
		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else {
			super.onReceive(context, intent);
		}

		// all the intents get handled by this method
		// mainly used to handle self created intents, which are not
		// handled by any other method

		// the super call delegates the action to the other methods

		// for example the APPWIDGET_UPDATE intent arrives here first
		// and the super call executes the onUpdate in this case
		// so it is even possible to handle the functionality of the
		// other methods here
		// or if you don't call super you can overwrite the standard
		// flow of intent handling
		super.onReceive(context, intent);
	}

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// runs on APPWIDGET_UPDATE
		// here is the widget content set, and updated
		// it is called once when the widget created
		// and periodically as set in the metadata xml

		// the layout modifications can be done using the AppWidgetManager
		// passed in the parameter, we will discuss it later

		// the appWidgetIds contains the Ids of all the widget instances
		// so here you want likely update all of them in an iteration

		// we will use only the first creation run
		super.onUpdate(context, appWidgetManager, appWidgetIds);

        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.search_widget);

            Intent intent = new Intent(context, Search.class);
			PendingIntent p = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.seeks_button, p);
			views.setOnClickPendingIntent(R.id.search_widget_text, p);
			appWidgetManager.updateAppWidget(appWidgetId, views);
			

            intent = new Intent(context, Settings.class);
			p = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.seeks_logo, p);
			appWidgetManager.updateAppWidget(appWidgetId, views);
        }


	}
}
