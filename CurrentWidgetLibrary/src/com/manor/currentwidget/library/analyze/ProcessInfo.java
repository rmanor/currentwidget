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

public class ProcessInfo implements Comparable<ProcessInfo>, ITwoValuesResult  {

	private int numberOfTimes;
	private long electricCurrentSum;
	public String processName;
	public String packageName;
	
	public ProcessInfo(String name, String packageName, long electricCurrent) {
		numberOfTimes = 1;
		electricCurrentSum = electricCurrent;
		processName = name;		
		this.packageName = packageName;
	}	

	@Override
	public String toString() {
		return processName;
	}
	
	public float getMean() {
		return electricCurrentSum / numberOfTimes;
	}
	
	public void addElectricCurrent(long c) {
		electricCurrentSum += c;
		++numberOfTimes;
	}

	public int compareTo(ProcessInfo another) {		
		float thisMean = getMean();
		float anotherMean = another.getMean();
		
		// for descending sort..
		if (thisMean < anotherMean)
			return 1;
		else {
			if (thisMean > anotherMean)		
				return -1;
			else
				return 0;
		}
	}

	public int describeContents() {
		return 0;
	}

	public String getValue1() {
		if (packageName != null && packageName.length() > 0) {
			return packageName;
		}
		return processName;
	}

	public String getValue2() {
		return Float.toString(getMean());
	}

}
