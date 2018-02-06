package com.obyrne.deirdre.experiments.msf.vcd.util;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * This class represents one MSF second received by one receiver
 */
public class ReceivedMsfSecond {

	private long mySecondStart;
	private int myExpectedSignal;
	private LinkedList<Integer> myRisingEdges = new LinkedList<Integer>();
	private LinkedList<Integer> myFallingEdges = new LinkedList<Integer>();
	
	/**
	 * Constructor - called at the first falling edge of the E2MS/MSF signal after the GPS PPS signal
	 * @param start the microsecond time value of the start of this MSF second
	 * @param expectedSignal the expected MSF timecode (A0B0, A0B1, ... )
	 */
	public ReceivedMsfSecond(long start, int expectedSignal) {
		mySecondStart = start;
		myExpectedSignal = expectedSignal;
	}

	/**
	 * Process a received E2MS/MSF signal
	 * @param us the microsecond at which the signal occurred
	 * @param value the new value of the signal (true means carrier present)
	 */
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
	
	/**
	 * Returns a collection of the times (in microseconds since the start of the MSF second) of the rising edges
	 */
	public Collection<Integer> getRisingEdges() {
		return myRisingEdges;
	}
	
	/**
	 * Returns a collection of the times (in microseconds since the start of the MSF second) of the falling edges
	 */
	public Collection<Integer> getFallingEdges() {
		return myFallingEdges;
	}

	/**
	 * Returns which of the 5 MSF signals this second should contain
	 */
	public int getSignal() {
		return myExpectedSignal;
	}
	
	/**
	 * Returns the signal analyser microsecond at which the MSF second apparently started
	 */
	public long getSecondStart() {
		return mySecondStart;
	}
	
	/**
	 * Helper method for processIntoMillisecondValues() - this method is called every time a high level
	 * (i.e. carrier present) of the E2MS/MSF signal is detected
	 * 
	 * @param processor the MsfMillisecondSignalLevelProcessor into which to send the processed data
	 * @param receiver the receiver number
	 * @param r the microsecond of the rising edge
	 * @param f the microseconds of the falling edge
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
	
	/**
	 * Process the data collected during this second into a MsfMillisecondSignalLevelProcessor
	 * @param receiver the receiver number
	 * @param processor the processor into which to send the processed data
	 */
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

	/**
	 * Helper method for addEdges(...)
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
	
	/**
	 * Method called by MsfEdgesVCDFileParser
	 * @param ans a map whose first key is microseconds since the MSF second edge, whose second key is the signal
	 *        id, and whose value is a 2-element integer array containing a count of the number of falling,rising edges
	 */
	public void addEdges(Map<Integer, Map<Integer,int[]>> ans) {
		addEdgesTo(ans, myFallingEdges.iterator(), 0);
		addEdgesTo(ans, myRisingEdges.iterator(), 1);
	}
}
