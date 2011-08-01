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
		if (tokens.length < 4)
			return;
	
		try
		{
			// remove mA at the end
			value = Long.parseLong(tokens[1].substring(0, tokens[1].length()-2));
		}
		catch (NumberFormatException nfe)
		{
			value = 0;
		}
			
		// drawing value
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
		ProcessInfo[] result = new ProcessInfo[processesData.size()];
		int i = -1;
		for (String k : processesData.keySet())
		{
			result[++i] = processesData.get(k);
		}
		
		//Object[] result = processesData.values().toArray();
		
		Arrays.sort(result);
		
		return result;

	}

}
