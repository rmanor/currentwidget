package com.manor.currentwidget.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ExcludedAppsActivity extends ListActivity {

	private MyAppInfo[] mAllApps = null;
	public final static String EXCLUDED_PREFIX = "excluded_";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences settings = getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		// load
		PackageManager pm = getApplicationContext().getPackageManager();
		
		
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = pm.queryIntentActivities( mainIntent, 0);
		
		
		ArrayList<MyAppInfo> allApps = new ArrayList<MyAppInfo>(pkgAppsList.size());

		for (ResolveInfo info : pkgAppsList) {
			if (info.activityInfo != null) {
				allApps.add(new MyAppInfo(info.activityInfo.loadLabel(pm).toString(), 
						info.activityInfo.processName));				
				
			}
		}
		
		mAllApps = new MyAppInfo[allApps.size()];
		mAllApps = allApps.toArray(mAllApps);
		
		Arrays.sort(mAllApps);
		
		setListAdapter(new ArrayAdapter<MyAppInfo>(this, 
				android.R.layout.simple_list_item_multiple_choice, 
				mAllApps));

		int position = 0;
		for (MyAppInfo m : mAllApps) {
			if (settings.getBoolean(EXCLUDED_PREFIX + m.getPackage(), false)) {
				getListView().setItemChecked(position, true);
			}
			++position;
		}
			
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		SharedPreferences settings = getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		
		Editor editor = settings.edit();
		editor.putBoolean(EXCLUDED_PREFIX + 
				((MyAppInfo)l.getItemAtPosition(position)).getPackage(), 
				l.isItemChecked(position));
		editor.commit();
	}
	
	class MyAppInfo implements Comparable<MyAppInfo> {
		private String mPackage;
		private String mName;
		
		MyAppInfo(String name, String pkg) {
			mName = name;
			mPackage = pkg;
		}
		
		@Override
		public String toString() {
			return mName;
		}
		
		public String getPackage() {
			return mPackage;
		}

		@Override
		public int compareTo(MyAppInfo another) {
			
			return mName.compareToIgnoreCase(another.mName);
		}
	}
	
}
