package com.manor.currentwidget.library.analyze;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.manor.currentwidget.library.R;

public class TwoValuesResultAdapter extends ArrayAdapter<ITwoValuesResult> {

	public TwoValuesResultAdapter(Context context, int textViewReousrceId, ITwoValuesResult[] items)
	{
		super(context, textViewReousrceId, items);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		 if (v == null) {
             LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.process_info_row, parent, false);
         }
         ITwoValuesResult p = getItem(position);
         if (p != null) {
                 TextView tt = (TextView) v.findViewById(R.id.processName);
                 TextView bt = (TextView) v.findViewById(R.id.meanCurrent);
                 if (tt != null) {
                       tt.setText(p.getValue1());             
                       }
                 if(bt != null){
                       bt.setText(p.getValue2());
                 }
         }
         return v;
	}
}
