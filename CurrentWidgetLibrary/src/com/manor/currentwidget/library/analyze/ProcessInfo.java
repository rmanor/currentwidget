package com.manor.currentwidget.library.analyze;


public class ProcessInfo implements Comparable<ProcessInfo>, ITwoValuesResult  {

	private int numberOfTimes;
	private long electricCurrentSum;
	public String processName;
	
	public ProcessInfo(String name, long electricCurrent) {
		numberOfTimes = 1;
		electricCurrentSum = electricCurrent;
		processName = name;
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
		return processName;
	}

	public String getValue2() {
		return Float.toString(getMean());
	}

}
