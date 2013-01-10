package com.manor.currentwidget.library.analyze;

import android.content.Context;

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
