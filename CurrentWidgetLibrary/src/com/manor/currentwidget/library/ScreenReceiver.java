/*package com.manor.currentwidget.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {
	
	public static boolean screenStatus = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (action == null)
			return;
		if (Intent.ACTION_SCREEN_ON.equals(action)) {
			Log.d("CurrentWidget", "screen on");
			screenStatus = true;
		}
		else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			Log.d("CurrentWidget", "screen off");
			screenStatus = false;
		}		
	}

}
*/