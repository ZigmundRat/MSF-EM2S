package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.IOException;

public class StdoutParser implements VCDFileParser {

	public void newGPSSecond(long us) {
		System.out.print("GPS second at ");
		System.out.println(us);
	}

	public void newMSFSignal(long us, int receiver, boolean value) {
		System.out.print(us);
		System.out.print(" MSF ");
		System.out.print(receiver);
		System.out.print(" is ");
		System.out.println(value ? "high" : "low");
	}


	public void writeResults() throws IOException {
	}

}
