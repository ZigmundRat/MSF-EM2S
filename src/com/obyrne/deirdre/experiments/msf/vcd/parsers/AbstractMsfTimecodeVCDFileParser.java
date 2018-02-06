package com.obyrne.deirdre.experiments.msf.vcd.parsers;

import java.io.IOException;

import com.obyrne.deirdre.experiments.msf.vcd.SignalSet;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfTimecodeGenerator;
import com.obyrne.deirdre.experiments.msf.vcd.util.ReceivedMsfSecond;

/**
 * A VCD file parser which has and utilises knowledge of the MSF timecode (i.e. which of the 5 MSF waveforms are
 * currently being transmitted).
 */
public abstract class AbstractMsfTimecodeVCDFileParser implements VCDFileParser {

	private MsfTimecodeGenerator myMsfTimecode;
	private ReceivedMsfSecond[] myThisSecond = new ReceivedMsfSecond[SignalSet.NUM_MSF_RECEIVERS];
	private ReceivedMsfSecond[] myPrevSecond = new ReceivedMsfSecond[SignalSet.NUM_MSF_RECEIVERS];
	private int[] myNumTimecodes = new int[5];

	/**
	 * Constructor
	 * @param timecode the MSF timecode generator we will use to determine which of the 5 MSF waveforms are being transmitted
	 */
	public AbstractMsfTimecodeVCDFileParser(MsfTimecodeGenerator timecode) {
		myMsfTimecode = timecode;
	}

	public void newGPSSecond(long us) {
		myNumTimecodes[myMsfTimecode.getMsfTimecode()]++;
		for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
			if (myPrevSecond[i] != null) {
				processReceivedSecond(myPrevSecond[i], i);
			}
			myPrevSecond[i] = myThisSecond[i];
			myThisSecond[i] = null;
		}
	}

	public void newMSFSignal(long us, int receiver, boolean value) {
		if (myThisSecond[receiver] == null) {
			if (!value) {
				myThisSecond[receiver] = new ReceivedMsfSecond(us, myMsfTimecode.getMsfTimecode());
			}
		} else {
			myThisSecond[receiver].processSignal(us, value);
		}
		if (myPrevSecond[receiver] != null) {
			myPrevSecond[receiver].processSignal(us, value);
		}
	}

	/**
	 * Returns the number of times a particular timecode has been seen
	 * @param timecode the timecode to query
	 * @return the number of times that timecode has been seen
	 */
	protected int getNumTimecodes(int timecode) {
		return myNumTimecodes[timecode];
	}

	/**
	 * This method is called whenever a second has been fully received by an E2MS receiver
	 * @param second the second which has been received
	 * @param receiver the receiver number
	 */
	protected abstract void processReceivedSecond(ReceivedMsfSecond second, int receiver);
	
	/**
	 * Write the results to the output.
	 * @throws IOException I/O exception during write.
	 */
	protected abstract void doWriteResults() throws IOException;
	
	public void writeResults() throws IOException {
		for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
			if (myPrevSecond[i] != null) {
				processReceivedSecond(myPrevSecond[i], i);
			}
			if (myThisSecond[i] != null) {
				processReceivedSecond(myThisSecond[i], i);

			}
		}
		// Now that we have all our data in, it's time to do the actual writing of the results!
		doWriteResults();
	}

}
