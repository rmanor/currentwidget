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

import android.content.SharedPreferences;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class CurrentWidgetExtension extends DashClockExtension {

	@Override
	protected void onInitialize(boolean isReconnect) {
		setUpdateWhenScreenOn(true);
		super.onInitialize(isReconnect);
	}
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
