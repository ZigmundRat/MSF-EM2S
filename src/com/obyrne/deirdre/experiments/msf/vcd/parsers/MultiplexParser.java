package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.IOException;

public class MultiplexParser implements VCDFileParser {

	private VCDFileParser[] myParsers;
	
	public MultiplexParser(VCDFileParser[] parsers) {
		myParsers = parsers;
	}

	public void newGPSSecond(long us) {
		for (VCDFileParser parser : myParsers) {
			parser.newGPSSecond(us);
		}
	}

	public void newMSFSignal(long us, int receiver, boolean value) {
		for (VCDFileParser parser : myParsers) {
			parser.newMSFSignal(us, receiver, value);
		}
	}


	public void writeResults() throws IOException {
		for (VCDFileParser parser : myParsers) {
			parser.writeResults();
		}
	}

}
