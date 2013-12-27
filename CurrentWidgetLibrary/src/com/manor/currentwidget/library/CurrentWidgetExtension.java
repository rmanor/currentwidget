package com.manor.currentwidget.library;

import android.content.SharedPreferences;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class CurrentWidgetExtension extends DashClockExtension {

	@Override
	protected void onUpdateData(int reason) {
		SharedPreferences settings = 
				getApplicationContext().getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		
		String currentText = settings.getString("0_text", "");
		
		if (currentText.length() > 0) {
			publishUpdate(new ExtensionData()
				.visible(true)
				.icon(R.drawable.icon)
				.status(currentText)
				.expandedTitle("CurrentWidget: " + currentText)
				/*.expandedBody(
						"Thanks for checking out this example extension for DashClock.")*/
				.contentDescription(
						"The amount of electric current the device is using (mA)."));
				/*.clickIntent(
						new Intent(Intent.ACTION_VIEW, Uri
								.parse("http://www.google.com"))));*/
		} else {
			publishUpdate(new ExtensionData().visible(false));
		}

	}
}
