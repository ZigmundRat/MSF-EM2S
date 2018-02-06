package com.obyrne.deirdre.experiments.msf.vcd.util;

/**
 * This class represents a millisecond-long chunk of the E2MS/MSF signal
 */
public class MsfSignalMillisecondSignalSlice {

	private int myLow, myHigh;
	private double myAns = 0.0;
	
	/**
	 * Constructor
	 * @param low the number of milliseconds (referenced from the start of the MSF second) to the start of this chunk
	 * @param high the number of milliseconds to the end of the chunk
	 */
	public MsfSignalMillisecondSignalSlice(int low, int high) {
		myLow = low;
		myHigh = high;
	}
	
	/**
	 * Called when we are processing a new second
	 */
	public void resetCount() {
		myAns = 0.0;
	}

	/**
	 * Check to see if a millisecond chunk is in this slice, and add it to the result if it is
	 * @param ms the millisecond
	 * @param value the average value during that millisecond
	 */
	public void addSignalSlice(int ms, double value) {
		if ((ms >= myLow) && (ms < myHigh)) {
			myAns += value;
		}
	}
	
	/**
	 * Returns the name of this slice
	 */
	public String getName() {
		return Integer.toString(myLow)+"ms - "+Integer.toString(myHigh)+"ms";
	}
	
	/**
	 * Returns the average value of the signal during this slice
	 * @return 0.0 if no carrier was detected during this slice, to 1.0 if no absence of carrier was detected
	 */
	public double getTotal() {
		return myAns / (myHigh - myLow);
	}

	/**
	 * Returns the threshold for the decoding algorithm
	 */
	public double getThreshold() {
		if (myLow == 200) {
			return 0.25;
		}
		return 0.15;
	}
	
	/**
	 * Returns 1 if the decoding algorithm decides that this slice shows carrier present, zero otherwise
	 */
	public int getDecodedLevel() {
		if (getTotal() <= getThreshold()) {
			return 0;
		}
		return 1;
	}
}
