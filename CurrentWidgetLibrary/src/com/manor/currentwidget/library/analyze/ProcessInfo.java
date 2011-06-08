package com.manor.currentwidget.library.analyze;

public class ProcessInfo {

	private int numberOfTimes;
	private long electricCurrentSum;
	private String processName;
	
	public ProcessInfo(String name, long electricCurrent) {
		numberOfTimes = 1;
		electricCurrentSum = electricCurrent;
		processName = name;
	}
	
	public float getMean() {
		return electricCurrentSum / numberOfTimes;
	}
	
	public void addElectricCurrent(long c) {
		electricCurrentSum += c;
		numberOfTimes++;
	}
}
