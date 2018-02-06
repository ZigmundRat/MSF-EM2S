package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.TreeSet;

import com.obyrne.deirdre.experiments.msf.vcd.Main;

/**
 * A VCD file parser which creates a histogram of the number of signal analyser microseconds occur between each
 * GPS second.
 */
public class GPSSecondIntervalsVCDFileParser implements VCDFileParser {
	
	private static final String OUTPUT_FILE = Main.OUTPUT_DIRECTORY + "gps-intervals.csv";
	
	private Writer myWriter;
	// Key: number of microseconds, Value: number of occurrences
	private Hashtable<Integer, Integer> myAns = new Hashtable<Integer,Integer>();
	private Long myLastSecond = null;

	public GPSSecondIntervalsVCDFileParser() throws IOException {
		myWriter = new FileWriter(new File(OUTPUT_FILE), false);
		myWriter.write("\"Microseconds since last GPS second\",\"Number of occurrances\"\n");
	}

	public void newGPSSecond(long us) {
		if (myLastSecond == null) {
			myLastSecond = us;
		} else {
			int interval = (int)(us - myLastSecond);
			Integer current = myAns.get(interval);
			myLastSecond = us;
			if (current == null) {
				myAns.put(interval, 1);
			} else {
				current++;
				myAns.put(interval, current);
			}
		}
	}


	public void newMSFSignal(long us, int receiver, boolean value) {
	}


	public void writeResults() throws IOException {
		TreeSet<Integer> keys = new TreeSet<Integer>(myAns.keySet());
		for (int key : keys) {
			myWriter.write(Integer.toString(key));
			myWriter.write(',');
			myWriter.write(Integer.toString(myAns.get(key)));
			myWriter.write('\n');
		}
		myWriter.close();
	}

}
