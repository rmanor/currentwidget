/*
 *  Copyright (c) 2010-2012 Ran Manor
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
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * @author Ran
 *
 */
public class CurrentWidget extends AppWidgetProvider {

	private static final int MAX_NUMBER_OF_VIEWS = 4;
	private static final String SWITCH_VIEW_ACTION = "CURRENT_WIDGET_SWITCH_VIEW";

	@Override
	public void onEnabled(Context context) {
		//Log.d("CurrentWidget", "on enabled");
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

			//Log.d("CurrentWidget", String.format("onDeleted, id: %s", Integer.toString(appWidgetId)));

			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

			Intent widgetUpdate = new Intent(context.getApplicationContext(), CurrentWidget.class);
			widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
			widgetUpdate.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));

			PendingIntent sender = PendingIntent.getBroadcast(context.getApplicationContext(), 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);			

			alarmManager.cancel(sender);			

		}

	}	

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);

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

		//Log.d("CurrentWidget", "onSwitchView");

		SharedPreferences settings = context.getApplicationContext().getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());

		AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		
		if (appWidgetProviderInfo == null)
			return;
		
		int layoutId = convertPrefValueToLayout(settings.getString(context.getString(R.string.pref_widget_type_key), "0"),
				appWidgetProviderInfo.initialLayout);
		
		if (layoutId == R.layout.main_text &&
				settings.getBoolean(context.getString(R.string.pref_customize_text_showall), false)) {

			// showing multiple values, no switching

			Intent configIntent = new Intent(context.getApplicationContext(), CurrentWidgetConfigure.class);
			configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
			configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			configIntent.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
			configIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(configIntent);

			return;

		}

		int currentView = settings.getInt("current_view", 0);

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
			for (int i=0;i<currentView;++i) {
				if (settings.getBoolean("view_" + Integer.toString(i), false)) {
					nextView = i;
					break;
				}
			}				
		}


		/*++currentView;
		if (currentView >= NUMBER_OF_VIEWS)
			currentView = 0;*/

		if (nextView == -1) {
			// didn't find next views
			// meaning only one view is enabled
			// go to configuration			

			Intent configIntent = new Intent(context.getApplicationContext(), CurrentWidgetConfigure.class);
			configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId } );
			configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			configIntent.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(appWidgetId)));
			configIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(configIntent);

		}
		else {
			// found next view
			currentView = nextView;
		}

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

		remoteViews.setTextViewText(R.id.text, settings.getString(Integer.toString(currentView) + "_text", "no data"));
 
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("current_view", currentView);
		editor.commit();

	}	

	/*private int getEnabledViews(Context context) {

		int enabledViews = 0;

		SharedPreferences settings = context.getApplicationContext().getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		for (int i=0;i<MAX_NUMBER_OF_VIEWS;i++) {
			if (settings.getBoolean("view_" + Integer.toString(i), true)) {
				enabledViews |= i;
				count++;
			}
		}

		if (enabledViews == 0)
			enabledViews = 1;

		return enabledViews;
	}*/

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {	

		//Log.d("CurrentWidget", "onUpdate");

		boolean doLogFile = true;


		for (int appWidgetId : appWidgetIds) {
			//Log.d("CurrentWidget", String.format("onUpdate, id: %s", Integer.toString(appWidgetId))); 

			updateAppWidget(context.getApplicationContext(), AppWidgetManager.getInstance(context), appWidgetId, doLogFile);

			// write to logfile only from one instance
			doLogFile = false;

		}
	}

	static private int convertPrefValueToLayout(String selectedLayoutValue, int initalLayout) {
		int v = 0;
		try {
			v = Integer.parseInt(selectedLayoutValue);
		}
		catch (NumberFormatException nfe) {
			Log.e("CurrentWidget", nfe.getMessage());
			nfe.printStackTrace();			
		}
		switch(v) {
		case 1:
			return R.layout.main_text;				
		default:				
			return initalLayout;
		}
	}

	@TargetApi(9)
	@SuppressWarnings("deprecation")
	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, boolean doLogFile) {		

		SharedPreferences settings = context.getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);

		long secondsInterval = 60;
		try {
			secondsInterval = Long.parseLong(settings.getString(context.getString(R.string.pref_interval_key), "60"));
		}
		catch(Exception ex) {
			secondsInterval = 60;
			Log.e("CurrentWidget", ex.getMessage());
			ex.printStackTrace();
		}

		AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		if (appWidgetProviderInfo == null) {
			return;
		}
		
		int layoutId = convertPrefValueToLayout(settings.getString(context.getString(R.string.pref_widget_type_key), "0"),
				appWidgetProviderInfo.initialLayout);

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

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

		}

		String currentText = null;
		boolean isCharging = true;

		//ICurrentReader currentReader =  CurrentReaderFactory.getCurrentReader();
		Long value = CurrentReaderFactory.getValue();

		if (value == null) {
			currentText = "no data";
			if (layoutId == R.layout.main)
				remoteViews.setImageViewResource(R.id.status_image, R.drawable.drawing);
			else
				remoteViews.setImageViewResource(R.id.status_image, R.drawable.lightning);
		}
		else
		{				
			if (value < 0)
			{
				value = value*(-1);
				//remoteViews.setTextColor(R.id.text, Color.rgb(117, 120, 118)); // drawing
				//remoteViews.setViewVisibility(R.id.charging_image, View.INVISIBLE);

				if (layoutId == R.layout.main)
					remoteViews.setImageViewResource(R.id.status_image, R.drawable.drawing);
				else
					remoteViews.setImageViewResource(R.id.status_image, R.drawable.lightning);

				isCharging = false;
			}
			else
			{
				if (layoutId == R.layout.main)
					remoteViews.setImageViewResource(R.id.status_image, R.drawable.charging);
				else
					remoteViews.setImageViewResource(R.id.status_image, R.drawable.lightning_green);

				if (settings.getBoolean(context.getString(R.string.pref_no_log_in_charge), false))
					doLogFile = false;

			}


			if (settings.getBoolean(context.getString(R.string.pref_op_enabled_key), false)) {
				int op = Integer.parseInt(settings.getString(context.getString(R.string.pref_op_type_key), "0"));
				if (op > 0) {
					float opValue = 0;
					try {
						opValue = Float.parseFloat(settings.getString(context.getString(R.string.pref_op_value_key), "0"));
					}
					catch (NumberFormatException nfe) {
						opValue = 0;
					}
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
		String voltageText = "no data";
		String temperatureText = "no data";
		int batteryLevel = -1;
		float currentVoltage = 0;		
		//float currentTime = 0;
		// get battery level & voltage
		try {
			Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			if (batteryIntent != null) {
				int scale = batteryIntent.getIntExtra("scale", 100);
				batteryLevel = (int)((float)batteryIntent.getIntExtra("level", 0)*100/scale);
				batteryLevelText = String.valueOf(batteryLevel) + "%";
				currentVoltage = (float)batteryIntent.getIntExtra("voltage", 0) / 1000;
				voltageText = Float.toString(currentVoltage) + "V";
				int temperature = batteryIntent.getIntExtra("temperature", 0);
				//Log.d("CurrentWidget", "temp: " + Integer.toString(temperature));
				temperatureText = String.format("%.1f", ((float)temperature/10)) + "\u00B0C";
				
				// in case no current value, show correct status anyway
				if (value == null) {
					if (batteryIntent.getIntExtra("status", 1) == BatteryManager.BATTERY_STATUS_CHARGING) {
						if (layoutId == R.layout.main)
							remoteViews.setImageViewResource(R.id.status_image, R.drawable.charging);
						else
							remoteViews.setImageViewResource(R.id.status_image, R.drawable.lightning_green);

					}
					else {
						if (layoutId == R.layout.main)
							remoteViews.setImageViewResource(R.id.status_image, R.drawable.drawing);
						else
							remoteViews.setImageViewResource(R.id.status_image, R.drawable.lightning);

					}
				}


			}
		}
		catch (Exception ex) {
			// can't register service
			Log.e("CurrentWidget", ex.getMessage());
			ex.printStackTrace();
		}

		int numberOfHighValueTimes = 0;

		// if not charging and notification is enabled in settings
		if (value != null && !isCharging &&
				settings.getBoolean(context.getString(R.string.pref_notification_enabled_key), false)) {

			boolean onlyIfScreenOff = settings.getBoolean(context.getString(R.string.pref_notification_screen_off_key), false);

			boolean isOk = false;
			if (Integer.parseInt(Build.VERSION.SDK) < 7)
				isOk = true;
			else {
				isOk = !onlyIfScreenOff || (onlyIfScreenOff && !Compatibility.isScreenOn(context));
			}

			// only if don't check for screen off
			// or if check and screen is off
			if (isOk) {

				numberOfHighValueTimes = settings.getInt("numberOfHighValueTimes", 0);

				if (value >= Long.parseLong(settings.getString(context.getString(R.string.pref_notification_threshold_key), "200"))) {
					++numberOfHighValueTimes;
				}
				else {
					numberOfHighValueTimes = 0;
				}

				if (numberOfHighValueTimes >= 
					Integer.parseInt(settings.getString(context.getString(R.string.pref_notification_number_of_updates_key), "3"))) {

					// check for excluded apps
					ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
					List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();				

					boolean excluded = false;


					for (RunningAppProcessInfo processInfo : runningApps) {

						/*Log.d("CurrentWidget", "checking: " + processInfo.processName + " importance: " +
								Integer.toString(processInfo.importance));*/


						if (settings.getBoolean(ExcludedAppsActivity.EXCLUDED_PREFIX +
								processInfo.processName, false)) {

							/*Log.d("CurrentWidget", "found: " + processInfo.processName + " importance: " +
										Integer.toString(processInfo.importance));*/

							if (processInfo.importance <= RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
								excluded = true;
								break;
							}
						}

					}

					if (!excluded &&
							settings.getBoolean(context.getString(R.string.pref_notification_exclude_incall), false)) {

						TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
						if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
							excluded = true;					
						}

					}

					if (!excluded &&
							settings.getBoolean(context.getString(R.string.pref_notification_exclude_bluetooth), false)) {

						// version 2.0 and above
						//bluetooth adapater

					}

					if (!excluded &&
							settings.getBoolean(context.getString(R.string.pref_notification_exclude_headset), false)) {

						// version 2.0 and above
						if (Compatibility.isWiredHeadsetOn(context)) {
							excluded = true;
						}					
					}



					if (!excluded) {
						Notification notification = new Notification(R.drawable.icon,
								"CurrentWidget detected a high current usage",
								System.currentTimeMillis());

						notification.flags |= Notification.FLAG_AUTO_CANCEL;

						Intent notificationIntent = new Intent(context.getApplicationContext(), CurrentWidgetConfigure.class);
						notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent, 0);

						notification.setLatestEventInfo(context.getApplicationContext(), 
								"CurrentWidget", "High current usage was detected", contentIntent);

						String sound = settings.getString(context.getString(R.string.pref_notification_sound_key), "");
						if (sound.length() > 0)
							notification.sound = Uri.parse(sound);

						if (settings.getBoolean(context.getString(R.string.pref_notification_vibrate_key), false))
							notification.defaults |= Notification.DEFAULT_VIBRATE;

						if (settings.getBoolean(context.getString(R.string.pref_notification_led_key), false))
						{
							notification.ledARGB = 0xffffffff;
							notification.ledOnMS = 300;
							notification.ledOffMS = 1000;
							notification.flags |= Notification.FLAG_SHOW_LIGHTS;						
						}

						NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
						notificationManager.notify(1, notification);					

						numberOfHighValueTimes = 0;
					}
				}

			}		

		}

		SharedPreferences.Editor editor = settings.edit();
		//editor.putInt("lastBatteryLevel", batteryLevel);
		editor.putInt("numberOfHighValueTimes", numberOfHighValueTimes);
		editor.putString("0_text", currentText);
		editor.putString("1_text", batteryLevelText);
		editor.putString("2_text", voltageText);
		editor.putString("3_text", temperatureText);
		/*editor.putFloat("lastTime", currentTime);
		editor.putFloat("lastVoltage", currentVoltage);*/
		editor.commit();

		if (settings.getBoolean(context.getString(R.string.pref_customize_text_showall), false) &&
				layoutId == R.layout.main_text) {
			String text = "";
			for (int i=0;i<MAX_NUMBER_OF_VIEWS;++i) {
				if (settings.getBoolean("view_" + Integer.toString(i), false)) {
					text += settings.getString(Integer.toString(i) + "_text", "no data") + "\n";
				}
			}
			if (text.length() == 0) {
				text = currentText;
			}
			else {
				text = text.substring(0, text.length()-1); // remove last newline
			}
			remoteViews.setTextViewText(R.id.text, text);
		}
		else {

			int currentView = settings.getInt("current_view", 0);	

			remoteViews.setTextViewText(R.id.text, 
					settings.getString(Integer.toString(currentView) + "_text", "no data"));
		}

		// set last update
		remoteViews.setTextViewText(R.id.last_updated_text, (new SimpleDateFormat("HH:mm:ss")).format(new Date()));



		// write to log file
		if (settings.getBoolean(context.getString(R.string.pref_log_enabled_key), false) && doLogFile
				&& Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

			final String defaultLogfile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/currentwidget.log";
			
			try {				
				long logMaxSize = Long.parseLong(settings.getString(context.getString(R.string.pref_log_maxsize), "500000"));
				 
				if (logMaxSize > 0) {
					File f = new File(settings.getString(context.getString(R.string.pref_log_filename_key),							
							defaultLogfile));
					if (f.length() >= logMaxSize) {
						if (settings.getBoolean(context.getString(R.string.pref_log_rotation), false)) {
							String filename = defaultLogfile; //settings.getString(context.getString(R.string.pref_log_filename_key), "/sdcard/currentwidget.log");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
							filename += "-" + sdf.format(new Date());
							File newFile = new File(filename);
							f.renameTo(newFile);
						}
						else {
							f.delete();
						}
					}
					
					f = null;
				}

				FileOutputStream logFile = new FileOutputStream(settings.getString(context.getString(R.string.pref_log_filename_key), 
						defaultLogfile), true);
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

						for (RunningAppProcessInfo processInfo : runningApps) {
							str += processInfo.processName + ";";
						}
					}
				}

				// voltage
				str += "," + voltageText;

				str += "\r\n";

				logOutput.writeBytes(str);

				logOutput.close();
				logFile.close();
				
			}
			catch (Exception ex) {
				Log.e("CurrentWidget", ex.getMessage(), ex);				
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
			if (settings.getBoolean(context.getString(R.string.pref_force_sleep_log), false))
				alarms.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (secondsInterval*1000),
						secondsInterval * 1000, widgetUpdatePi);       
			else
				alarms.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + (secondsInterval*1000),
						secondsInterval * 1000, widgetUpdatePi);	        
		}
		else {
			alarms.cancel(widgetUpdatePi);
		}


		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);


	}

}

