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
import android.app.backup.BackupManager;
import android.appwidget.AppWidgetManager;
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

	public static final String URL = "https://market.android.com/details?id=com.manor.currentwidget";
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	// The request code must be 0 or higher.
	public static final int PLUS_ONE_REQUEST_CODE = 1;

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	public final static String SHARED_PREFS_NAME = "currentWidgetPrefs";

	private XYMultipleSeriesDataset mDataset = null;
	private XYMultipleSeriesRenderer mRenderer = null;

	// @@@ when you'll have more results types, move it to a resultsDataHolder or move in extras
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

		mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setZoomEnabled(true);
		mRenderer.setZoomEnabled(true, true);
		mRenderer.setPanEnabled(true);
		mRenderer.setPanEnabled(true, true);
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.WHITE);
		r.setFillPoints(true);
		r.setLineWidth(4);
		mRenderer.addSeriesRenderer(r);

		mRenderer.setYTitle("mA");
		mRenderer.setXTitle("Date/Time");
		mRenderer.setAxesColor(Color.DKGRAY);
		mRenderer.setLabelsColor(Color.WHITE);
		mRenderer.setAxisTitleTextSize(14);
		mRenderer.setLabelsTextSize(20);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
			} catch (SendIntentException e) {
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
	}

	@Override
	public void onDisconnected() {
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_show_info_notification_state_key)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_log_filename_key)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_op_type_key)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_op_value_key)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_temp_units_key)));
	}
	
	private Intent getUpdateIntent(int appWdgetId) {
		Intent updateIntent = new Intent(getApplicationContext(), CurrentWidget.class);
		updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWdgetId);
		updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,	new int[] { appWdgetId });
		updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		updateIntent.setData(Uri.withAppendedPath(
				Uri.parse("droidrm://widget/id/"), String.valueOf(appWdgetId)));
		return updateIntent;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				sendBroadcast(getUpdateIntent(mAppWidgetId));
			} else {
				//ComponentName name = new ComponentName(getApplicationContext(), com.manor.currentwidget.library.CurrentWidget.class);
				/*mClass	"com.manor.currentwidget.library.CurrentWidget" (id=830034730760)	
				mPackage	"com.manor.currentwidget" (id=830034730656)*/	

				/*ComponentName name = new ComponentName("com.manor.currentwidget", "com.manor.currentwidget.library.CurrentWidget");
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
				int[] ids = appWidgetManager.getAppWidgetIds(name);
				List<AppWidgetProviderInfo> l = appWidgetManager.getInstalledProviders();
				for (AppWidgetProviderInfo p : l) {
					Log.e("CurrentWidget", p.provider.toString());
					if (p.provider.getClassName().equals("com.manor.currentwidget.library.CurrentWidget")) {
						ids = appWidgetManager.getAppWidgetIds(p.provider);
					}
				}
				if (ids != null && ids.length > 0) {
					getApplicationContext().sendBroadcast(getUpdateIntent(ids[0]));
					CurrentWidget.updateAppWidget(getApplicationContext(), appWidgetManager, ids[0], false);
				}*/
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
		if (Build.VERSION.SDK_INT >= 8) {
			// @@@ Test backup
			 BackupManager backupManager = new BackupManager(this);
			 backupManager.dataChanged();
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

				while ((line = ds.readLine()) != null && !isCancelled()) {
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
			if (!isCancelled() && result != null) {
				_progressDialog.dismiss();
				mDataset = new XYMultipleSeriesDataset();
				mDataset.addSeries(result);
				Intent i = ChartFactory.getTimeChartIntent(
						getApplicationContext(), mDataset, mRenderer, null);				
				startActivity(i);
			}
		}
		
		@Override
		protected void onCancelled() {
			_progressDialog.dismiss();
			super.onCancelled();
		}
	};
	
	private void HandleShowNotification(boolean check) {
		if (check) {
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
	}
	
	private void LaunchMarket(String marketUrl, String webUrl) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(marketUrl));
			startActivity(intent);
		} catch (Exception ex) {
			ex.printStackTrace();
			// market not installed, send to browser
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
			startActivity(intent);
		}		
	}

	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		final String defaultLogfile = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/currentwidget.log";

		if (preference.getKey().equals(getString(R.string.pref_show_info_notification_key))) {
			HandleShowNotification(((CheckBoxPreference)preference).isChecked());
			return true;
		} else if (preference.getKey().equals("view_log")) {
			@SuppressWarnings("deprecation")
			String logFilename = getPreferenceManager().getSharedPreferences()
					.getString(getString(R.string.pref_log_filename_key), defaultLogfile);
			File logFile = new File(logFilename);
			if (logFile.exists()) {
				Intent viewLogIntent = new Intent(Intent.ACTION_VIEW);
				viewLogIntent.setDataAndType(Uri.fromFile(logFile), "text/plain");
				startActivity(Intent.createChooser(viewLogIntent, "CurrentWidget"));
			} else {
				new AlertDialog.Builder(this).setMessage("Log file not found")
						.setPositiveButton("OK", null).show();
			}
			return true;
		} else if (preference.getKey().equals("donate")) {
			LaunchMarket("market://details?id=com.manor.currentwidgetpaid", "http://play.google.com/store/apps/details?id=com.manor.currentwidgetpaid");
			return true;
		} else if (preference.getKey().equals("rate")) {
			LaunchMarket("market://details?id=com.manor.currentwidget", "http://play.google.com/store/apps/details?id=com.manor.currentwidget");
			return true;
		} else if (preference.getKey().equals("moreApps")) {
			LaunchMarket("market://search?q=pub:RmDroider", "http://play.google.com/store/search?q=RmDroider");
			return true;
		} else if (preference.getKey().equals("view_graph")) {
			SharedPreferences settings = getSharedPreferences(SHARED_PREFS_NAME, 0);
			File f = new File(
					settings.getString(getApplicationContext().getString(R.string.pref_log_filename_key), defaultLogfile));
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
								public void onClick(DialogInterface dialog, int which) {
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
								public void onClick(DialogInterface dialog,	int which) {
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
			new logLineProcessorAsyncTask()
					.execute(new TopProcessesLineProcessor());
			return true;
		} else if (preference.getKey().equals("analyze_values_count")) {
			new logLineProcessorAsyncTask()
					.execute(new ValuesCountLineProcessor());
			return true;
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
			return true;
		}
		return false;
	};

	private class logLineProcessorAsyncTask extends
			AsyncTask<ILogLineProcessor, Integer, ITwoValuesResult[]> {
		private ProgressDialog dialog = null;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
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
			if (!isCancelled()) {
				dialog.dismiss();
				if (result == null || result.length == 0) {		
					new AlertDialog.Builder(CurrentWidgetConfigure.this)
							.setMessage("No log data")
							.setPositiveButton("OK", null).show();		
					return;
				}
				Intent i = new Intent(getApplicationContext(), ResultsActivity.class);
				startActivity(i);
			}
		}
		
		@Override
		protected void onCancelled() {
			dialog.dismiss();
			super.onCancelled();
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
