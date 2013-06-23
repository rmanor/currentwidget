package com.manor.currentwidget.library;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusOneButton;

public class PlusOnePreference extends Preference { 

	private PlusClient mPlusClient = null; 
	
	public PlusOnePreference(Context context) {
		super(context);		
	}
	
	public PlusOnePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public PlusOnePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setPlusClient(PlusClient plusClient) {
		mPlusClient = plusClient;
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);		
		if (mPlusClient != null) {
			PlusOneButton plusOneButton = (PlusOneButton)view.findViewById(R.id.plus_one_button);
			plusOneButton.initialize(mPlusClient, CurrentWidgetConfigure.URL, CurrentWidgetConfigure.PLUS_ONE_REQUEST_CODE);
		}
	}
}
