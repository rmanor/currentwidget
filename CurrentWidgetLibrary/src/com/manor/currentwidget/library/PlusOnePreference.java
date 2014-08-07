package com.manor.currentwidget.library;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.plus.PlusOneButton;

public class PlusOnePreference extends Preference { 

	private PlusOneButton mPlusOneButton = null;
	
	public PlusOnePreference(Context context) {
		super(context);		
	}
	
	public PlusOnePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public PlusOnePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void Initialize() {
		if (mPlusOneButton != null) {
			mPlusOneButton.initialize(CurrentWidgetConfigure.URL, CurrentWidgetConfigure.PLUS_ONE_REQUEST_CODE);			
		}
	}
	
	@Override
	protected void onAttachedToActivity() {
		super.onAttachedToActivity();
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mPlusOneButton = (PlusOneButton)view.findViewById(R.id.plus_one_button);
		Initialize();
	}
}
