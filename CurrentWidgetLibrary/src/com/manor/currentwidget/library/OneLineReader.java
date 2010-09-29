package com.manor.currentwidget.library;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class OneLineReader implements ICurrentReader {

	private File _f = null;
	private boolean _convertToMillis = false;
	
	public OneLineReader(File f, boolean convertToMillis) {
		_f = f;
		_convertToMillis = convertToMillis;
	}
	
	public Long getValue() {
		
		String text = null;
		
		try {
			
		
			FileInputStream fs = new FileInputStream(_f);
			
			DataInputStream ds = new DataInputStream(fs);
		
			text = ds.readLine();
			
			ds.close();		
			fs.close();	
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		Long value = null;
		
		if (text != null)
		{
			try
			{
				value = Long.parseLong(text);
			}
			catch (NumberFormatException nfe)
			{
				value = null;
			}
			
			if (_convertToMillis)
				value = value/1000; // convert to milliampere

		}
		
		return value;
	}

}
