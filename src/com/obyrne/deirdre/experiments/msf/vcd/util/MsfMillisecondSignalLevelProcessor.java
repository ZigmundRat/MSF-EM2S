package com.obyrne.deirdre.experiments.msf.vcd.util;

/**
 * The E2MS/MSF signal is split up into millisecond chunks for analysis. This interface is for those classes which
 * process those chunks.
 */
public interface MsfMillisecondSignalLevelProcessor {

	/**
	 * A millisecond-long chunk of the signal has been analysed, and the results are presented for further processing
	 * @param receiver the E2MS receiver number
	 * @param expectedSignal the expected MSF signal
	 * @param ms the number of the millisecond chunk (referenced from the E2MS falling edge which marks the start of the
	 *        MSF second)
	 * @param val the average value of the signal during this chunk
	 */
	public void msfMillisecondSignalLevelValue(int receiver, int expectedSignal, int ms, double val);

}
