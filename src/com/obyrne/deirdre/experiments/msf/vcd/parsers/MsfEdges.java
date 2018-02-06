package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import com.obyrne.deirdre.experiments.msf.vcd.Main;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfTimecodeGenerator;
import com.obyrne.deirdre.experiments.msf.vcd.util.ReceivedMsfSecond;

public class MsfEdges extends AbstractMsfTimecodeVCDFileParser {

	private static final String OUTPUT_FILE = Main.OUTPUT_DIRECTORY + "msf-edges.csv";
	
	private Map<Integer, Map<Integer,int[]>> myAns = new HashMap<Integer, Map<Integer,int[]>>();
	private Writer myWriter;

	public MsfEdges(MsfTimecodeGenerator timecode) throws IOException {
		super(timecode);
		myWriter = new FileWriter(new File(OUTPUT_FILE), false);
		myWriter.write("\"Millis since MSF second\",\"A0B0 F\",\"A0B0 R\",\"A0B1 F\",\"A0B1 R\"," +
				"\"A1B0 F\",\"A1B0 R\",\"A1B1 F\",\"A1B1 R\",\"MIN F\",\"MIN R\"\n");
	}

	protected void processReceivedSecond(ReceivedMsfSecond second, int receiver) {
		second.addEdges(myAns);
	}
	
	private void writeDoubleVal(double v) throws IOException {
		myWriter.write(String.format(Locale.ENGLISH,"%09.7f",v));
	}

	protected void doWriteResults() throws IOException {
		TreeSet<Integer> millis = new TreeSet<Integer>();
		millis.addAll(myAns.keySet());
		for (int i : millis) {
			Map<Integer,int[]> cols = myAns.get(i);
			myWriter.write(Integer.toString(i));
			for (int sig = 0 ; sig < 5 ; sig++) {
				int[] v = cols.get(sig);
				if (v == null) v = new int[2];
				myWriter.write(',');
				writeDoubleVal((double)(v[0])/(double)(getNumTimecodes(sig)));
				myWriter.write(',');
				writeDoubleVal((double)(v[1])/(double)(getNumTimecodes(sig)));
			}
			myWriter.write('\n');
		}
		myWriter.close();
	}

}
