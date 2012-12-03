package com.manor.currentwidget.library.analyze;

import android.app.ListActivity;
import android.os.Bundle;

import com.manor.currentwidget.library.CurrentWidgetConfigure;
import com.manor.currentwidget.library.R;

public class ResultsActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
	
		
		//Parcelable[] p = getIntent().getParcelableArrayExtra("com.manor.currentwidget.ProcessInfo");
		
		ITwoValuesResult[] p = CurrentWidgetConfigure.p;

		if (p != null) {
			TwoValuesResultAdapter adapter = 
					new TwoValuesResultAdapter(this, R.layout.process_info_row, p);
			/*ArrayAdapter<Parcelable> adapter = 
			new ArrayAdapter<Parcelable>(this, 
						android.R.layout.simple_list_item_1, 
					p);*/
			
			// 	Bind to our new adapter.
			setListAdapter(adapter);
		}
	}
}
