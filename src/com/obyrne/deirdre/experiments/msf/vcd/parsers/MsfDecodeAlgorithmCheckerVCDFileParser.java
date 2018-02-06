package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.util.Hashtable;
import java.util.Map;
import java.util.TreeSet;

import com.obyrne.deirdre.experiments.msf.vcd.util.MsfMillisecondSignalLevelProcessor;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfTimecodeGenerator;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfSignalMillisecondSignalSlice;
import com.obyrne.deirdre.experiments.msf.vcd.util.ReceivedMsfSecond;

/**
 * A VCD file parser which checks an MSF decode algorithm
 */
public class MsfDecodeAlgorithmCheckerVCDFileParser extends AbstractMsfTimecodeVCDFileParser implements MsfMillisecondSignalLevelProcessor {

	private MsfSignalMillisecondSignalSlice[] mySlices;
	private int[][] myDecodedSignals = new int[5][6];
	private Map<Integer,Integer> myErrors = new Hashtable<Integer,Integer>();
	// Keys are binary code and expected signal, values are number of occurences
	private Map<String,Map<Integer,Integer>> myErrorSignals = new Hashtable<String,Map<Integer,Integer>>();

	/**
	 * Constructor
	 * @param slices the slices of the MSF signal that we check
	 * @param timecode the MSF timecode
	 */
	public MsfDecodeAlgorithmCheckerVCDFileParser(MsfSignalMillisecondSignalSlice[] slices, MsfTimecodeGenerator timecode) {
		super(timecode);
		mySlices = slices;
	}

	public void msfMillisecondSignalLevelValue(int receiver, int expectedSignal, int ms, double val) {
		for (MsfSignalMillisecondSignalSlice s : mySlices) {
			s.addSignalSlice(ms, val);
		}
	}

	protected void processReceivedSecond(ReceivedMsfSecond second, int receiver) {
		char[] signal = new char[10];
		int decodedSignal;
		String decoded;
		for (MsfSignalMillisecondSignalSlice s : mySlices) {
			s.resetCount();
		}
		second.processIntoMillisecondValues(receiver, this);
		for (int i = 0 ; i < 10 ; i++) {
			if (mySlices[i].getDecodedLevel() == 0) {
				signal[i] = '0';
			} else {
				signal[i] = '1';
			}
		}
		decoded = new String(signal);
		if (       decoded.equals("0111111111")) {
			decodedSignal = MsfTimecodeGenerator.A0B0;
		} else if (decoded.equals("0101111111")) {
			decodedSignal = MsfTimecodeGenerator.A0B1;
		} else if (decoded.equals("0011111111")) {
			decodedSignal = MsfTimecodeGenerator.A1B0;
		} else if (decoded.equals("0001111111")) {
			decodedSignal = MsfTimecodeGenerator.A1B1;
		} else if (decoded.equals("0000011111")) {
			decodedSignal = MsfTimecodeGenerator.MIN;
		} else {
			decodedSignal = 5;
		}
		myDecodedSignals[second.getSignal()][decodedSignal]++;
		if (second.getSignal() != decodedSignal) {
			int tenMinute = (int)(second.getSecondStart() / 600000000l);
			Integer count = myErrors.get(tenMinute);
			Map<Integer,Integer> signals = myErrorSignals.get(decoded);
			if (count == null) {
				myErrors.put(tenMinute, 1);
			} else {
				myErrors.put(tenMinute, count+1);
			}
			if (signals == null) {
				signals = new Hashtable<Integer,Integer>();
				signals.put(second.getSignal(), 1);
				myErrorSignals.put(decoded, signals);
			} else {
				count = signals.get(second.getSignal());
				if (count == null) count = 0;
				signals.put(second.getSignal(), count+1);
			}
		}

	}

	protected void doWriteResults() {
		TreeSet<String> signalSet = new TreeSet<String>(myErrorSignals.keySet());
		for (int tenMin = 0 ; tenMin < 1200 ; tenMin++) {
			Integer c = myErrors.get(tenMin);
			if (c == null) c = 0;
			System.out.print(tenMin);
			System.out.print(',');
			System.out.println(c.toString());
		}
		for (String signal : signalSet) {
			Map<Integer,Integer> count = myErrorSignals.get(signal);
			System.out.print(signal);
			for (int s = 0 ; s < 5 ; s++) {
				Integer c = count.get(s);
				if (c == null) c = 0;
				System.out.print(',');
				System.out.print(c.toString());
			}
			System.out.println();
		}
		for (int i = 0 ; i < 5 ; i++) {
			System.out.print(i);
			for (int j = 0 ; j < 6 ; j++) {
				System.out.print(String.format(",%d", myDecodedSignals[i][j]));
			}
			System.out.println();
		}
	}

}
