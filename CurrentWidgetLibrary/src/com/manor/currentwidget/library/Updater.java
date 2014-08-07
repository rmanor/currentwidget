package com.manor.currentwidget.library;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

// Consider IntentService as well.. try this with 1 seconds interval.
public class Updater extends BroadcastReceiver {

	private static final String NO_DATA_TEXT = "no data";
	public static final int INFO_NOTIFICATION_ID = 2;

	private static Long NormalizeCurrentValue(Long value, int op, float opValue) {
		if (op > 0 && opValue > 0) {
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
		return value;
	}
	
	static int HandleHighAlert(Context context, SharedPreferences settings, Long value) {
		int numberOfHighValueTimes = 0;
		boolean onlyIfScreenOff = settings.getBoolean(context.getString(R.string.pref_notification_screen_off_key), false);

		boolean isOk = false;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR_MR1) {
			isOk = true;
		} else {
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
					if (settings.getBoolean(ExcludedAppsActivity.EXCLUDED_PREFIX +
							processInfo.processName, false)) {
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
						settings.getBoolean(context.getString(R.string.pref_notification_exclude_headset), false)) {
					// version 2.0 and above
					if (Compatibility.isWiredHeadsetOn(context)) {
						excluded = true;
					}					
				}

				if (!excluded) {
					Intent notificationIntent = new Intent(context.getApplicationContext(), CurrentWidgetConfigure.class);
					notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent, 0);

					Notification notification = new NotificationCompat.Builder(context)
						.setContentTitle("CurrentWidget")
						.setContentText("CurrentWidget detected a high current usage")
						.setSmallIcon(R.drawable.icon)
						.setContentIntent(contentIntent)
						.build();

					notification.flags |= Notification.FLAG_AUTO_CANCEL;

					/*notification.setLatestEventInfo(context.getApplicationContext(), 
							"CurrentWidget", "High current usage was detected", contentIntent);*/

					String sound = settings.getString(context.getString(R.string.pref_notification_sound_key), "");
					if (sound.length() > 0) {
						notification.sound = Uri.parse(sound);
					}
					if (settings.getBoolean(context.getString(R.string.pref_notification_vibrate_key), false)) {
						notification.defaults |= Notification.DEFAULT_VIBRATE;
					}
					if (settings.getBoolean(context.getString(R.string.pref_notification_led_key), false)) {
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
		
		return numberOfHighValueTimes;
	}
	
	public static void updateInfoNotification(Context context, String notification_text) {
		Intent notificationIntent = new Intent(context.getApplicationContext(), CurrentWidgetConfigure.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent, 0);

		Notification notification = new NotificationCompat.Builder(context)
			.setContentTitle("CurrentWidget")
			.setContentText(notification_text)
			.setSmallIcon(R.drawable.icon)
			.setContentIntent(contentIntent)
			.build();

		notification.flags |= Notification.FLAG_NO_CLEAR;

		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(INFO_NOTIFICATION_ID, notification);	
	}
	
	public static void clearInfoNotification(Context context) {
		((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(INFO_NOTIFICATION_ID);
	}


	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		//Toast.makeText(context, "UPDATER!!!!!!!!!!!!!!!!!!!!!", Toast.LENGTH_LONG).show();
		//Log.d("CurrentWidget", "UPDATER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		SharedPreferences settings = 
				context.getApplicationContext().getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);

		
		String currentText = null;
		Long value = CurrentReaderFactory.getValue();
		if (value == null) {
			currentText = NO_DATA_TEXT;
		} else {				
			if (value < 0) {
				value = value * (-1);
			}

			if (settings.getBoolean(context.getString(R.string.pref_op_enabled_key), false)) {
				int op = Integer.parseInt(settings.getString(context.getString(R.string.pref_op_type_key), "0"));
				float opValue = 0;
				try {
					opValue = Float.parseFloat(settings.getString(context.getString(R.string.pref_op_value_key), "0"));
				}
				catch (NumberFormatException nfe) {
					opValue = 0;
				}
				value = NormalizeCurrentValue(value, op, opValue);
			}
			currentText = value.toString() + "mA";
		}	

		String batteryLevelText = NO_DATA_TEXT;
		String voltageText = NO_DATA_TEXT;
		String temperatureText = NO_DATA_TEXT;
		int batteryLevel = -1;
		float currentVoltage = 0;	
		boolean isCharging = false;
		// Get information from battery intent.
		try {
			Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			if (batteryIntent != null) {
				int scale = batteryIntent.getIntExtra("scale", 100);
				batteryLevel = (int)((float)batteryIntent.getIntExtra("level", 0)*100/scale);
				batteryLevelText = String.valueOf(batteryLevel) + "%";
				currentVoltage = (float)batteryIntent.getIntExtra("voltage", 0) / 1000;
				voltageText = Float.toString(currentVoltage) + "V";
				int temperature = batteryIntent.getIntExtra("temperature", 0);
				if (settings.getString(context.getString(R.string.pref_temp_units_key), "0").equals("1")) {
					temperatureText = String.format(Locale.ENGLISH, "%.1f\u00B0F", (((float)temperature/10) * 1.8) + 32);
				} else {
					temperatureText = String.format(Locale.ENGLISH, "%.1f\u00B0C", ((float)temperature/10));
				}
				isCharging = batteryIntent.getIntExtra("status", 1) == BatteryManager.BATTERY_STATUS_CHARGING;
			}
		}
		catch (Exception ex) {
			Log.e("CurrentWidget", ex.getMessage());
			ex.printStackTrace();
		}
		
		if (settings.getBoolean(context.getString(R.string.pref_show_sign), false) && !isCharging) {
			currentText = "-" + currentText;
		}
		
		boolean doLogFile = true;
		
		if (isCharging && settings.getBoolean(context.getString(R.string.pref_no_log_in_charge), false)) {
			doLogFile = false;
		}

		int numberOfHighValueTimes = 0;

		// if not charging and notification is enabled in settings
		if (value != null && !isCharging &&
				settings.getBoolean(context.getString(R.string.pref_notification_enabled_key), false)) {
			numberOfHighValueTimes = HandleHighAlert(context, settings, value);
		}
		
		if (settings.getBoolean(context.getString(R.string.pref_show_info_notification_key), false)) {
			int infoState = 
					Integer.parseInt(settings.getString(context.getString(R.string.pref_show_info_notification_state_key), "0"));
			boolean showInfoNotification = infoState == 0 || (isCharging && infoState == 1) ||
					(!isCharging && infoState == 2);		
			if (showInfoNotification) {			
				final String notification_text = currentText + " - " + batteryLevelText + " - " +
						voltageText + " - " + temperatureText;		
				updateInfoNotification(context, notification_text);
			} else {
				clearInfoNotification(context);
			}
		} 
		
		SharedPreferences.Editor editor = settings.edit();
		//editor.putInt("lastBatteryLevel", batteryLevel);
		editor.putInt("numberOfHighValueTimes", numberOfHighValueTimes);
		editor.putString("0_text", currentText);
		editor.putString("1_text", batteryLevelText);
		editor.putString("2_text", voltageText);
		editor.putString("3_text", temperatureText);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			editor.apply();
		} else {			
			editor.commit();
		}
		/*editor.putFloat("lastTime", currentTime);
		editor.putFloat("lastVoltage", currentVoltage);*/
		
		// Write to log file.
		// && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
		// @@@@@@@@@@
		if (settings.getBoolean(context.getString(R.string.pref_log_enabled_key), false) && doLogFile) {			
			final String defaultLogfile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/currentwidget.log";			
			try {				
				long logMaxSize = Long.parseLong(settings.getString(context.getString(R.string.pref_log_maxsize), "500000"));				 
				if (logMaxSize > 0) {
					File f = new File(settings.getString(context.getString(R.string.pref_log_filename_key),							
							defaultLogfile));					
					if (f.length() >= logMaxSize) {
						if (settings.getBoolean(context.getString(R.string.pref_log_rotation), false)) {
							String filename = defaultLogfile; //settings.getString(context.getString(R.string.pref_log_filename_key), "/sdcard/currentwidget.log");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
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

				String str = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)).format(new Date()) + ",";
				if (!isCharging) {
					str += "-";
				}
				str += currentText;
				// add battery level
				str += "," + batteryLevelText;
				if (settings.getBoolean(context.getString(R.string.pref_log_apps_key), false)) {
					ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
					List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();
					if (runningApps != null) {
						str += ",";
						for (RunningAppProcessInfo processInfo : runningApps) {
							str += processInfo.processName + ";";
						}
					}
				}
				// Voltage, Temperature
				str += "," + voltageText + "," + temperatureText;
				str += "\r\n";
				logOutput.writeBytes(str);
				logOutput.close();
				logFile.close();				
			}
			catch (Exception ex) {
				Log.e("CurrentWidget", ex.getMessage(), ex);
				/*AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(ex.getMessage() + " - " + ex.getStackTrace().toString() + " - " + ex.toString()).create().show();*/				
			}			
		}		

		
		// Update AppWidget.
		// Find Widgets.
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName[] widgets = 
			{new ComponentName("com.manor.currentwidget", "com.manor.currentwidget.library.CurrentWidget"),
				new ComponentName("com.manor.currentwidget", "com.manor.currentwidget.library.CurrentWidgetText")};
		for (ComponentName widget : widgets) {
			int[] ids = appWidgetManager.getAppWidgetIds(widget);
			if (ids != null && ids.length > 0) {
				CurrentWidget.updateAppWidget(context.getApplicationContext(), appWidgetManager, ids[0], false);
			}
		}
		
		// Update notifications
		
		AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		long secondsInterval = 60;
		try {
			secondsInterval = Long.parseLong(settings.getString(context.getString(R.string.pref_interval_key), "60"));
		}
		catch(Exception ex) {
			secondsInterval = 60;
			Log.e("CurrentWidget", ex.getMessage());
			ex.printStackTrace();
		}

		Intent widgetUpdate = new Intent(context.getApplicationContext(), Updater.class);
		widgetUpdate.setAction("com.manor.currentwidget.UPDATE");
		PendingIntent widgetUpdatePi = PendingIntent.getBroadcast(context, 0, widgetUpdate,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// @@@@@@@@@@@@ TODO: DON'T FORGET TO TEST ON FROYO OR SOMETHING OLD EVEN 1.6
		// ALSO TEST IF THIS IS OK, I.E. NO EXTRA ALARMS?
		if (secondsInterval > 0) {       	    
			if (settings.getBoolean(context.getString(R.string.pref_force_sleep_log), false)) {
				alarms.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (secondsInterval*1000),
						secondsInterval * 1000, widgetUpdatePi);       
			} else {
				alarms.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + (secondsInterval*1000),
						secondsInterval * 1000, widgetUpdatePi);
			}
		}
		else {
			alarms.cancel(widgetUpdatePi);
		}
	}

}
