/*
 *  Copyright (c) 2010-2013 Ran Manor
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.manor.currentwidget.library.analyze.ILogLineProcessor;
import com.manor.currentwidget.library.analyze.ITwoValuesResult;
import com.manor.currentwidget.library.analyze.ResultsActivity;
import com.manor.currentwidget.library.analyze.TopProcessesLineProcessor;
import com.manor.currentwidget.library.analyze.ValuesCountLineProcessor;

public class CurrentWidgetConfigure extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, ConnectionCallbacks,
		OnConnectionFailedListener {

	// private PlusClient mPlusClient;
	// private PlusOneButton mPlusOneButton;

	public static final String URL = "https://market.android.com/details?id=com.manor.currentwidget";
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	// The request code must be 0 or higher.
	public static final int PLUS_ONE_REQUEST_CODE = 1;

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	public final static String SHARED_PREFS_NAME = "currentWidgetPrefs";

	private XYMultipleSeriesDataset _dataset = null;
	private XYMultipleSeriesRenderer _renderer = null;

	// @@@ when you'll have more results types, move it to a resultsDataHolder
	// singleton class
	public static ITwoValuesResult[] p = null;

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (getString(R.string.pref_value_text_size_key).equals(key)) {
			if (TextUtils.isEmpty(sharedPreferences.getString(key, ""))) {
				((EditTextPreference) findPreference(key))
						.setText(getString(R.string.pref_value_text_size_default));
			}
		} else if (getString(R.string.pref_last_updated_text_size_key).equals(
				key)) {
			if (TextUtils.isEmpty(sharedPreferences.getString(key, ""))) {
				((EditTextPreference) findPreference(key))
						.setText(getString(R.string.pref_last_updated_text_size_default));
			}
		} else if (getString(R.string.pref_update_now_text_size_key)
				.equals(key)) {
			if (TextUtils.isEmpty(sharedPreferences.getString(key, ""))) {
				((EditTextPreference) findPreference(key))
						.setText(getString(R.string.pref_update_now_text_size_default));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName(SHARED_PREFS_NAME);

		addPreferencesFromResource(R.xml.prefs);

		// get widget id
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

		// init result as ok
		setResult(RESULT_OK, resultValue);

		if (this.getApplicationContext().getPackageName()
				.equals("com.manor.currentwidgetpaid")) {
			findPreference("donate").setTitle("Thank you for donating!");
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR_MR1) {
			Preference p = findPreference(getString(R.string.pref_notification_screen_off_key));
			p.setEnabled(false);
			p.setSummary("Requires Android 2.1+");
			((CheckBoxPreference) p).setChecked(false);
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) {
			/*
			 * Preference p =
			 * findPreference(getString(R.string.pref_notification_exclude_bluetooth
			 * )); p.setEnabled(false); p.setSummary("Requires Android 2.0+");
			 * ((CheckBoxPreference)p).setChecked(false);
			 */
			Preference p = findPreference(getString(R.string.pref_notification_exclude_headset));
			p.setEnabled(false);
			p.setSummary("Requires Android 2.0+");
			((CheckBoxPreference) p).setChecked(false);
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			Preference p = findPreference(getString(R.string.pref_value_text_size_key));
			p.setEnabled(false);
			p.setSummary("Requires Android 4.1+");
			p = findPreference(getString(R.string.pref_last_updated_text_size_key));
			p.setEnabled(false);
			p.setSummary("Requires Android 4.1+");
			p = findPreference(getString(R.string.pref_update_now_text_size_key));
			p.setEnabled(false);
			p.setSummary("Requires Android 4.1+");
		}

		/*
		 * SharedPreferences settings = getSharedPreferences(SHARED_PREFS_NAME,
		 * 0);
		 * 
		 * findPreference("text_textColor").setEnabled(settings.getString(getString
		 * (R.string.pref_widget_type_key), "0").equals("1"));
		 * 
		 * findPreference(getString(R.string.pref_widget_type_key)).
		 * setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		 * 
		 * public boolean onPreferenceChange(Preference preference, Object
		 * newValue) {
		 * 
		 * Preference p = findPreference("text_textColor");
		 * p.setEnabled(Integer.parseInt(newValue.toString()) == 1);
		 * 
		 * return true; } });
		 */

		/*
		 * mPlusClient = new PlusClient.Builder(this, this, this).clearScopes()
		 * .build();
		 */		
		_renderer = new XYMultipleSeriesRenderer();
		_renderer.setZoomButtonsVisible(true);
		_renderer.setZoomEnabled(true);
		_renderer.setZoomEnabled(true, true);
		_renderer.setPanEnabled(true);
		_renderer.setPanEnabled(true, true);
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.WHITE);
		r.setFillPoints(true);
		r.setLineWidth(4);
		_renderer.addSeriesRenderer(r);

		_renderer.setYTitle("mA");
		_renderer.setXTitle("Date/Time");
		_renderer.setAxesColor(Color.DKGRAY);
		_renderer.setLabelsColor(Color.WHITE);
		_renderer.setAxisTitleTextSize(14);
		_renderer.setLabelsTextSize(20);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
			} catch (SendIntentException e) {
				// mPlusClient.connect();
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
	}

	@Override
	public void onDisconnected() {
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_OK) {
			// mPlusClient.connect();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// mPlusClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// mPlusClient.disconnect();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_show_info_notification_state_key)));

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * SharedPreferences settings = getSharedPreferences(SHARED_PREFS_NAME,
		 * 0); Log.d("CurrentWidget", "sound: " +
		 * settings.getString(getString(R.string.pref_notification_sound_key),
		 * ""));
		 */

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Intent updateIntent = new Intent(this.getApplicationContext(),
						CurrentWidget.class);
				updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						mAppWidgetId);
				updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
						new int[] { mAppWidgetId });
				updateIntent
						.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				updateIntent.setData(Uri.withAppendedPath(
						Uri.parse("droidrm://widget/id/"),
						String.valueOf(mAppWidgetId)));

				sendBroadcast(updateIntent);

				// @@@ try calling the static function instead
			} else {
				ComponentName name = new ComponentName(getApplicationContext(),
						CurrentWidget.class);
				AppWidgetManager appWidgetManager = AppWidgetManager
						.getInstance(getApplicationContext());
				int[] ids = appWidgetManager.getAppWidgetIds(name);
				if (ids == null || ids.length == 0) {
					// Log.e("CurrentWidget",
					// "Error - CurrentWidget not found");
				} else {
					// Log.e("CurrentWidget", "here2");
					// for (int id : ids) {
					Intent updateIntent = new Intent(getApplicationContext(),
							CurrentWidget.class);
					// /updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					// id);
					updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
							ids);
					updateIntent
							.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

					getApplicationContext().sendBroadcast(updateIntent);
					// }
					CurrentWidget.updateAppWidget(getApplicationContext(),
							appWidgetManager, ids[0], false);
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@SuppressWarnings("deprecation")
	@TargetApi(8)
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			/*
			 * BackupManager backupManager = new BackupManager(this);
			 * backupManager.dataChanged();
			 */
			try {
				Class managerClass = Class
						.forName("android.app.backup.BackupManager");
				Constructor managerConstructor = managerClass
						.getConstructor(Context.class);
				Object manager = managerConstructor.newInstance(this);
				Method m = managerClass.getMethod("dataChanged");
				m.invoke(manager);
			} catch (ClassNotFoundException e) {
				Log.d("CurrentWidget", "No Backup Mananger");
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private class StartGraphTask extends AsyncTask<Void, Integer, XYSeries> {
		private ProgressDialog _progressDialog = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			_progressDialog = new ProgressDialog(CurrentWidgetConfigure.this);
			_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			_progressDialog.setMessage("Loading. Please Wait...");
			_progressDialog.setCancelable(true);
			_progressDialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
			_progressDialog.setProgress(0);
			_progressDialog.show();
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);			
			if (values[1] != _progressDialog.getMax()) {
				_progressDialog.setMax(values[1]);
			}
			_progressDialog.setProgress(values[0]);
		}
		
		@Override
		protected XYSeries doInBackground(Void... params) {
			SharedPreferences settings = getSharedPreferences(
					SHARED_PREFS_NAME, 0);			
			TimeSeries series = new TimeSeries("Electric Current");
			FileReader logFile = null;
			try {
				final String defaultLogfile = Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/currentwidget.log";
				File f = new File(settings.getString(
						getApplicationContext().getString(
								R.string.pref_log_filename_key),
						defaultLogfile));
				logFile = new FileReader(f);
				// DataInputStream ds = new DataInputStream(logFile);
				BufferedReader ds = new BufferedReader(logFile);

				publishProgress(0, (int) f.length());				

				String line = null;
				String[] tokens = null;
				int x = 0;
				int bytesRead = 0;

				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm:ss", Locale.US);

				while ((line = ds.readLine()) != null
						&& !isCancelled()) {

					bytesRead += line.length();
					publishProgress(bytesRead, (int)f.length());

					// 0 is datetime , 1 is value, 3 all the rest
					tokens = line.split(",", 3);

					// add to graph series
					// tokens[1]
					// Log.d("CurrentWidget", line);
					if (tokens.length > 1) {
						try {
							series.add(dateFormat.parse(tokens[0]), Double
									.parseDouble(tokens[1].substring(0,
											tokens[1].length() - 2)));
							x = x + 1;
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

				}
				ds.close();
				logFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
			return series;
		}
		
		@Override
		protected void onPostExecute(XYSeries result) {
			super.onPostExecute(result);
			_progressDialog.dismiss();
			if (!isCancelled()) {
				_dataset = new XYMultipleSeriesDataset();
				_dataset.addSeries(result);
				Intent i = ChartFactory.getTimeChartIntent(
						getApplicationContext(), _dataset, _renderer, null);
				/*
				 * Intent i = new Intent(getApplicationContext(),
				 * GraphActivity.class); i.putExtra(GraphActivity.EXTRA_DATASET,
				 * _dataset);
				 */
				startActivity(i);
			}
		}
	};

	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		final String defaultLogfile = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/currentwidget.log";

		if (preference.getKey().equals(
				getString(R.string.pref_show_info_notification_key))) {
			if (((CheckBoxPreference) preference).isChecked()) {
				SharedPreferences settings = getSharedPreferences(
						SHARED_PREFS_NAME, 0);
				final String notification_text = settings.getString("0_text",
						"no data")
						+ " - "
						+ settings.getString("1_text", "no data")
						+ " - "
						+ settings.getString("2_text", "no data")
						+ " - "
						+ settings.getString("3_text", "no data");
				CurrentWidget.updateInfoNotification(getApplicationContext(),
						notification_text);
			} else {
				CurrentWidget.clearInfoNotification(getApplicationContext());
			}
		} else if (preference.getKey().equals("view_log")) {
			String logFilename = getPreferenceManager().getSharedPreferences()
					.getString(getString(R.string.pref_log_filename_key),
							defaultLogfile);
			File logFile = new File(logFilename);
			if (logFile.exists()) {
				Intent viewLogIntent = new Intent(Intent.ACTION_VIEW);
				viewLogIntent.setDataAndType(Uri.fromFile(logFile),
						"text/plain");
				startActivity(Intent.createChooser(viewLogIntent,
						"CurrentWidget"));
			} else {
				new AlertDialog.Builder(this).setMessage("Log file not found")
						.setPositiveButton("OK", null).show();
			}

			return true;

		} else if (preference.getKey().equals("donate")) {

			try {
				Intent intent = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("market://details?id=com.manor.currentwidgetpaid"));
				startActivity(intent);
			} catch (Exception ex) {
				ex.printStackTrace();
				// market not installed, send to browser
				Intent intent = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("http://play.google.com/store/apps/details?id=com.manor.currentwidgetpaid"));
				startActivity(intent);
			}
			return true;
		} else if (preference.getKey().equals("rate")) {
			try {
				Intent intent = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("market://details?id=com.manor.currentwidget"));
				startActivity(intent);
			} catch (Exception ex) {
				ex.printStackTrace();
				// market not installed, send to browser
				Intent intent = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("http://play.google.com/store/apps/details?id=com.manor.currentwidget"));
				startActivity(intent);
			}
			return true;
		} else if (preference.getKey().equals("moreApps")) {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("market://search?q=pub:RmDroider"));
				startActivity(intent);
			} catch (Exception ex) {
				ex.printStackTrace();
				// market not installed, send to browser
				Intent intent = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("http://play.google.com/store/search?q=RmDroider"));
				startActivity(intent);
			}
			return true;
		} else if (preference.getKey().equals("view_graph")) {
			SharedPreferences settings = getSharedPreferences(
					SHARED_PREFS_NAME, 0);
			File f = new File(settings.getString(getApplicationContext()
					.getString(R.string.pref_log_filename_key), defaultLogfile));
			if (f.exists()) {
				(new StartGraphTask()).execute();
			} else {
				new AlertDialog.Builder(this).setMessage("Log file not found")
						.setPositiveButton("OK", null).show();
			}

			return true;

		} else if (preference.getKey().equals("clear_log")) {

			new AlertDialog.Builder(this)
					.setMessage("Are you sure you want to delete the log file?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									SharedPreferences settings = getSharedPreferences(
											SHARED_PREFS_NAME, 0);
									File f = new File(
											settings.getString(
													getApplicationContext()
															.getString(
																	R.string.pref_log_filename_key),
													defaultLogfile));
									Toast t = null;
									if (f.exists()) {
										if (f.delete())
											t = Toast.makeText(
													getApplicationContext(),
													"Log file deleted",
													Toast.LENGTH_LONG);
										else
											t = Toast.makeText(
													getApplicationContext(),
													"Error deleting log file",
													Toast.LENGTH_LONG);
									} else {
										t = Toast.makeText(
												getApplicationContext(),
												"No log file",
												Toast.LENGTH_LONG);
									}

									t.show();

								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									dialog.dismiss();

								}
							}).show();

			return true;
		} else if (preference.getKey().equals("analyze_help")) {

			new AlertDialog.Builder(this)
					.setTitle("Analyze Help")
					.setMessage(getString(R.string.pref_analyze_help))
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();

			return true;
		} else if (preference.getKey().equals("analyze_top_processes")) {

			// LogAnalyzer.getInstance(getApplicationContext()).getProcessesSortedByAverageCurrent();

			new logLineProcessorAsyncTask()
					.execute(new TopProcessesLineProcessor());
			// CurrentWidgetConfigure.p = p;

			/*
			 * Intent i = new Intent(this, ResultsActivity.class);
			 * startActivity(i);
			 */

			return true;
		} else if (preference.getKey().equals("analyze_values_count")) {

			new logLineProcessorAsyncTask()
					.execute(new ValuesCountLineProcessor());

		} else if (preference.getKey().equals("text_textColor")) {

			SharedPreferences settings = getSharedPreferences(
					SHARED_PREFS_NAME, 0);

			AmbilWarnaDialog dialog = new AmbilWarnaDialog(this,
					settings.getInt(getString(R.string.pref_text_text_color),
							0xFFFFFFFF), new OnAmbilWarnaListener() {
						public void onOk(AmbilWarnaDialog dialog, int color) {
							// color is the color selected by the user.
							SharedPreferences settings = getSharedPreferences(
									SHARED_PREFS_NAME, 0);
							SharedPreferences.Editor editor = settings.edit();
							editor.putInt(
									getString(R.string.pref_text_text_color),
									color);
							editor.commit();
						}

						public void onCancel(AmbilWarnaDialog dialog) {
							// cancel was selected by the user
						}
					});

			dialog.show();

			return true;
		} else if (preference.getKey().equals("excludedApps")) {

			Intent i = new Intent(this, ExcludedAppsActivity.class);
			startActivity(i);

		}

		return false;
	};

	private class logLineProcessorAsyncTask extends
			AsyncTask<ILogLineProcessor, Integer, ITwoValuesResult[]> {

		private ProgressDialog dialog = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			/*
			 * dialog = ProgressDialog.show(CurrentWidgetConfigure.this, "",
			 * "Loading. Please Wait...", true, true, new OnCancelListener() {
			 * 
			 * public void onCancel(DialogInterface dialog) { cancel(true); }
			 * });
			 */

			dialog = new ProgressDialog(CurrentWidgetConfigure.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMessage("Loading. Please Wait...");
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});

			dialog.setProgress(0);
			dialog.show();

		}

		@Override
		protected ITwoValuesResult[] doInBackground(ILogLineProcessor... params) {

			SharedPreferences settings = getApplicationContext()
					.getSharedPreferences(
							CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);

			FileReader logFile = null;

			try {
				String external_path = Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ File.separator;
				logFile = new FileReader(settings.getString(
						getApplicationContext().getString(
								R.string.pref_log_filename_key), external_path
								+ "currentwidget.log"));
				File f = new File(settings.getString(getApplicationContext()
						.getString(R.string.pref_log_filename_key),
						external_path + "currentwidget.log"));
				int fileSize = (int) f.length();

				int bytesRead = 0;

				BufferedReader ds = new BufferedReader(logFile);

				String line = null;

				ILogLineProcessor logLineProcessor = params[0];

				while ((line = ds.readLine()) != null && !isCancelled()) {

					bytesRead += line.length();
					publishProgress(fileSize, bytesRead);

					logLineProcessor.process(line);

				}

				ds.close();
				logFile.close();

				return (ITwoValuesResult[]) logLineProcessor.getResult();

			} catch (IOException e) {
				e.printStackTrace();
				// @@@ show error message
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			dialog.setMax(values[0]);
			dialog.setProgress(values[1]);
		}

		@Override
		protected void onPostExecute(ITwoValuesResult[] result) {
			super.onPostExecute(result);

			CurrentWidgetConfigure.p = result;
			dialog.dismiss();

			if (result == null || result.length == 0) {

				new AlertDialog.Builder(CurrentWidgetConfigure.this)
						.setMessage("No log data")
						.setPositiveButton("OK", null).show();

				return;
			}

			Intent i = new Intent(getApplicationContext(),
					ResultsActivity.class);
			startActivity(i);

		}

		@Override
		protected void onCancelled() {
			super.onCancelled();

			dialog.dismiss();

		}
	}

	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				preference.getContext()
						.getSharedPreferences(SHARED_PREFS_NAME, 0)
						.getString(preference.getKey(), ""));
	}

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

}
