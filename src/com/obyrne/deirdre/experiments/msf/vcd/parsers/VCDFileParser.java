package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.IOException;

public interface VCDFileParser {

	public void newGPSSecond(long us);
	public void newMSFSignal(long us, int receiver, boolean value);
	public void writeResults() throws IOException;

}
