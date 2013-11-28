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

package com.manor.currentwidget.library.analyze;

import java.util.Arrays;
import java.util.HashMap;

public class TopProcessesLineProcessor implements ILogLineProcessor {

	private long value = 0;
	private HashMap<String, ProcessInfo> processesData = new HashMap<String, ProcessInfo>();
	
	public void process(String line) {		
		// 0 is date/time , 1 is value, 2 battery level, 3 running processes separated by semicolons, 4 all the rest
		String[] tokens = line.split(",", 5);
		
		// if there is apps info
		if (tokens.length < 4) {
			return;	
		}
		
		try	{
			// remove mA at the end
			value = Long.parseLong(tokens[1].substring(0, tokens[1].length()-2));
		}
		catch (NumberFormatException nfe) {
			value = 0;
		}
			
		// Only if discharging.
		if (value  < 0) {
			value = Math.abs(value);
			String[] processes = tokens[3].split(";");
			for (String process : processes) {
				process = process.trim();
				if (!processesData.containsKey(process)) {
					processesData.put(process, new ProcessInfo(process, value));
				}
				else {
					processesData.get(process).addElectricCurrent(value);
				}
			}
		}
	}
	
	public Object[] getResult() {		
		// copy to array and merge sort
		ProcessInfo[] result = processesData.values().toArray(new ProcessInfo[processesData.size()]);
		Arrays.sort(result);		
		return result;
	}

}
