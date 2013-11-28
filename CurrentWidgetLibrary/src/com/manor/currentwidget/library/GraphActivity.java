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

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

public class GraphActivity extends Activity {

	private XYMultipleSeriesDataset _dataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer _renderer = new XYMultipleSeriesRenderer();
	private GraphicalView _chartView;

	public static final String EXTRA_DATASET = "extra_dataset";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		_dataset = (XYMultipleSeriesDataset) extras.getSerializable(EXTRA_DATASET);

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
		/*r.setPointStyle(PointStyle.CIRCLE);
		r.setDisplayChartValues(false);*/
	        
		_renderer.addSeriesRenderer(r);
		_renderer.setYTitle("mA");
		_renderer.setXTitle("Date/Time");
		_renderer.setAxesColor(Color.DKGRAY);
		_renderer.setLabelsColor(Color.WHITE);
		_renderer.setAxisTitleTextSize(14);
		_renderer.setLabelsTextSize(20);
		
		//_renderer.setClickEnabled(true);		
		//_renderer.setPointSize(4);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (_chartView == null) {
			_chartView = ChartFactory.getTimeChartView(this, _dataset,
					_renderer, null);
			setContentView(_chartView);
			// enable the chart click events
			_renderer.setClickEnabled(true);
			_renderer.setSelectableBuffer(10);
			_renderer.setZoomEnabled(true);
			_renderer.setZoomEnabled(true, true);
			_renderer.setPanEnabled(true);
			_renderer.setPanEnabled(true, true);

			/*_chartView.setOnLongClickListener(new View.OnLongClickListener() {
				public boolean onLongClick(View v) {
					// handle the click event on the chart
					SeriesSelection seriesSelection = _chartView
							.getCurrentSeriesAndPoint();
					if (seriesSelection == null) {
						//Toast.makeText(GraphActivity.this, "nothing", Toast.LENGTH_SHORT).show();
						return false;
					} else {
						// display information of the clicked point
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy/MM/dd HH:mm:ss", Locale.US);
						Date d = new Date((long)seriesSelection.getXValue());						
						Toast.makeText(
								GraphActivity.this,
										" closest point value X="
										+ dateFormat.format(d) + ", Y="
										+ seriesSelection.getValue(),
								Toast.LENGTH_SHORT).show();
						return true;
					}
				}
			})*/;
		} else {
			_chartView.repaint();
		}

	}
}
