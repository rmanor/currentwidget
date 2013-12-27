package com.manor.currentwidget.library.analyze;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import com.manor.currentwidget.library.CurrentWidgetConfigure;
import com.manor.currentwidget.library.R;

public class ResultsActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		ITwoValuesResult[] p = CurrentWidgetConfigure.p;
		if (p != null) {
			TwoValuesResultAdapter adapter = 
					new TwoValuesResultAdapter(this, R.layout.process_info_row, p);			
			setListAdapter(adapter);
		}		
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			Object item = l.getItemAtPosition(position);
			if (item instanceof ProcessInfo) {
				ProcessInfo p = (ProcessInfo)item;
				Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				intent.setData(Uri.parse("package:" + p.processName));
				startActivity(intent);
			}
		}
	}
}
