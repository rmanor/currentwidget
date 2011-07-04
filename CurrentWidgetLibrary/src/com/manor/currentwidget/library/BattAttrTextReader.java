package com.manor.currentwidget.library;

import java.io.BufferedReader;
import java.io.FileReader;

import android.util.Log;

public class BattAttrTextReader {

	public static Long getValue() {

		String text = null;
		Long value = null;
		
		try {
			
			// @@@ debug 
			//StringReader fr = new StringReader("vref: 1248\r\nbatt_id: 3\r\nbatt_vol: 4068\r\nbatt_current: 0\r\nbatt_discharge_current: 123\r\nbatt_temperature: 329\r\nbatt_temp_protection:normal\r\nPd_M:0\r\nI_MBAT:313\r\npercent_last(RP): 94\r\npercent_update: 71\r\nlevel: 71\r\nfirst_level: 100\r\nfull_level:100\r\ncapacity:1580\r\ncharging_source: USB\r\ncharging_enabled: Slow\r\n");
			FileReader fr = new FileReader("/sys/class/power_supply/battery/batt_attr_text");
			BufferedReader br = new BufferedReader(fr);	
			
			String line = br.readLine();	
			
			while (line != null) 
			{
				if (line.contains("batt_current"))
				{
					text = line.substring(line.indexOf("batt_current: ") + 14);
					value = Long.parseLong(text);
					if (value != 0)				
						break;
				}
				
				if (line.contains("batt_discharge_current"))
				{
					text = line.substring(line.indexOf("batt_discharge_current: ") + 24);
					value = (-1)*Long.parseLong(text);									
					break;
				}
				
				
				line = br.readLine();
			}
			
			br.close();
			fr.close();
		}
		catch (Exception ex) {
			Log.e("CurrentWidget", ex.getMessage());
			ex.printStackTrace();
		}		
				
	
		return value;
	}

}
