package com.manor.currentwidget.library;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.plus.PlusOneButton;

public class PlusOnePreference extends Preference { 

	public PlusOnePreference(Context context) {
		super(context);		
	}
	
	public PlusOnePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public PlusOnePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);		
		PlusOneButton plusOneButton = (PlusOneButton)view.findViewById(R.id.plus_one_button);
		plusOneButton.initialize(CurrentWidgetConfigure.URL, CurrentWidgetConfigure.PLUS_ONE_REQUEST_CODE);
	}
}
