package com.manor.currentwidget.library.analyze;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

import android.R.array;
import android.util.SparseIntArray;

public class ValuesCountLineProcessor implements ILogLineProcessor {

	private int value = 0;
	
	//private HashMap<Integer, Integer> _histogram = new HashMap<Integer, Integer>();
	private SparseIntArray _histogram = new SparseIntArray();
	
	public void process(String line) {
		
		String[] tokens = line.split(",", 3);
		
		if (tokens.length < 2)
			return;
		
		try
		{
			// remove mA at the end
			value = Integer.parseInt(tokens[1].substring(0, tokens[1].length()-2));
		}
		catch (Exception nfe) // not a number, weird string, etc.
		{
			value = 0;
		}


		if (value == 0)
			return;
		
		
		//if (_histogram.containsKey(value)) {
		if (_histogram.indexOfKey(value) >= 0) {
			_histogram.put(value, 
					_histogram.get(value) + 1);
		}
		else {
			_histogram.put(value, 1);
		}
		
	}

	public Object[] getResult() {
		
		TwoIntValuesResult[] result = new TwoIntValuesResult[_histogram.size()];
		
		//int i = -1;
		//_histogram.
		//for (Integer k : _histogram.keySet()) {
		for (int i=0;i<_histogram.size();++i) {
			result[i] = new TwoIntValuesResult(_histogram.keyAt(i),
					_histogram.valueAt(i));
		}
		
		Arrays.sort(result);
		
		return result;
	}

}
