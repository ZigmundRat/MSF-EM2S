package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.obyrne.deirdre.experiments.msf.vcd.Main;
import com.obyrne.deirdre.experiments.msf.vcd.SignalSet;

/**
 * A VCD file parser which generates a histogram of where in the UTC second the MSF second edge occurs.
 */
public class MsfSecondEdgesVCDFileParser implements VCDFileParser {

	// The seconds graph contains a "corrected seconds edge position" graph offset by 75,000 microseconds
	private static final String OUTPUT_FILE = Main.OUTPUT_DIRECTORY + "msf-seconds.csv";
	private static final String CORRELATION_FILE = Main.OUTPUT_DIRECTORY + "msf-correlation.csv";

	private long mySecondStart = 0L;
	// Key - edge location (microseconds), value - number of occurrences for each receiver
	private Hashtable<Integer,Integer[]> myAns = new Hashtable<Integer,Integer[]>();
	private boolean[] myMsfSecondEdgeFound = new boolean[SignalSet.NUM_MSF_RECEIVERS];
	private long[] myPreviousMsfSecond = new long[SignalSet.NUM_MSF_RECEIVERS];
	// Key - Length of second, value - map whose key is receiver number and whose value is a list of second offsets
	private Map<Integer,Map<Integer,List<Integer>>> myCorrelationGraph = new Hashtable<Integer,Map<Integer,List<Integer>>>();
	private Writer myWriter;
	private Writer myCorrelationWriter;
	
	public MsfSecondEdgesVCDFileParser() throws IOException {
		myWriter = new FileWriter(new File(OUTPUT_FILE), false);
		myWriter.write("\"Microseconds since GPS second\"");
		for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
			myWriter.write(",\"Receiver ");;
			myWriter.write(Integer.toString(i+1));
			myWriter.write('"');
		}
		myWriter.write('\n');
		myCorrelationWriter = new FileWriter(new File(CORRELATION_FILE), false);
		myCorrelationWriter.write("\"Previous second length\"");
		for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
			myCorrelationWriter.write(",\"Receiver ");;
			myCorrelationWriter.write(Integer.toString(i+1));
			myCorrelationWriter.write('"');
		}
		myCorrelationWriter.write('\n');
	}

	public void newGPSSecond(long us) {
		mySecondStart = us;
		for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
			myMsfSecondEdgeFound[i] = false;
		}
	}

	private void addToCorrelationGraph(int receiver, int secondDuration, int secondLateness) {
		Map<Integer,List<Integer>> v;
		List<Integer> a;

		if ((secondLateness >= 35000) || (secondLateness < 5000)) return;
		v = myCorrelationGraph.get(secondDuration);
		if (v == null) {
			a = new LinkedList<Integer>();
			v = new Hashtable<Integer,List<Integer>>();
			v.put(receiver, a);
			myCorrelationGraph.put(secondDuration, v);
		} else {
			a = v.get(receiver);
			if (a == null) {
				a = new LinkedList<Integer>();
				v.put(receiver, a);
			}
		}
		a.add(secondLateness);
	}

	private void addToAns(int receiver,int duration) {
		Integer[] count = myAns.get(duration);
		if (count == null) {
			count = new Integer[SignalSet.NUM_MSF_RECEIVERS];
			for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
				if (receiver == i) {
					count[i] = 1;
				} else {
					count[i] = 0;
				}
			}
			myAns.put(duration, count);
		} else {
			count[receiver]++;
			myAns.put(duration, count);
		}
	}

	public void newMSFSignal(long us, int receiver, boolean value) {
		int duration;
		if (myMsfSecondEdgeFound[receiver]) return;
		if (value) return;
		myMsfSecondEdgeFound[receiver] = true;
		duration = (int)(us - mySecondStart);
		if (duration < 40000) addToAns(receiver,duration);
		if (myPreviousMsfSecond[receiver] != 0) {
			int secondLength = (int)(us - myPreviousMsfSecond[receiver]);
			if ((secondLength < 1020000) && (secondLength >= 980000)) {
				addToCorrelationGraph(receiver, secondLength, duration);
				if (duration < 40000) {
					duration -= 0.482899 * secondLength - 460620.0;
					duration += 75000;
					addToAns(receiver,duration);
				}
			}
		}
		myPreviousMsfSecond[receiver] = us;
	}

	public void writeResults() throws IOException {
		TreeSet<Integer> keys = new TreeSet<Integer>(myAns.keySet());
		for (int key : keys) {
			Integer[] vals = myAns.get(key);
			myWriter.write(Integer.toString(key));
			for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
				myWriter.write(',');
				myWriter.write(Integer.toString(vals[i]));
			}
			myWriter.write('\n');
		}
		myWriter.close();
		keys = new TreeSet<Integer>(myCorrelationGraph.keySet());
		for (int secondLength : keys) {
			Map<Integer,List<Integer>> v = myCorrelationGraph.get(secondLength);
			for (int receiver : v.keySet()) {
				int[] row = new int[SignalSet.NUM_MSF_RECEIVERS];
				for (int value : v.get(receiver)) {
					row[receiver] = value;
					myCorrelationWriter.write(Integer.toString(secondLength));
					for (int j = 0 ; j < SignalSet.NUM_MSF_RECEIVERS ; j++) {
						myCorrelationWriter.write('\t');
						if (row[j] != 0) myCorrelationWriter.write(Integer.toString(row[j]));
					}
					myCorrelationWriter.write('\n');
				}
			}
		}
		myCorrelationWriter.close();
	}

}
