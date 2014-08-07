/*
 *  Copyright (c) 2010-2014 Ran Manor
 *  
 *  This file is part of CurrentWidget.
 *    
 * 	CurrentWidget is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CurrentWidget is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CurrentWidget.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.manor.currentwidget.library;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

/**
 * @author Ran Manor
 *
 */
public class CurrentWidget extends AppWidgetProvider {

	private static final int MAX_NUMBER_OF_VIEWS = 4;
	private static final String SWITCH_VIEW_ACTION = "CURRENT_WIDGET_SWITCH_VIEW";
	private static final String NO_DATA_TEXT = "no data";
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Intent widgetUpdate = new Intent(context.getApplicationContext(), CurrentWidget.class);
		widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		for (int i=0; i < appWidgetIds.length; ++i) {
			final int appWidgetId = appWidgetIds[i];
			widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
			widgetUpdate.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));			
			PendingIntent widgetUpdatePi = PendingIntent.getBroadcast(context, 0, widgetUpdate,
					PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.cancel(widgetUpdatePi);
		}	
	}	

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Bundle extras = null;
		if (SWITCH_VIEW_ACTION.equals(action)) {
			extras = intent.getExtras();
			if (extras != null) {
				final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
						AppWidgetManager.INVALID_APPWIDGET_ID); 
				if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
					this.onSwitchView(context, appWidgetId);
				}
			}
		} else {
			super.onReceive(context, intent); 
		} 	
	}

	private static Intent getConfigIntent(Context context, int appWidgetId) {
		Intent configIntent = new Intent(context.getApplicationContext(), CurrentWidgetConfigure.class);
		configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
		configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		configIntent.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
		configIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return configIntent;
	}
	
	private void onSwitchView(Context context, int appWidgetId) {	
		SharedPreferences settings = 
				context.getApplicationContext().getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
		AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);		
		if (appWidgetProviderInfo == null)
			return;
		int layoutId = appWidgetProviderInfo.initialLayout; //getLayoutId(context, appWidgetId);		
		if (layoutId == R.layout.main_text &&
				settings.getBoolean(context.getString(R.string.pref_customize_text_showall), false)) {
			// showing multiple values, no switching
			context.startActivity(getConfigIntent(context, appWidgetId));
			return;
		}

		int currentView = settings.getInt("current_view_" + Integer.toString(appWidgetId), 0);

		// look for next enabled view
		int nextView = -1;

		// to look forward?
		if (currentView < (MAX_NUMBER_OF_VIEWS - 1)) {
			// look forward 
			for (int i=currentView+1;i<MAX_NUMBER_OF_VIEWS;++i) {
				if (settings.getBoolean("view_" + Integer.toString(i), false)) {
					nextView = i;
					break;
				}					
			}			
		}
		
		// didn't find, look from beginning
		if (nextView < 0 && currentView > 0) {
			for (int i=0; i < currentView; ++i) {
				if (settings.getBoolean("view_" + Integer.toString(i), false)) {
					nextView = i;
					break;
				}
			}				
		}
		
		if (nextView == -1) {
			// didn't find next views, meaning only one view is enabled, go to configuration.	
			context.startActivity(getConfigIntent(context, appWidgetId));
			return;
		}
		else {
			// found next view
			currentView = nextView;
		}
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);
		remoteViews.setTextViewText(R.id.text,
				settings.getString(Integer.toString(currentView) + "_text", NO_DATA_TEXT));
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("current_view_" + Integer.toString(appWidgetId), currentView);
		editor.commit();		
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}	

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {	
		boolean doLogFile = true;
		for (int appWidgetId : appWidgetIds) {
			updateAppWidget(context.getApplicationContext(), AppWidgetManager.getInstance(context), appWidgetId, doLogFile);
			// Write to logfile only from one instance.
			doLogFile = false;
		}
	}

	
	@TargetApi(16)
	static void setTextSizes(Context context, RemoteViews remoteViews, SharedPreferences settings) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {			
			remoteViews.setTextViewTextSize(R.id.text,
						TypedValue.COMPLEX_UNIT_SP,
						Integer.parseInt(settings.getString(context.getString(R.string.pref_value_text_size_key), "18")));
			remoteViews.setTextViewTextSize(R.id.last_updated_text,
					TypedValue.COMPLEX_UNIT_SP,
					Integer.parseInt(settings.getString(context.getString(R.string.pref_last_updated_text_size_key), "12")));
			remoteViews.setTextViewTextSize(R.id.update_now_button,
					TypedValue.COMPLEX_UNIT_SP,
					Integer.parseInt(settings.getString(context.getString(R.string.pref_update_now_text_size_key), "10")));
		}
	}
	
	@SuppressLint("NewApi")
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, boolean doLogFile) {
		// @@ Read everything from settings. 
		
		SharedPreferences settings = context.getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		if (appWidgetProviderInfo == null) {
			return;
		}
		
		int layoutId = appWidgetProviderInfo.initialLayout; //getLayoutId(context, appWidgetId);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		// Customizations.
		if (layoutId == R.layout.main_text) {
			// text color
			int color = settings.getInt(context.getString(R.string.pref_text_text_color), 0xFFFFFFFF);
			remoteViews.setTextColor(R.id.text, color);
			remoteViews.setTextColor(R.id.last_updated_text, color);
			remoteViews.setTextColor(R.id.update_now_button, color);
			// show time
			remoteViews.setViewVisibility(R.id.last_updated_text, 
					settings.getBoolean(context.getString(R.string.pref_customize_text_showtime), true)?View.VISIBLE:View.GONE);
			// show update now button
			remoteViews.setViewVisibility(R.id.update_now_button, 
					settings.getBoolean(context.getString(R.string.pref_customize_text_showupdate), true)?View.VISIBLE:View.GONE);		
			setTextSizes(context, remoteViews, settings);
		}
		
		boolean isCharging = settings.getBoolean("is_charging", false);
		
		if (isCharging) {
			if (layoutId == R.layout.main) {
				remoteViews.setImageViewResource(R.id.status_image, R.drawable.charging);
			} else {
				remoteViews.setImageViewResource(R.id.status_image, R.drawable.lightning_green);
			}			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				remoteViews.setContentDescription(R.id.status_image, "Charging");
			}
		} else {
			if (layoutId == R.layout.main) {
				remoteViews.setImageViewResource(R.id.status_image, R.drawable.drawing);
			} else {
				remoteViews.setImageViewResource(R.id.status_image, R.drawable.lightning);
			}			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				remoteViews.setContentDescription(R.id.status_image, "Discharging");
			}
		}
		
		// Show all customization.
		if (settings.getBoolean(context.getString(R.string.pref_customize_text_showall), false) &&
				layoutId == R.layout.main_text) {
			String text = "";
			for (int i = 0; i < MAX_NUMBER_OF_VIEWS; ++i) {
				if (settings.getBoolean("view_" + Integer.toString(i), false)) {
					text += settings.getString(Integer.toString(i) + "_text", NO_DATA_TEXT);
					if (i < MAX_NUMBER_OF_VIEWS - 1) {
						text += "\n";
					}
				}
			}
			remoteViews.setTextViewText(R.id.text, text);
		}
		else {
			// Set text for current view.
			int currentView = settings.getInt("current_view_" + Integer.toString(appWidgetId), 0);
			remoteViews.setTextViewText(R.id.text, 
					settings.getString(Integer.toString(currentView) + "_text", NO_DATA_TEXT));
		}

		// Set last update time. @@@@ TODO: GET DATETIME FROM PREFS.
		remoteViews.setTextViewText(R.id.last_updated_text, 
				(new SimpleDateFormat("HH:mm:ss", Locale.US)).format(new Date()));

		
		Intent switchViewIntent = new Intent(context.getApplicationContext(), CurrentWidget.class);
		switchViewIntent.setAction(SWITCH_VIEW_ACTION);
		switchViewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
		switchViewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		switchViewIntent.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
		PendingIntent switchViewPi = PendingIntent.getBroadcast(context, 0, switchViewIntent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.text, switchViewPi);
		remoteViews.setOnClickPendingIntent(R.id.status_image, switchViewPi);

		Intent widgetUpdate = new Intent(context.getApplicationContext(), Updater.class);
		widgetUpdate.setAction("com.manor.currentwidget.UPDATE");
		PendingIntent widgetUpdatePi = PendingIntent.getBroadcast(context, 0, widgetUpdate,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// update widget
		remoteViews.setOnClickPendingIntent(R.id.update_now_button, widgetUpdatePi);
		remoteViews.setOnClickPendingIntent(R.id.last_updated_text, widgetUpdatePi);
		remoteViews.setOnClickPendingIntent(R.id.last_update_title, widgetUpdatePi);
		remoteViews.setOnClickPendingIntent(R.id.linear_layout, widgetUpdatePi);
		
		// @@ I think that I need to call udpater here.. no? or just set the alarm? 
		// or in other words: on boot onUpadte is called, so how do I set the repeating alarm?
		// when is updater called on boot? this is the bug anayway.
		

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}
}
