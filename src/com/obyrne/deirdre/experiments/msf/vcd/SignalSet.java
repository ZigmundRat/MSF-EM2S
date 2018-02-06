package com.obyrne.deirdre.experiments.msf.vcd;

/**
 * A set of signals (MSF1, MSF2 and/or GPS) from a single line of the VCD input file
 */
public class SignalSet {

	// The character used in the VCD file for each of the signals
	private static final char MSF1_VCD_CHAR = '!';
	private static final char MSF2_VCD_CHAR = '"';
	private static final char GPS_VCD_CHAR = '#';
	
	// The number of MSF receivers we have - used in data normalisation calculations
	public static final int NUM_MSF_RECEIVERS = 2;
	
	// A set of Booleans, which are
	// - true if the line contains an entry indicating the MSF carrier or the GPS rising edge have been detected
	// - false if the line contains an entry indicating the lack of MSF carrier or GPS falling edge have been detected
	// - null if the line does not mention the MSF carrier / GPS signal
	private Boolean myGps = null;
	private Boolean myMsf1 = null;
	private Boolean myMsf2 = null;
	
	// The time of this entry in logic analyser microseconds
	private long myTime;
	
	/**
	 * Constructor
	 * @param line the line as read from the VCD input file
	 */
	public SignalSet(String line) {
		String[] elements = line.split(" ");
		myTime = Long.parseLong(elements[0].substring(1));
		for (int i = 1 ; i < elements.length ; i++) {
			char[] element = elements[i].toCharArray();
			switch (element[1]) {
			case MSF1_VCD_CHAR: myMsf1 = (element[0] == '1'); break;
			case MSF2_VCD_CHAR: myMsf2 = (element[0] == '1'); break;
			case GPS_VCD_CHAR: myGps = (element[0] == '1'); break;
			}
		}
	}

	/**
	 * Get the GPS signal
	 * @return true if this line contains the GPS rising edge (synchronised on the UTC second), false if it contains the
	 *         GPS falling edge, or null if it does not mention the GPS signal
	 */
	public Boolean getGps() {
		return myGps;
	}
	
	/**
	 * Get the EM2S MSF signal for a particular receiver
	 * @param receiver the receiver number
	 * @return true if the MSF carrier signal has been detected, false if the MSF carrier signal loss has been detected, or
	 *         null if the line does not mention the MSF carrier signal
	 */
	public Boolean getMsf(int receiver) {
		if (receiver == 0) {
			return myMsf1;
		} else if (receiver == 1) {
			return myMsf2;
		}
		return null;
	}

	/**
	 * Get the time of this entry
	 * @return the time of this entry in signal analyser microseconds. It is observed that there are an average of
	 *         about 1,000,152.2 of these microseconds per SI second.
	 */
	public long getTime() {
		return myTime;
	}

}
