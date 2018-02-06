package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.IOException;

/**
 * An interface for classes which process the VCD file information
 */
public interface VCDFileParser {

	/**
	 * Called when a new GPS second was discovered in the VCD file
	 * @param us the signal analyser microsecond at which the GPS second started
	 */
	public void newGPSSecond(long us);
	
	/**
	 * Called when a new EM2S/MSF signal level is detected in the VCD file
	 * @param us the signal analyser microsecond at which the level was detected
	 * @param receiver the EM2S receiver number
	 * @param value true if the carrier was detected, false otherwise
	 */
	public void newMSFSignal(long us, int receiver, boolean value);
	
	/**
	 * Called when the entire VCD file has been read
	 * @throws IOException I/O exception writing out the results
	 */
	public void writeResults() throws IOException;

}
