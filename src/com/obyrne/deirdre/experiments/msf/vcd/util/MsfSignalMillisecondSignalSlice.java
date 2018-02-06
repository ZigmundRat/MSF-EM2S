package com.obyrne.deirdre.experiments.msf.vcd.util;

public class MsfSignalMillisecondSignalSlice {

	private int myLow, myHigh;
	private double myAns = 0.0;
	
	public MsfSignalMillisecondSignalSlice(int low, int high) {
		myLow = low;
		myHigh = high;
	}
	
	public void resetCount() {
		myAns = 0.0;
	}

	public void addSignalSlice(int ms, double value) {
		if ((ms >= myLow) && (ms < myHigh)) {
			myAns += value;
		}
	}
	
	public String getName() {
		return Integer.toString(myLow)+"ms - "+Integer.toString(myHigh)+"ms";
	}
	
	public double getTotal() {
		return myAns / (myHigh - myLow);
	}

	public double getThreshold() {
		if (myLow == 200) {
			return 0.25;
		}
		return 0.15;
	}
	
	public int getDecodedLevel() {
		if (getTotal() <= getThreshold()) {
			return 0;
		}
		return 1;
	}
}
