package com.manor.currentwidget.library;

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

@TargetApi(8)
public class CwBackupAgent extends BackupAgentHelper {
	
	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferencesBackupHelper helper = 
			new SharedPreferencesBackupHelper(this, CurrentWidgetConfigure.SHARED_PREFS_NAME);
		
		addHelper(CurrentWidgetConfigure.SHARED_PREFS_NAME, helper);
		
	}

}
