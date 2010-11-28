package com.manor.currentwidget.library;

import java.io.File;

import android.os.Build;

public class CurrentReaderFactory {
	static public ICurrentReader getCurrentReader() {
		
		File f = null;
		
		// htc desire hd / desire z?
		if (Build.MODEL.toLowerCase().contains("desire hd") ||
				Build.MODEL.toLowerCase().contains("desire z")) {
			f = new File("/sys/class/power_supply/battery/batt_current");
			if (f.exists())
				return new OneLineReader(f, false);
		}
		
		// sony ericsson xperia x1
		f = new File("/sys/devices/platform/i2c-adapter/i2c-0/0-0036/power_supply/ds2746-battery/current_now");
		if (f.exists())
			return new OneLineReader(f, false);
		
		// xdandroid
		/*if (Build.MODEL.equalsIgnoreCase("MSM")) {*/
			f = new File("/sys/devices/platform/i2c-adapter/i2c-0/0-0036/power_supply/battery/current_now");
			if (f.exists())
				return new OneLineReader(f, false);
		/*}*/
	
		// droid eris
		f = new File("/sys/class/power_supply/battery/smem_text");		
		if (f.exists())
			return new SMemTextReader();
		
		// some htc devices
		f = new File("/sys/class/power_supply/battery/batt_current");
		if (f.exists())
			return new OneLineReader(f, false);
		
		// nexus one
		f = new File("/sys/class/power_supply/battery/current_now");
		if (f.exists())
			return new OneLineReader(f, true);
		
		// samsung galaxy vibrant		
		f = new File("/sys/class/power_supply/battery/batt_chg_current");
		if (f.exists())
			return new OneLineReader(f, false); 
		
		return null;
	}
}
