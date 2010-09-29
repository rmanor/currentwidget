package com.manor.currentwidget.library;

import java.io.BufferedReader;
import java.io.FileReader;

public class SMemTextReader implements ICurrentReader {

	public Long getValue() {

		boolean success = false;
		String text = null;
		
		try {
			
			// @@@ debug StringReader fr = new StringReader("batt_id: 1\r\nbatt_vol: 3840\r\nbatt_vol_last: 0\r\nbatt_temp: 1072\r\nbatt_current: 1\r\nbatt_current_last: 0\r\nbatt_discharge_current: 112\r\nVREF_2: 0\r\nVREF: 1243\r\nADC4096_VREF: 4073\r\nRtemp: 70\r\nTemp: 324\r\nTemp_last: 0\r\npd_M: 20\r\nMBAT_pd: 3860\r\nI_MBAT: -114\r\npd_temp: 0\r\npercent_last: 57\r\npercent_update: 58\r\ndis_percent: 64\r\nvbus: 0\r\nusbid: 1\r\ncharging_source: 0\r\nMBAT_IN: 1\r\nfull_bat: 1300000\r\neval_current: 115\r\neval_current_last: 0\r\ncharging_enabled: 0\r\ntimeout: 30\r\nfullcharge: 0\r\nlevel: 58\r\ndelta: 1\r\nchg_time: 0\r\nlevel_change: 0\r\nsleep_timer_count: 11\r\nOT_led_on: 0\r\noverloading_charge: 0\r\na2m_cable_type: 0\r\nover_vchg: 0\r\n");
			FileReader fr = new FileReader("/sys/class/power_supply/battery/smem_text");
			BufferedReader br = new BufferedReader(fr);	
			
			String line = br.readLine();	
			
			while (line != null) 
			{
				if (line.contains("I_MBAT"))
				{
					text = line.substring(line.indexOf("I_MBAT: ") + 8);
					success = true;
					break;
				}
				line = br.readLine();
			}
			
			br.close();
			fr.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		Long value = null;
		
		if (success) {
			
			try
			{
				value = Long.parseLong(text);
			}
			catch (NumberFormatException nfe)
			{
				value = null;
			}

		}	
		
		return value;
	}

}
