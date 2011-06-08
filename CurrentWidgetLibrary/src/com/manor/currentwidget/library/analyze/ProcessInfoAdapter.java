package com.manor.currentwidget.library.analyze;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.manor.currentwidget.library.R;

public class ProcessInfoAdapter extends ArrayAdapter<ProcessInfo> {

	public ProcessInfoAdapter(Context context, int textViewReousrceId, ProcessInfo[] items)
	{
		super(context, textViewReousrceId, items);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		 if (v == null) {
             LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.process_info_row, null);
         }
         ProcessInfo p = getItem(position);
         if (p != null) {
                 TextView tt = (TextView) v.findViewById(R.id.processName);
                 TextView bt = (TextView) v.findViewById(R.id.meanCurrent);
                 if (tt != null) {
                       tt.setText(p.processName);             
                       }
                 if(bt != null){
                       bt.setText(Float.toString(p.getMean()));
                 }
         }
         return v;
	}
}
