package com.manor.currentwidget.library.analyze;

import android.os.Parcel;
import android.os.Parcelable;

public class ProcessInfo implements Comparable<ProcessInfo>, Parcelable  {

	private int numberOfTimes;
	private long electricCurrentSum;
	public String processName;
	
	public ProcessInfo(String name, long electricCurrent) {
		numberOfTimes = 1;
		electricCurrentSum = electricCurrent;
		processName = name;
	}
	
	public ProcessInfo(Parcel in) {
		numberOfTimes = in.readInt();
		electricCurrentSum = in.readLong();
		processName = in.readString();
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

	public void writeToParcel(Parcel out, int arg1) {
		out.writeInt(numberOfTimes);
		out.writeLong(electricCurrentSum);
		out.writeString(processName);
	}
	
	
	
	  public static final Parcelable.Creator<ProcessInfo> CREATOR
      		= new Parcelable.Creator<ProcessInfo>() {
		  		public ProcessInfo createFromParcel(Parcel in) {
		  			return new ProcessInfo(in);
		  		}
		  		
		  		public ProcessInfo[] newArray(int size) {
	  				return new ProcessInfo[size];
  				}
	  };
}
