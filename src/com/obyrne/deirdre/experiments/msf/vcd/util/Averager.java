package com.obyrne.deirdre.experiments.msf.vcd.util;

public class Averager {

	private int mySamples = 0;
	private double myTotal = 0;
	
	public Averager() {
	}
	
	public void add(double val) {
		myTotal += val;
		mySamples++;
	}
	
	public double getAverage() {
		return myTotal/mySamples;
	}

}
