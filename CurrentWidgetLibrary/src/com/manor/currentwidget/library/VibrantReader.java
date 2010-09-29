package com.manor.currentwidget.library;

import java.io.File;

public class VibrantReader extends OneLineReader {

	public VibrantReader(File f) {
		super(f, false);
	}
	
	public Long getValue() {
		Long value = super.getValue();
		if (value != null) {
			value = Math.round(value / 1.8367);
		}
		
		return value;
	}
	
}
