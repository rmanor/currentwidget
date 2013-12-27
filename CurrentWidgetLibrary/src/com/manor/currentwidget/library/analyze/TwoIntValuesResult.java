/*
 *  Copyright (c) 2010-2014 Ran Manor
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
