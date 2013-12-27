/*
 *  Copyright (c) 2010-2014 Ran Manor
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

package com.manor.currentwidget.library.analyze;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.manor.currentwidget.library.R;

public class TwoValuesResultAdapter extends ArrayAdapter<ITwoValuesResult> {

	public TwoValuesResultAdapter(Context context, int textViewReousrceId, ITwoValuesResult[] items) {
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
