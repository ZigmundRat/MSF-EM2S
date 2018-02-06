package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeSet;

import com.obyrne.deirdre.experiments.msf.vcd.Main;
import com.obyrne.deirdre.experiments.msf.vcd.SignalSet;

/**
 * An experimental VCD file parser which gives a histogram of the lengths of MSF pulses. It was discovered to give
 * no useful information.
 */
public class MsfPulseWidthsVCDFileParser implements VCDFileParser {

	private static final int MAX_DURATION = 150000;
	private static final String OUTPUT_FILE = Main.OUTPUT_DIRECTORY + "msf-pulses.csv";
	
	private long[] myLastPulse = new long[SignalSet.NUM_MSF_RECEIVERS];
	private Hashtable<Integer, Map<Integer,Integer>> myAns = new Hashtable<Integer,Map<Integer,Integer>>();
	private Writer myWriter;
	
	public MsfPulseWidthsVCDFileParser() throws IOException {
		myWriter = new FileWriter(new File(OUTPUT_FILE), false);
		myWriter.write("\"Microseconds\"");
		for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
			myWriter.write(",\"Receiver ");;
			myWriter.write(Integer.toString(i+1));
			myWriter.write('"');
		}
		myWriter.write('\n');
	}
	
	public void newGPSSecond(long us) {
	}

	private void addToAns(int receiver, int duration) {
		Map<Integer,Integer> vals;

		if (duration >= MAX_DURATION) return;
		vals = myAns.get(duration);
		if (vals == null) {
			vals = new Hashtable<Integer,Integer>();
			myAns.put(duration, vals);
		}
		if (vals.get(receiver) == null) {
			vals.put(receiver, 1);
		} else {
			vals.put(receiver, vals.get(receiver) + 1);
		}
	}

	public void newMSFSignal(long us, int receiver, boolean value) {
		if (myLastPulse[receiver] != 0) {
			addToAns(receiver,(int)(us - myLastPulse[receiver]));
		}
		myLastPulse[receiver] = us;
	}

	public void writeResults() throws IOException {
		TreeSet<Integer> keys = new TreeSet<Integer>(myAns.keySet());
		for (int key : keys) {
			Map<Integer,Integer> vals = myAns.get(key);
			myWriter.write(Integer.toString(key));
			for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
				Integer ans = vals.get(i);
				if (ans == null) ans = 0;
				myWriter.write(',');
				myWriter.write(ans.toString());
			}
			myWriter.write('\n');
		}
		myWriter.close();
	}

}
