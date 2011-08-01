package com.manor.currentwidget.library.analyze;

public class TwoIntValuesResult implements Comparable<TwoIntValuesResult>, ITwoValuesResult {

	private int _value1;
	private int _value2;
	
	public TwoIntValuesResult(int value1, int value2) {
		
		_value1 = value1;
		_value2 = value2;
	}
	
	public String getValue1() {
		return Integer.toString(_value1);
	}

	public String getValue2() {
		return Integer.toString(_value2);
	}
	
	public int compareTo(TwoIntValuesResult another) {		
	
		// for descending sort..
		if (_value2 < another._value2)
			return 1;
		else {
			if (_value2 > another._value2)		
				return -1;
			else
				return 0;
		}
	}

}
