/*
 *  Copyright (c) 2010-2013 Ran Manor
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

import java.io.File;
import java.util.Locale;

import android.annotation.TargetApi;
import android.os.Build;

public class CurrentReaderFactory {
	
	static final String BUILD_MODEL = Build.MODEL.toLowerCase(Locale.ENGLISH);
	
	@TargetApi(4)
	static public Long getValue() {
		
		File f = null;

		if (CurrentReaderFactory.BUILD_MODEL.contains("sgh-i337") ||
				CurrentReaderFactory.BUILD_MODEL.contains("gt-i9505")) {
			f = new File("/sys/class/power_supply/battery/current_now");
			if (f.exists()) {
				return OneLineReader.getValue(f, false);
			}			
		}
		
		if (CurrentReaderFactory.BUILD_MODEL.contains("cynus")) {
			f = new File("/sys/devices/platform/mt6329-battery/FG_Battery_CurrentConsumption");
			if (f.exists()) {
				return OneLineReader.getValue(f, false);
			}
		}
		// Zopo Zp900
		if (CurrentReaderFactory.BUILD_MODEL.contains("zp900") ||
				CurrentReaderFactory.BUILD_MODEL.contains("jy-g3")) {	
			f = new File("/sys/class/power_supply/battery/BatteryAverageCurrent");
			if (f.exists()) {
				return OneLineReader.getValue(f, false);
			}
		}
		
		// Samsung Galaxy Tab 2
		if (CurrentReaderFactory.BUILD_MODEL.contains("gt-p31") ||
			CurrentReaderFactory.BUILD_MODEL.contains("gt-p51")) {
			f = new File("/sys/class/power_supply/battery/current_avg");
			if (f.exists()) {
				return OneLineReader.getValue(f, false);
			}	
		}
			
		// HTC One X
		if (CurrentReaderFactory.BUILD_MODEL.contains("htc one x")) {
			f = new File("/sys/class/power_supply/battery/batt_attr_text");
			if (f.exists()) {
				Long value = BattAttrTextReader.getValue(f, "I_MBAT", "I_MBAT");
				if (value != null)
					return value;
			}			
		}
		
		// wildfire S
		if (CurrentReaderFactory.BUILD_MODEL.contains("wildfire s")) {
			f = new File("/sys/class/power_supply/battery/smem_text");
			if (f.exists()) {
				Long value = BattAttrTextReader.getValue(f, "eval_current", "batt_current");
				if (value != null)
					return value;
			}
		}
		
		// trimuph with cm7, lg ls670, galaxy s3, galaxy note 2
		if (CurrentReaderFactory.BUILD_MODEL.contains("triumph") ||
				CurrentReaderFactory.BUILD_MODEL.contains("ls670") ||
				CurrentReaderFactory.BUILD_MODEL.contains("gt-i9300") ||
				CurrentReaderFactory.BUILD_MODEL.contains("gt-n7100") ||
				CurrentReaderFactory.BUILD_MODEL.contains("sgh-i317")) {
			f = new File("/sys/class/power_supply/battery/current_now");
			if (f.exists()) {
				return OneLineReader.getValue(f, false);
			}
		}
		
		// htc desire hd / desire z / inspire?
		// htc evo view tablet
		if (CurrentReaderFactory.BUILD_MODEL.contains("desire hd") ||
				CurrentReaderFactory.BUILD_MODEL.contains("desire z") ||
				CurrentReaderFactory.BUILD_MODEL.contains("inspire") ||
				CurrentReaderFactory.BUILD_MODEL.contains("pg41200"))  {			
			f = new File("/sys/class/power_supply/battery/batt_current");
			if (f.exists()) {
				return OneLineReader.getValue(f, false);
			}
		}

		// nexus one cyangoenmod
		f = new File("/sys/devices/platform/ds2784-battery/getcurrent");
		if (f.exists()) {
			return OneLineReader.getValue(f, true);
		}

		// sony ericsson xperia x1
		f = new File("/sys/devices/platform/i2c-adapter/i2c-0/0-0036/power_supply/ds2746-battery/current_now");
		if (f.exists()) {
			return OneLineReader.getValue(f, false);
		}
		
		// xdandroid
		/*if (Build.MODEL.equalsIgnoreCase("MSM")) {*/
			f = new File("/sys/devices/platform/i2c-adapter/i2c-0/0-0036/power_supply/battery/current_now");
			if (f.exists()) {
				return OneLineReader.getValue(f, false);
			}
		/*}*/
	
		// droid eris
		f = new File("/sys/class/power_supply/battery/smem_text");		
		if (f.exists()) {
			Long value = SMemTextReader.getValue();
			if (value != null)
				return value;
		}
		
		// htc sensation / evo 3d
		f = new File("/sys/class/power_supply/battery/batt_attr_text");
		if (f.exists())
		{
			Long value = BattAttrTextReader.getValue(f, "batt_discharge_current", "batt_current");
			if (value != null)
				return value;
		}
		
		// some htc devices
		f = new File("/sys/class/power_supply/battery/batt_current");
		if (f.exists())
			return OneLineReader.getValue(f, false);
		
		// nexus one
		f = new File("/sys/class/power_supply/battery/current_now");
		if (f.exists())
			return OneLineReader.getValue(f, true);
		
		// samsung galaxy vibrant		
		f = new File("/sys/class/power_supply/battery/batt_chg_current");
		if (f.exists())
			return OneLineReader.getValue(f, false);
		
		// sony ericsson x10
		f = new File("/sys/class/power_supply/battery/charger_current");
		if (f.exists())
			return OneLineReader.getValue(f, false);
		
		// Nook Color
		f = new File("/sys/class/power_supply/max17042-0/current_now");
		if (f.exists())
			return OneLineReader.getValue(f, false);
		
		// Xperia Arc
		f = new File("/sys/class/power_supply/bq27520/current_now");
		if (f.exists())
			return OneLineReader.getValue(f, true);
		
		// Motorola Atrix
		f = new File("/sys/devices/platform/cpcap_battery/power_supply/usb/current_now");
		if (f.exists()) 
			return OneLineReader.getValue(f, false);
		
		// Acer Iconia Tab A500
		f = new File("/sys/EcControl/BatCurrent");
		if (f.exists())
			return OneLineReader.getValue(f, false);
		
		// charge current only, Samsung Note
		f = new File("/sys/class/power_supply/battery/batt_current_now");
		if (f.exists())
			return OneLineReader.getValue(f, false);
		
		// galaxy note, galaxy s2
		f = new File("/sys/class/power_supply/battery/batt_current_adc");
		if (f.exists())
			return OneLineReader.getValue(f, false);
		
		// intel
		f = new File("/sys/class/power_supply/max170xx_battery/current_now");
		if (f.exists())
			return OneLineReader.getValue(f, true);
		
		// Sony Xperia U
		f = new File("/sys/class/power_supply/ab8500_fg/current_now");
		if (f.exists())
			return OneLineReader.getValue(f,  true);
		
		f = new File("/sys/class/power_supply/android-battery/current_now");
		if (f.exists()) {
			return OneLineReader.getValue(f,  false);
		}
		
		return null;
	}
}
