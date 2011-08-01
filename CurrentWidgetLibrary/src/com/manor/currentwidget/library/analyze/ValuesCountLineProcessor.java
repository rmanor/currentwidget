package com.manor.currentwidget.library.analyze;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

import android.R.array;

public class ValuesCountLineProcessor implements ILogLineProcessor {

	private int value = 0;
	
	private HashMap<Integer, Integer> _histogram = new HashMap<Integer, Integer>();
	
	public void process(String line) {
		
		String[] tokens = line.split(",", 3);
		
		if (tokens.length < 2)
			return;
		
		try
		{
			// remove mA at the end
			value = Integer.parseInt(tokens[1].substring(0, tokens[1].length()-2));
		}
		catch (NumberFormatException nfe)
		{
			value = 0;
		}


		if (value == 0)
			return;
		
		if (_histogram.containsKey(value)) {
			_histogram.put(value, 
					_histogram.get(value) + 1);
		}
		else {
			_histogram.put(value, 1);
		}
		
	}

	public Object[] getResult() {
		
		TwoIntValuesResult[] result = new TwoIntValuesResult[_histogram.size()];
		
		int i = -1;
		for (Integer k : _histogram.keySet()) {
			result[++i] = new TwoIntValuesResult(k, _histogram.get(k));
		}
		
		Arrays.sort(result);
		
		return result;
	}

}
