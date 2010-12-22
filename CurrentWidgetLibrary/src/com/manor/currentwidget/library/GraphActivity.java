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
import java.io.FileInputStream;
import java.io.IOException;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class GraphActivity extends Activity {
	
	  private XYMultipleSeriesDataset _dataset = new XYMultipleSeriesDataset();
	  private XYMultipleSeriesRenderer _renderer = new XYMultipleSeriesRenderer();
	  private XYSeries _series;
	  //private XYSeriesRenderer mCurrentRenderer;
	  private GraphicalView _chartView;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.graph_layout);	
		
		// read logfile, add as series
		SharedPreferences settings = getApplicationContext().getSharedPreferences(CurrentWidgetConfigure.SHARED_PREFS_NAME, 0);
		
		XYSeries _series = new XYSeries("Electric Current");
		_dataset.addSeries(_series);
		_renderer.addSeriesRenderer(new XYSeriesRenderer());
		
		FileInputStream logFile;
		try {
			logFile = new FileInputStream(settings.getString(getApplicationContext().getString(R.string.pref_log_filename_key), "/sdcard/currentwidget.log"));
			DataInputStream ds = new DataInputStream(logFile);
			
			String line = null;
			int x = 0;
			while ( ( line = ds.readLine() ) != null ) {
				
				// 0 is datetime , 1 is value, 3 all the rest
				String[] tokens = line.split(",", 3);
				
				// add to graph series
				//tokens[1]	
				Log.d("CurrentWidget", line);
				if (tokens.length > 1) {
					_series.add(x, Double.parseDouble(tokens[1]));
					x = x + 1;
				}					
				       
			}
			
			ds.close();
			logFile.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (_chartView != null)
			_chartView.repaint();
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (_chartView == null) {
		
	      LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
	      _chartView = ChartFactory.getLineChartView(this, _dataset, _renderer);
	      layout.addView(_chartView, new LayoutParams(LayoutParams.FILL_PARENT,	    		  
	          LayoutParams.FILL_PARENT));
	      
		}		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

}
