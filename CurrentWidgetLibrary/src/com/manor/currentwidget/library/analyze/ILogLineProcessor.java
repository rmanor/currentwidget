package com.manor.currentwidget.library.analyze;

public interface ILogLineProcessor {

	void process(String line);
	Object[] getResult();
}
