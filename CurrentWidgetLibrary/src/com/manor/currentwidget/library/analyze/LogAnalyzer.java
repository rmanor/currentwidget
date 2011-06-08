package com.manor.currentwidget.library.analyze;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;

import com.manor.currentwidget.library.CurrentWidgetConfigure;
import com.manor.currentwidget.library.R;

public class LogAnalyzer {

	public static HashMap<String, ProcessInfo> getProcessesSortedByAverageCurrent(Context context) {		
		
		SharedPreferences settings = context.getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		
		FileInputStream logFile = null;
		
		HashMap<String, ProcessInfo> processesData = new HashMap<String, ProcessInfo>();

		try {
			logFile = new FileInputStream(settings.getString(context.getString(R.string.pref_log_filename_key), "/sdcard/currentwidget.log"));
			DataInputStream ds = new DataInputStream(logFile);

			String line = null;
			String[] tokens = null;
			String[] processes = null;			
			long value = 0;
			while ( ( line = ds.readLine() ) != null) {

				// 0 is date/time , 1 is value, 2 battery level, 3 running processes separated by semicolons
				tokens = line.split(",", 4);	
				
				// @@@ TAKE ONLY WHEN DRAWING! ADD TO HELP
				
				// remove mA at the end
				value = Long.parseLong(tokens[1].substring(0, tokens[1].length()-2));
					
				// if there is apps info
				if (tokens.length >= 4 /*&& value  < 0 */) {
					
					processes = tokens[3].split(";");
					for (int i=0;i<processes.length;i++) {
						if (!processesData.containsKey(processes[i])) {
							processesData.put(processes[i], new ProcessInfo(processes[i], value));
						}
						else {
							processesData.get(processes[i]).addElectricCurrent(value);
						}
					}
				}					

			}		

			ds.close();
			logFile.close();			
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return processesData;

	}
}
