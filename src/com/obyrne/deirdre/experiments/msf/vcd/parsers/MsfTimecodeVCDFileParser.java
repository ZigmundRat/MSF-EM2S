package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import com.obyrne.deirdre.experiments.msf.vcd.Main;
import com.obyrne.deirdre.experiments.msf.vcd.SignalSet;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfMillisecondSignalLevelProcessor;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfTimecodeGenerator;
import com.obyrne.deirdre.experiments.msf.vcd.util.ReceivedMsfSecond;

/**
 * A VCD file parser which produces a graph, for each signal, of the average signal level throughout the second
 */
public class MsfTimecodeVCDFileParser extends AbstractMsfTimecodeVCDFileParser implements MsfMillisecondSignalLevelProcessor {
	
	private static final String OUTPUT_FILE = Main.OUTPUT_DIRECTORY + "msf-timecode.csv";
	private Map<Integer, Map<Integer,Double>> myAns = new Hashtable<Integer, Map<Integer,Double>>();
	private Writer myWriter;

	public MsfTimecodeVCDFileParser(MsfTimecodeGenerator timecode) throws IOException {
		super(timecode);
		myWriter = new FileWriter(new File(OUTPUT_FILE), false);
		myWriter.write("\"Millis since MSF second\",\"A0B0\",\"A0B1\",\"A1B0\",\"A1B1\",\"MIN\"\n");
	}
	
	protected void processReceivedSecond(ReceivedMsfSecond second, int receiver) {
		second.processIntoMillisecondValues(receiver, this);
	}

	private void writeDoubleVal(double v) throws IOException {
		myWriter.write(String.format(Locale.ENGLISH,"%09.7f",v));
	}

	protected void doWriteResults() throws IOException {
		TreeSet<Integer> millis = new TreeSet<Integer>();
		millis.addAll(myAns.keySet());
		for (int i : millis) {
			Map<Integer,Double> cols = myAns.get(i);
			myWriter.write(Integer.toString(i));
			for (int sig = 0 ; sig < 5 ; sig++) {
				myWriter.write(',');
				writeDoubleVal(cols.get(sig) / getNumTimecodes(sig) / SignalSet.NUM_MSF_RECEIVERS);
			}
			myWriter.write('\n');
		}
		myWriter.close();
	}

	public void msfMillisecondSignalLevelValue(int receiver, int expectedSignal, int ms, double val) {
		Map<Integer,Double> v = myAns.get(ms);
		if (v == null) {
			v = new Hashtable<Integer,Double>();
			for (int i = 0 ; i < 5 ; i++) {
				v.put(i, 0.0);
			}
			myAns.put(ms, v);
		}
		v.put(expectedSignal, v.get(expectedSignal) + val);
	}

}
