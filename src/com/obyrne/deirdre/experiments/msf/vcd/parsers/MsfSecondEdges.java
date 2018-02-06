package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.TreeSet;

import com.obyrne.deirdre.experiments.msf.vcd.Main;
import com.obyrne.deirdre.experiments.msf.vcd.SignalSet;

public class MsfSecondEdges implements VCDFileParser {

	private static final String OUTPUT_FILE = Main.OUTPUT_DIRECTORY + "msf-seconds.csv";

	private long mySecondStart = 0L;
	private Hashtable<Integer,Integer[]> myAns = new Hashtable<Integer,Integer[]>();
	private boolean[] myMsfSecondEdgeFound = new boolean[SignalSet.NUM_MSF_RECEIVERS];
	private Writer myWriter;
	
	public MsfSecondEdges() throws IOException {
		myWriter = new FileWriter(new File(OUTPUT_FILE), false);
		myWriter.write("\"Microseconds since GPS second\"");
		for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
			myWriter.write(",\"Receiver ");;
			myWriter.write(Integer.toString(i+1));
			myWriter.write('"');
		}
		myWriter.write('\n');
	}

	public void newGPSSecond(long us) {
		mySecondStart = us;
		for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
			myMsfSecondEdgeFound[i] = false;
		}
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
	}

}
