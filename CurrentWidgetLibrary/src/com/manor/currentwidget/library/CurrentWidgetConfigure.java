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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.achartengine.ChartFactory;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.manor.currentwidget.library.analyze.LogAnalyzer;
import com.manor.currentwidget.library.analyze.ProcessInfo;
import com.manor.currentwidget.library.analyze.ResultsActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.widget.Toast;

public class CurrentWidgetConfigure extends PreferenceActivity {

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;	

	public final static String SHARED_PREFS_NAME = "currentWidgetPrefs";
	
	private XYMultipleSeriesDataset _dataset = null;
	private XYMultipleSeriesRenderer _renderer = null;
	private ProgressDialog _progressDialog = null;
	private boolean _graphLoadingCancelled = false;
	
	// @@@ when you'll have more results, move it to a resultsDataHolder singleton class
	public static ProcessInfo[] p = null;
	
	public CurrentWidgetConfigure() {
		super();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		

		getPreferenceManager().setSharedPreferencesName(SHARED_PREFS_NAME);

		addPreferencesFromResource(R.xml.prefs);

		// get widget id
		Intent intent = getIntent();		
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

		// init result as ok
		setResult(RESULT_OK, resultValue);

		if (this.getApplicationContext().getPackageName().equals("com.manor.currentwidgetpaid")) {
			findPreference("donate").setTitle("Thank you for donating!");
		}


	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Intent updateIntent = new Intent(this.getApplicationContext(), CurrentWidget.class);
				updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mAppWidgetId } );
				updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				updateIntent.setData(Uri.withAppendedPath(Uri.parse("droidrm://widget/id/"), String.valueOf(mAppWidgetId)));

				sendBroadcast(updateIntent);
				
				// @@@ try calling the static function instead
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void startGraphActivity() {
	
		// start a thread , show progress bar, allow cancel
		
		Thread t = new Thread() {
			public void run() {
				
				SharedPreferences settings = getSharedPreferences(SHARED_PREFS_NAME, 0);

				//Intent i = new Intent(getApplicationContext(), GraphActivity.class);
				_dataset = new XYMultipleSeriesDataset();
				TimeSeries series = new TimeSeries("Electric Current");

				FileInputStream logFile = null;

				try {
					logFile = new FileInputStream(settings.getString(getApplicationContext().getString(R.string.pref_log_filename_key), "/sdcard/currentwidget.log"));
					DataInputStream ds = new DataInputStream(logFile);

					String line = null;
					String[] tokens = null;
					int x = 0;			
					while ( ( line = ds.readLine() ) != null && !_graphLoadingCancelled) {

						// 0 is datetime , 1 is value, 3 all the rest
						tokens = line.split(",", 3);						
						
						// add to graph series
						//tokens[1]	
						//Log.d("CurrentWidget", line);
						if (tokens.length > 1) {
							try {

								series.add(new Date(tokens[0]), Double.parseDouble(tokens[1].substring(0, tokens[1].length() - 2)));

								x = x + 1;
							}
							catch (Exception ex) {
								ex.printStackTrace();
							}
						}					

					}		

					ds.close();
					logFile.close();			


				} catch (IOException e) {
					e.printStackTrace();
				}

				_dataset.addSeries(series);

				_renderer = new XYMultipleSeriesRenderer();

				XYSeriesRenderer r = new XYSeriesRenderer();
				r.setColor(Color.WHITE);
				r.setFillPoints(true);
				_renderer.addSeriesRenderer(r);	    
				_renderer.setYTitle("mA");	    

				_renderer.setAxesColor(Color.DKGRAY);
				_renderer.setLabelsColor(Color.LTGRAY);
				
				runOnUiThread(_fininshedLoadingGraphRunnable);

			}
		};

		_graphLoadingCancelled = false;
		_progressDialog = ProgressDialog.show(this, "", "Loading. Please wait...", true, true, new DialogInterface.OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				_graphLoadingCancelled = true;
			}
		});

		t.start();

	}
	
	private final Runnable _fininshedLoadingGraphRunnable = new Runnable() {
		
		public void run() {
			if (!_graphLoadingCancelled) {
				Intent i = ChartFactory.getTimeChartIntent(getApplicationContext(), _dataset, _renderer, null);
				startActivity(i);
			}
			_progressDialog.dismiss();
		};
	};


	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

		if (preference.getKey().equals("view_log")) {
			String logFilename = getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_log_filename_key), "/sdcard/currentwidget.log");
			File logFile = new File(logFilename);
			if (logFile.exists()) {
				Intent viewLogIntent = new Intent(Intent.ACTION_VIEW);					
				viewLogIntent.setDataAndType(Uri.fromFile(logFile), "text/plain");
				startActivity(Intent.createChooser(viewLogIntent, "CurrentWidget"));
			}
			else {
				new AlertDialog.Builder(this).setMessage("Log file not found").setPositiveButton("OK", null).show();						
			}

			return true;

		} else if (preference.getKey().equals("donate")) {

			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.manor.currentwidgetpaid"));						
				startActivity(intent);
			}
			catch (Exception ex) {
				ex.printStackTrace();
				// market not installed, send to browser
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=com.manor.currentwidgetpaid"));						
				startActivity(intent);
			}			
			return true;
		} else if (preference.getKey().equals("view_graph")) {

			SharedPreferences settings = getSharedPreferences(SHARED_PREFS_NAME, 0);
			File f = new File(settings.getString(getApplicationContext().getString(R.string.pref_log_filename_key), "/sdcard/currentwidget.log"));

			if (f.exists()) {
				startGraphActivity();
			}
			else {
				new AlertDialog.Builder(this).setMessage("Log file not found").setPositiveButton("OK", null).show();
			}


			return true;

		} else if (preference.getKey().equals("clear_log")) {

			new AlertDialog.Builder(this).setMessage("Are you sure you want to delete the log file?")
						.setCancelable(false)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							
							
							public void onClick(DialogInterface dialog, int which) {
								
								SharedPreferences settings = getSharedPreferences(SHARED_PREFS_NAME, 0);
								File f = new File(settings.getString(getApplicationContext().getString(R.string.pref_log_filename_key), "/sdcard/currentwidget.log"));
								Toast t = null;
								if (f.exists()) {
									if (f.delete())
										t = Toast.makeText(getApplicationContext(), "Log file deleted", Toast.LENGTH_LONG);
									else
										t = Toast.makeText(getApplicationContext(), "Error deleting log file", Toast.LENGTH_LONG);
								}
								else {
									t = Toast.makeText(getApplicationContext(), "No log file", Toast.LENGTH_LONG);
								}

								t.show();
								
							}
						})
						.setNegativeButton("No", new DialogInterface.OnClickListener() {
							
							
							public void onClick(DialogInterface dialog, int which) {
								
								dialog.dismiss();
								
							}
						}).show();
			

			return true;
		} else if (preference.getKey().equals("analyze_help")) {

			new AlertDialog.Builder(this).setTitle("Analyze Help").
			setMessage(getString(R.string.pref_analyze_help)).
			setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
			}).show();
			
			return true;
		} else if (preference.getKey().equals("analyze_top_processes")) {
			
			LogAnalyzer.getInstance(getApplicationContext()).getProcessesSortedByAverageCurrent();			
			
			new getProcessesSortedByAverageCurrentAsyncTask().execute((Void[])null);
			//CurrentWidgetConfigure.p = p;
			
			/*Intent i = new Intent(this, ResultsActivity.class);
			startActivity(i);*/
			
			return true;
		}

		return false;
	};
	
	private class getProcessesSortedByAverageCurrentAsyncTask extends AsyncTask<Void, Integer, ProcessInfo[]> {
		
		private ProgressDialog dialog = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			dialog = ProgressDialog.show(CurrentWidgetConfigure.this, "", "Loading. Please Wait...", true, true, new OnCancelListener() {
				
				public void onCancel(DialogInterface dialog) {
					cancel(true);					
				}
			});
			
			dialog.setProgress(0);
					
		}
		
		@Override
		protected ProcessInfo[] doInBackground(Void... params) {
			
			SharedPreferences settings = getApplicationContext().getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
			
			FileInputStream logFile = null;
			
			HashMap<String, ProcessInfo> processesData = new HashMap<String, ProcessInfo>();

			try {
				logFile = new FileInputStream(settings.getString(getApplicationContext().getString(R.string.pref_log_filename_key), "/sdcard/currentwidget.log"));
				int fileSize = logFile.available();
				int bytesRead = 0;
				
				DataInputStream ds = new DataInputStream(logFile);

				String line = null;
				String[] tokens = null;
				String[] processes = null;			
				long value = 0;
				while ( ( line = ds.readLine() ) != null && !isCancelled()) {

					bytesRead += line.length();
					publishProgress(bytesRead/fileSize);
					// 0 is date/time , 1 is value, 2 battery level, 3 running processes separated by semicolons, 4 all the rest
					tokens = line.split(",", 5);					
				
					try
					{
						// remove mA at the end
						value = Long.parseLong(tokens[1].substring(0, tokens[1].length()-2));
					}
					catch (NumberFormatException nfe)
					{
						value = 0;
					}
						
					// if there is apps info & drawing value
					if (tokens.length >= 4 && value  < 0) {
						value = Math.abs(value);
						processes = tokens[3].split(";");
						for (String process : processes) {
							process = process.trim();
							if (!processesData.containsKey(process)) {
								processesData.put(process, new ProcessInfo(process, value));
							}
							else {
								processesData.get(process).addElectricCurrent(value);
							}
						}
					}					

				}		

				ds.close();
				logFile.close();	
				
				// copy to array and merge sort
				ProcessInfo[] result = new ProcessInfo[processesData.size()];
				int i = -1;
				for (String k : processesData.keySet())
				{
					result[++i] = processesData.get(k);
				}
				
				Arrays.sort(result);	
						
				return result;			

			} catch (IOException e) {
				e.printStackTrace();
				// @@@ show error message
			}			
		
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			dialog.setProgress(values[0]);
		}
		
		@Override
		protected void onPostExecute(ProcessInfo[] result) {
			super.onPostExecute(result);
			
			CurrentWidgetConfigure.p = result;
			dialog.dismiss();
			
			if (result == null || result.length == 0) {
				
				new AlertDialog.Builder(CurrentWidgetConfigure.this).setMessage("No log data").setPositiveButton("OK", null).show();				
				
				return;
			}

			Intent i = new Intent(getApplicationContext(), ResultsActivity.class);
			startActivity(i);			
			
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			
			dialog.dismiss();
			
		}
	};


}
