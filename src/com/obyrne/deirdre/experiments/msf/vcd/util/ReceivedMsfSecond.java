package com.obyrne.deirdre.experiments.msf.vcd.util;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

// TODO I should check the received second against what's expected
//
// A should be detected by looking at the signal between 160ms and 180ms
// B should be detected by looking at between 260ms and 280ms
// M should be detected between 400ms and 420ms
//
// ... OR - see below!!!
//

public class ReceivedMsfSecond {

	private long mySecondStart;
	private int myExpectedSignal;
	private LinkedList<Integer> myRisingEdges = new LinkedList<Integer>();
	private LinkedList<Integer> myFallingEdges = new LinkedList<Integer>();
	
//	private static final int MAX_ALLOWED_EDGES = 16;
	
//	private double my20to80Millis = 0.0;
//	private double my160to180Millis = 0.0;
//	private double my260to280Millis = 0.0;
//	private double my380to460Millis = 0.0;
//	private double my560to980Millis = 0.0;
	
//	private static final double SIGNAL_INTEGRITY_THRESHOLD = 0.75;
	
	// Always called at a falling edge
	public ReceivedMsfSecond(long start, int expectedSignal) {
		mySecondStart = start;
		myExpectedSignal = expectedSignal;
	}

	public void processSignal(long us, boolean value) {
		int interval = (int)(us - mySecondStart);
		if (interval >= 1080000) return;
		// TODO check that the signals are in the expected order
		if (value) {
			myRisingEdges.add(interval);
		} else {
			myFallingEdges.add(interval);
		}
	}
	
	public Collection<Integer> getRisingEdges() {
		return myRisingEdges;
	}
	
	public Collection<Integer> getFallingEdges() {
		return myFallingEdges;
	}

	public int getSignal() {
		return myExpectedSignal;
	}
	
	public long getSecondStart() {
		return mySecondStart;
	}

	/*
	private void addToMap(Map<Integer, Map<Integer,Double>> ans, int ms, double val) {
		Map<Integer,Double> v = ans.get(ms);
		if (v == null) {
			v = new Hashtable<Integer,Double>();
			for (int i = 0 ; i < 5 ; i++) {
				v.put(i, 0.0);
			}
			ans.put(ms, v);
		}
		v.put(myExpectedSignal, v.get(myExpectedSignal) + val);
		*/
		/*
		if (ms < 20) return;
		else if (ms < 80) my20to80Millis += val;
		else if (ms < 160) return;
		else if (ms < 180) my160to180Millis += val;
		else if (ms < 260) return;
		else if (ms < 280) my260to280Millis += val;
		else if (ms < 380) return;
		else if (ms < 460) my380to460Millis += val;
		else if (ms < 560) return;
		else if (ms < 980) my560to980Millis += val;
		*/
//	}

	/*
	public boolean getSignalIntegrity() {
		if (my20to80Millis > 60.0 * (1.0 - SIGNAL_INTEGRITY_THRESHOLD)) return false;
		if (my560to980Millis < 420.0 * SIGNAL_INTEGRITY_THRESHOLD) return false;
		if ((myExpectedSignal == MsfSignalGenerator.A0B0) || (myExpectedSignal == MsfSignalGenerator.A0B1)) {
			if (my160to180Millis < 20.0 * SIGNAL_INTEGRITY_THRESHOLD) return false;
		} else {
			if (my160to180Millis > 20.0 * (1.0 - SIGNAL_INTEGRITY_THRESHOLD)) return false;
		}
		if ((myExpectedSignal == MsfSignalGenerator.A0B0) || (myExpectedSignal == MsfSignalGenerator.A1B0)) {
			if (my260to280Millis < 20.0 * SIGNAL_INTEGRITY_THRESHOLD) return false;
		} else {
			if (my260to280Millis > 20.0 * (1.0 - SIGNAL_INTEGRITY_THRESHOLD)) return false;
		}
		if (myExpectedSignal == MsfSignalGenerator.MIN) {
			if (my380to460Millis > 80.0 * (1.0 - SIGNAL_INTEGRITY_THRESHOLD)) return false;
		} else {
			if (my380to460Millis < 80.0 * SIGNAL_INTEGRITY_THRESHOLD) return false;
		}
		return true;
	}
	*/
	
	/*
	private void addToMap(Map<Integer, Map<Integer,Double>> ans, int r, int f) {
		int msr = r / 1000;
		int msf = f / 1000;
		if (msr == msf) {
			addToMap(ans, msr, 0.001 * (f - r));
		} else {
			addToMap(ans, msr++, 0.001 * (1000 - (r % 1000)));
			while (msr != msf) {
				addToMap(ans, msr++, 1.0);
			}
			addToMap(ans, msf, 0.001 * (msf % 1000));
		}
	}
	*/
	
	private void processSignalIsHigh(MsfMillisecondSignalLevelProcessor processor, int receiver, int r, int f) {
		int msr = r / 1000;
		int msf = f / 1000;
		if (msr == msf) {
			processor.msfMillisecondSignalLevelValue(receiver, myExpectedSignal, msr, 0.001*(f-r));
		} else {
			processor.msfMillisecondSignalLevelValue(receiver, myExpectedSignal, msr++, 0.001 * (1000 - (r % 1000)));
			while (msr != msf) {
				processor.msfMillisecondSignalLevelValue(receiver, myExpectedSignal, msr++, 1.0);
			}
			processor.msfMillisecondSignalLevelValue(receiver, myExpectedSignal, msf, 0.001 * (msf % 1000));
		}
	}
	
	public void processIntoMillisecondValues(int receiver, MsfMillisecondSignalLevelProcessor processor) {
		Iterator<Integer> rising;
		Iterator<Integer> falling;
		
		rising = myRisingEdges.iterator();
		falling = myFallingEdges.iterator();
		while (rising.hasNext()) {
			int r = rising.next();
			int f;
			if (falling.hasNext()) {
				f = falling.next();
			} else {
				f = 1079999;
			}
			processSignalIsHigh(processor, receiver, r, f);
		}	
	}

	/*
	// First key - millis
	// Second key - signal id
	// Value - total signal
	public void addTo(Map<Integer, Map<Integer,Double>> ans) {
		Iterator<Integer> rising;
		Iterator<Integer> falling;
		
//		if (myRisingEdges.size() + myFallingEdges.size() > MAX_ALLOWED_EDGES) return;
		rising = myRisingEdges.iterator();
		falling = myFallingEdges.iterator();
		while (rising.hasNext()) {
			int r = rising.next();
			int f;
			if (falling.hasNext()) {
				f = falling.next();
			} else {
				f = 1079999;
			}
			addToMap(ans, r, f);
		}
	}
	*/
	
	private void addEdgesTo(Map<Integer, Map<Integer,int[]>> ans, Iterator<Integer> edges, int fallingRising) {
		while (edges.hasNext()) {
			int edge = (edges.next())/1000;
			Map<Integer,int[]> vals = ans.get(edge);
			int[] result;
			if (vals == null) {
				vals = new Hashtable<Integer,int[]>();
				ans.put(edge, vals);
			}
			result = vals.get(myExpectedSignal);
			if (result == null) {
				result = new int[2];
			}
			result[fallingRising]++;
			vals.put(myExpectedSignal, result);
		}
	}
	
	// First key - us
	// Second key - signal id
	// Value - [falling,rising] counts
	public void addEdges(Map<Integer, Map<Integer,int[]>> ans) {
//		if (myRisingEdges.size() + myFallingEdges.size() > MAX_ALLOWED_EDGES) return;
		addEdgesTo(ans, myFallingEdges.iterator(), 0);
		addEdgesTo(ans, myRisingEdges.iterator(), 1);
	}
}
