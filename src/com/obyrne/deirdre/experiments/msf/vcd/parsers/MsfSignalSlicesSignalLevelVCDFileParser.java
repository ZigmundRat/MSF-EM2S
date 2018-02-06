package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import com.obyrne.deirdre.experiments.msf.vcd.Main;
import com.obyrne.deirdre.experiments.msf.vcd.SignalSet;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfMillisecondSignalLevelProcessor;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfTimecodeGenerator;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfSignalMillisecondSignalSlice;
import com.obyrne.deirdre.experiments.msf.vcd.util.ReceivedMsfSecond;

/**
 * A VCD file parser which generates a histogram of the occurrence of an average signal level for each slice of the
 * signal
 */
public class MsfSignalSlicesSignalLevelVCDFileParser extends AbstractMsfTimecodeVCDFileParser implements MsfMillisecondSignalLevelProcessor {

	private MsfSignalMillisecondSignalSlice[] mySlices;
	// Keys
	// Name of the interval (e.g. "100ms - 200ms")
	// Percent level
	// Expected signal
	// Value - total value
	private Map<String, Map<Integer, Map<Integer,Integer>>> myAns = new Hashtable<String, Map<Integer, Map<Integer,Integer>>>();
	private Writer myWriter;
	
	public MsfSignalSlicesSignalLevelVCDFileParser(String fileName, MsfSignalMillisecondSignalSlice[] slices, MsfTimecodeGenerator timecode) throws IOException {
		super(timecode);
		mySlices = slices;
		myWriter = new FileWriter(new File(Main.OUTPUT_DIRECTORY + fileName));
		for (MsfSignalMillisecondSignalSlice s : mySlices) {
			Map<Integer,Map<Integer,Integer>> v = new Hashtable<Integer,Map<Integer,Integer>>();
			myAns.put(s.getName(), v);
			for (int pc = 0 ; pc < 100 ; pc++) {
				Map<Integer,Integer> v2 = new Hashtable<Integer,Integer>();
				v.put(pc, v2);
				for (int sig = 0 ; sig < 5 ; sig++) {
					v2.put(sig, 0);
				}
			}
		}
	}
		
	protected void processReceivedSecond(ReceivedMsfSecond second, int receiver) {
		for (MsfSignalMillisecondSignalSlice s : mySlices) {
			s.resetCount();
		}
		second.processIntoMillisecondValues(receiver, this);
		for (MsfSignalMillisecondSignalSlice s : mySlices) {
			int percent = (int)(Math.floor(100.0 * s.getTotal()));
			Map<Integer,Map<Integer,Integer>> levels = myAns.get(s.getName());
			Map<Integer,Integer> v;
			if (percent > 101) throw new IllegalArgumentException(s.getName() + " - " + Integer.toString(percent));
			if (percent >= 100) percent = 99;
			v = levels.get(percent);
			v.put(second.getSignal(), v.get(second.getSignal()) + 1);
		}
	}

	public void msfMillisecondSignalLevelValue(int receiver, int expectedSignal, int ms, double val) {
		for (MsfSignalMillisecondSignalSlice s : mySlices) {
			s.addSignalSlice(ms, val);
		}
	}
	
	private void writeDoubleVal(double v) throws IOException {
		myWriter.write(String.format(Locale.ENGLISH,"%09.7f",v));
	}

	protected void doWriteResults() throws IOException {
		for (MsfSignalMillisecondSignalSlice s : mySlices) {
			Map<Integer,Map<Integer,Integer>> sliceValues = myAns.get(s.getName());
			myWriter.write('"');
			myWriter.write(s.getName());
			myWriter.write("\"\n");
			myWriter.write("\"Level\",\"A0B0\",\"A0B1\",\"A1B0\",\"A1B1\",\"MIN\"\n");
			for (int i = 0 ; i < 100 ; i++) {
				Map<Integer,Integer> signalValues = sliceValues.get(i);
				myWriter.write(Integer.toString(i));
				for (int sig = 0 ; sig < 5 ; sig++) {
					double val = signalValues.get(sig);
					val = val / SignalSet.NUM_MSF_RECEIVERS / getNumTimecodes(sig);
					myWriter.write(',');
					writeDoubleVal(val);
				}
				myWriter.write('\n');
			}
		}
		myWriter.close();
	}


}
