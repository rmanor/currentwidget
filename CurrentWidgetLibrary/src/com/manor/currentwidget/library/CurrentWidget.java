/*
 *  Copyright (c) 2010-2011 Ran Manor
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

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * @author Ran
 *
 */
public class CurrentWidget extends AppWidgetProvider {
	
	private static final int NUMBER_OF_VIEWS = 2;
	private static final String SWITCH_VIEW_ACTION = "CURRENT_WIDGET_SWITCH_VIEW";
	
	@Override
	public void onEnabled(Context context) {
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		
		// dumb fix for my old bug
		int maxId = 0;
		for (int i=0;i<appWidgetIds.length;i++) {
			if (appWidgetIds[i] > maxId)
				maxId = appWidgetIds[i];
		}	
		
		maxId = maxId * 2;
		
		for (int appWidgetId=1;appWidgetId<=maxId;appWidgetId++) {
			
			Log.d("CurrentWidget", String.format("onDeleted, id: %s", Integer.toString(appWidgetId)));

			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			
			Intent widgetUpdate = new Intent(context.getApplicationContext(), CurrentWidget.class);
			widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
	        widgetUpdate.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));

			PendingIntent sender = PendingIntent.getBroadcast(context.getApplicationContext(), 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);			
			
			alarmManager.cancel(sender);			

		}

	}	

	
	// fix for 1.5 SDK bug
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
		}
		else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
	    	extras = intent.getExtras();
	    	if (extras != null) {
	    		final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
	    				AppWidgetManager.INVALID_APPWIDGET_ID); 
	    		if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) { 
	    			this.onDeleted(context, new int[] { appWidgetId });
	    		}
	        } 
	    } else { 
	        super.onReceive(context, intent); 
	    } 	
	}
	
	private void onSwitchView(Context context, int appWidgetId) {
		
		SharedPreferences settings = context.getApplicationContext().getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		
		int currentView = settings.getInt("current_view", 0);
		++currentView;
		if (currentView >= NUMBER_OF_VIEWS)
			currentView = 0;
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
		remoteViews.setTextViewText(R.id.text, settings.getString(Integer.toString(currentView) + "_text", "no data"));
		
		AppWidgetManager.getInstance(context.getApplicationContext()).updateAppWidget(appWidgetId, remoteViews);
	
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("current_view", currentView);
		editor.commit();

	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {	
		
		boolean doLogFile = true;
		
		
		 for (int appWidgetId : appWidgetIds) {
			 //Log.d("CurrentWidget", String.format("onUpdate, id: %s", Integer.toString(appWidgetId))); 
			 	
			 updateAppWidget(context.getApplicationContext(), AppWidgetManager.getInstance(context), appWidgetId, doLogFile);
			 
			 // write to logfile only from one instance
			 doLogFile = false;

		 }
	}	

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, boolean doLogFile) {		
	
		SharedPreferences settings = context.getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		
		long secondsInterval = 60;
		try {
			secondsInterval = Long.parseLong(settings.getString(context.getString(R.string.pref_interval_key), "60"));
		}
		catch(Exception ex) {
			secondsInterval = 60;
		}
		 
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);		
             
		String currentText = null;
		boolean isCharging = true;
		
		//ICurrentReader currentReader =  CurrentReaderFactory.getCurrentReader();
		Long value = CurrentReaderFactory.getValue();
		
		if (value == null)
			currentText = "no data";	
		else
		{				
			if (value < 0)
			{
				value = value*(-1);
				//remoteViews.setTextColor(R.id.text, Color.rgb(117, 120, 118)); // drawing
				//remoteViews.setViewVisibility(R.id.charging_image, View.INVISIBLE);
				remoteViews.setImageViewResource(R.id.status_image, R.drawable.drawing);
				isCharging = false;
			}
			else
				remoteViews.setImageViewResource(R.id.status_image, R.drawable.charging);
				//remoteViews.setViewVisibility(R.id.charging_image, View.VISIBLE);
				//remoteViews.setTextColor(R.id.text, Color.rgb(100, 168, 0)); // charging
			
			
			if (settings.getBoolean(context.getString(R.string.pref_op_enabled_key), false)) {
				int op = Integer.parseInt(settings.getString(context.getString(R.string.pref_op_type_key), "0"));
				if (op > 0) {
					float opValue = Float.parseFloat(settings.getString(context.getString(R.string.pref_op_value_key), "0"));
					if (opValue > 0) {
						switch(op) {
						case 1:
							value = (long)Math.round(value * opValue);
							break;
						case 2:
							value = (long)Math.round(value / opValue);
							break;
						case 3:
							value = (long)Math.round(value + opValue);
							break;
						case 4:
							value = (long)Math.round(value - opValue);
							break;
						}
					}
				}
			}					
			
			currentText = value.toString() + "mA";
		}	
		
		String batteryLevelText = "no data";
		
		// get battery level
		try {
			Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			if (batteryIntent != null) {
				batteryLevelText = String.valueOf(batteryIntent.getIntExtra("level", 0)) + "%";
			}
		}
		catch (Exception ex) {
			// can't register service
		}

		
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("0_text", currentText);
		editor.putString("1_text", batteryLevelText);
		editor.commit();
		
		int currentView = settings.getInt("current_view", 0);	
		
		remoteViews.setTextViewText(R.id.text, 
				settings.getString(Integer.toString(currentView) + "_text", "no data"));
		
		// set last update
		remoteViews.setTextViewText(R.id.last_updated_text, (new SimpleDateFormat("HH:mm:ss")).format(new Date()));
		
		// write to log file
		if (settings.getBoolean(context.getString(R.string.pref_log_enabled_key), false) && doLogFile) {
			
			try {
				FileOutputStream logFile = new FileOutputStream(settings.getString(context.getString(R.string.pref_log_filename_key), "/sdcard/currentwidget.log"), true);
				DataOutputStream logOutput = new DataOutputStream(logFile);
				
				String str = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()) + ",";
				if (!isCharging)
					str += "-";
				
				str += currentText;
				
				// add battery level
				str += "," + batteryLevelText;
				
				if (settings.getBoolean(context.getString(R.string.pref_log_apps_key), false)) {
				
					ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
					List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();
					
					if (runningApps != null)
					{
						str += ",";
						
						for (int i=0;i<runningApps.size();i++) {
							str += runningApps.get(i).processName + ";";
						}				
					}
					
				}
				 
				 str += "\r\n";
				
				logOutput.writeBytes(str);
				
				logOutput.close();
				logFile.close();
			}
			catch (Exception ex) {
				Log.e("CurrentWidget", ex.getMessage());
			}			
		}
		
		/*Intent configIntent = new Intent(context.getApplicationContext(), CurrentWidgetConfigure.class);
		configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
		configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		configIntent.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
        PendingIntent configPi = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);*/
		
		Intent switchViewIntent = new Intent(context.getApplicationContext(), CurrentWidget.class);
		switchViewIntent.setAction(SWITCH_VIEW_ACTION);
		switchViewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
		switchViewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		PendingIntent switchViewPi = PendingIntent.getBroadcast(context, 0, switchViewIntent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
        
        remoteViews.setOnClickPendingIntent(R.id.text, switchViewPi);
        remoteViews.setOnClickPendingIntent(R.id.status_image, switchViewPi);


        Intent widgetUpdate = new Intent(context.getApplicationContext(), CurrentWidget.class);
        widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
        widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        widgetUpdate.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
        
        // make this pending intent unique
        //widgetUpdate.setData(Uri.withAppendedPath(Uri.parse(CurrentWidget.URI_SCHEME + "://widget/id/"), String.valueOf(mAppWidgetId)));
        
        PendingIntent widgetUpdatePi = PendingIntent.getBroadcast(context, 0, widgetUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        // update widget
        remoteViews.setOnClickPendingIntent(R.id.update_now_button, widgetUpdatePi);
        remoteViews.setOnClickPendingIntent(R.id.last_updated_text, widgetUpdatePi);
        remoteViews.setOnClickPendingIntent(R.id.last_update_title, widgetUpdatePi);
        remoteViews.setOnClickPendingIntent(R.id.linear_layout, widgetUpdatePi);
        
        //Log.d("CurrentWidget", "secondsInterval: " + Long.toString(secondsInterval));
        
        AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        
        // schedule the new widget for updating
        // @@@ enough? I think I need to cancel previous one as well!
        if (secondsInterval > 0) {       	    
	        //alarms.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5*60*1000, newPending);
	        alarms.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + (secondsInterval*1000),
	                secondsInterval * 1000, widgetUpdatePi);	        
        }
        else {
        	alarms.cancel(widgetUpdatePi);
        }
	
       
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);


	}

}

