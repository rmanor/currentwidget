package com.manor.currentwidget.library.analyze;

import android.content.Context;

public interface ILogLineProcessor {

	void process(String line, Context context);
	Object[] getResult();
}
