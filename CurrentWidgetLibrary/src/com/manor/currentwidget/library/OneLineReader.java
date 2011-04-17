/*
 *  Copyright (c) 2010-2011 Ran Manor
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

package com.manor.currentwidget.library;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.util.Log;

public class OneLineReader {

	/*private File _f = null;
	private boolean _convertToMillis = false;
	
	public OneLineReader(File f, boolean convertToMillis) {
		_f = f;
		_convertToMillis = convertToMillis;
	}*/
	
	public static Long getValue(File _f, boolean _convertToMillis) {
		
		String text = null;
		
		try {
			
		
			FileInputStream fs = new FileInputStream(_f);
			
			DataInputStream ds = new DataInputStream(fs);
		
			text = ds.readLine();
			
			ds.close();		
			fs.close();	
			
		}
		catch (Exception ex) {
			Log.e("CurrentWidget", ex.getMessage());
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
				Log.e("CurrentWidget", nfe.getMessage());
				value = null;
			}
			
			if (_convertToMillis && value != null)
				value = value/1000; // convert to milliampere

		}
		
		return value;
	}

}
