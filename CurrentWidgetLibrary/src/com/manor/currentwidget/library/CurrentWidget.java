/**
 * 
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
	
	@Override
	public void onEnabled(Context context) {
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		
		for (int appWidgetId : appWidgetIds) {
			
			Log.i("CurrentWidget", String.format("onDeleted, id: %s", Integer.toString(appWidgetId)));

			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			
			Intent widgetUpdate = new Intent(context, CurrentWidget.class);
			widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
	        widgetUpdate.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));

			PendingIntent sender = PendingIntent.getBroadcast(context, 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);			
			
			alarmManager.cancel(sender);

		}

	}	

	
	// fix for 1.5 SDK bug
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		/*if (action == null) {
	    	Bundle extras = intent.getExtras();
	    	if (extras != null) {
	    		final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
	    				AppWidgetManager.INVALID_APPWIDGET_ID); 
	    		if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
	    			
	    		}
		}
		else*/ if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
	    	Bundle extras = intent.getExtras();
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
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {	
		
		 for (int appWidgetId : appWidgetIds) {
			 Log.i("CurrentWidget", String.format("onUpdate, id: %s", Integer.toString(appWidgetId))); 
			 	
			 updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);			 

		 }
	}
	
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {		
	
		SharedPreferences settings = context.getSharedPreferences("currentWidgetPrefs", 0);
		
		long secondsInterval = settings.getLong(CurrentWidgetConfigure.SECOND_INTERVAL_SETTING + appWidgetId, 60);
		 
		
		// set on click for whole layout to launch configuration
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
		Intent configIntent = new Intent(context, CurrentWidgetConfigure.class);		
		configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		configIntent.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.linear_layout, configPendingIntent);        
      
		
		String text = null;
		boolean isCharging = true;
		
		// @@@ add /sys/class/power_supply/battery/batt_chg_current?
		ICurrentReader currentReader =  CurrentReaderFactory.getCurrentReader();
		if (currentReader == null)
			text = "error1";	
		else
		{	
			Long value = currentReader.getValue();
			if (value == null)
				text = "error2";
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
				
				int op = settings.getInt(CurrentWidgetConfigure.OP + appWidgetId, 0);
				if (op > 0) {
					float opValue = settings.getFloat(CurrentWidgetConfigure.OP_VALUE + appWidgetId, 0);
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
					
				
				text = value.toString() + "mA";
			}					
		}	
		
		
		remoteViews.setTextViewText(R.id.text, text);
		
		// set last update
		remoteViews.setTextViewText(R.id.last_updated_text, (new SimpleDateFormat("HH:mm:ss")).format(new Date()));
		
		// write to log file
		if (settings.getBoolean(CurrentWidgetConfigure.LOG_ENABLED_SETTING + appWidgetId, false)) {
			
			try {
				FileOutputStream logFile = new FileOutputStream(settings.getString(CurrentWidgetConfigure.LOG_FILENAME_SETTING + appWidgetId, "/sdcard/currentwidget.log"), true);
				DataOutputStream logOutput = new DataOutputStream(logFile);
				
				String str = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()) + ",";
				if (!isCharging)
					str += "-";
				
				str += text;
				
				// get battery level
				try {
					Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
					if (batteryIntent != null) {
						str += "," + String.valueOf(batteryIntent.getIntExtra("level", 0)) + "%";
					}
				}
				catch (Exception ex) {
					// can't register service
					str += ",000";
				}
				
				if (settings.getBoolean(CurrentWidgetConfigure.LOG_APPS_SETTING + appWidgetId, false)) {
				
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

        Intent widgetUpdate = new Intent(context, CurrentWidget.class);
        widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
        widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        widgetUpdate.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
        
        // make this pending intent unique
        //widgetUpdate.setData(Uri.withAppendedPath(Uri.parse(CurrentWidget.URI_SCHEME + "://widget/id/"), String.valueOf(mAppWidgetId)));
        
        PendingIntent newPending = PendingIntent.getBroadcast(context, 0, widgetUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        // set on click for button
        remoteViews.setOnClickPendingIntent(R.id.update_now_button, newPending);
        
        // schedule the new widget for updating
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //alarms.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5*60*1000, newPending);
        alarms.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + (secondsInterval*1000),
                secondsInterval * 1000, newPending);        
       
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);


	}

}

