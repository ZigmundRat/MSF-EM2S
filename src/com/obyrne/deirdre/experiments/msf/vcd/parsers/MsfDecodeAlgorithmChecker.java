package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.util.Hashtable;
import java.util.Map;
import java.util.TreeSet;

import com.obyrne.deirdre.experiments.msf.vcd.util.MsfMillisecondSignalLevelProcessor;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfTimecodeGenerator;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfSignalMillisecondSignalSlice;
import com.obyrne.deirdre.experiments.msf.vcd.util.ReceivedMsfSecond;

public class MsfDecodeAlgorithmChecker extends AbstractMsfTimecodeVCDFileParser implements MsfMillisecondSignalLevelProcessor {

	private MsfSignalMillisecondSignalSlice[] mySlices;
	private int[][] myDecodedSignals = new int[5][6];
	private Map<Integer,Integer> myErrors = new Hashtable<Integer,Integer>();
	private Map<String,Integer> myErrorSignals = new Hashtable<String,Integer>();

	public MsfDecodeAlgorithmChecker(MsfSignalMillisecondSignalSlice[] slices, MsfTimecodeGenerator timecode) {
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
			Integer count = myErrorSignals.get(decoded);
			decodedSignal = 5;
			if (count == null) {
				myErrorSignals.put(decoded, 1);
			} else {
				myErrorSignals.put(decoded, count+1);
			}
		}
		myDecodedSignals[second.getSignal()][decodedSignal]++;
		if (second.getSignal() != decodedSignal) {
			int tenMinute = (int)(second.getSecondStart() / 600000000l);
			Integer count = myErrors.get(tenMinute);
			if (count == null) {
				myErrors.put(tenMinute, 1);
			} else {
				myErrors.put(tenMinute, count+1);
			}
		}

	}

	protected void doWriteResults() {
//		TreeSet<Integer> tenMinutes = new TreeSet<Integer>(myErrors.keySet());
		TreeSet<String> signalSet = new TreeSet<String>(myErrorSignals.keySet());
		for (int tenMin = 0 ; tenMin < 1200 ; tenMin++) {
			System.out.print(tenMin);
			System.out.print(',');
			System.out.println(myErrors.get(tenMin));
		}
		for (String signal : signalSet) {
			System.out.print(signal);
			System.out.print(',');
			System.out.println(myErrorSignals.get(signal));
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
