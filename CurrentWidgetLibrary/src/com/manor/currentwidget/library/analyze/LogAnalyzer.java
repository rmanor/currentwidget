package com.manor.currentwidget.library.analyze;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.manor.currentwidget.library.CurrentWidgetConfigure;
import com.manor.currentwidget.library.R;

public class LogAnalyzer {

	private LogAnalyzer() {
		
	}
	
	public static LogAnalyzer getInstance(Context context) {
		_instance._context = context;
		return _instance;
	}
	
	private static LogAnalyzer _instance = new LogAnalyzer();
	
	private Context _context = null;
	
	public void getProcessesSortedByAverageCurrent() {		
		
		//new getProcessesSortedByAverageCurrentAsyncTask().execute(null);

	}
	
}
