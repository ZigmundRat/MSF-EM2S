package com.obyrne.deirdre.experiments.msf.vcd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.obyrne.deirdre.experiments.msf.vcd.parsers.VCDFileParser;

/**
 * A VCD file containing the GPS and MSF data. This VCD file must have a timebase of 1us, and it must contain three
 * wire signals
 * <ul>
 * <li> wire 1 ! MSF1 </li>
 * <li> wire 1 " MSF2 </li>
 * <li> wire 1 # GPS </li>
 * </ul>
 * The two MSF signals must be high when the carrier is detected, and low when it is not. The GPS signal must be a
 * PPS signal whose rising edge is synchronised to UTC seconds. Furthermore, the file needs to start recording in the
 * second before 2018/01/28 22:57:55.
 */
public class VCDFile {

	private BufferedReader myReader = null;
	private long myLastGPSEdge = 0L;
	
	/**
	 * Constructor
	 * @param in an input stream from the VCD file
	 * @throws IOException an I/O exception during VCD file verification
	 */
	public VCDFile(InputStream in) throws IOException {
		String line;
		myReader = new BufferedReader(new InputStreamReader(in));
		while (true) {
			line = myReader.readLine();
			if (line.startsWith("$timescale ")) {
				if (!line.contains(" 1 us $end")) throw new IOException("Need 1us time scale");
			} else if (line.startsWith("$var wire 1 ")) {
				if ((!line.contains(" ! MSF1 $end")) && (!line.contains(" MSF2 $end")) && (!line.contains(" # GPS $end")))
					throw new IOException("Not the expected signals");
			} else if (line.equals("$enddefinitions $end")) {
				return;
			}
		}
	}

	/**
	 * Parse this VCD file into a parser, which will analyse it and produce a report
	 * @param parser the parser
	 * @throws IOException I/O exception during parsing
	 */
	public void parseInto(VCDFileParser parser) throws IOException {
		String line;
		boolean gpsSecondFound = false;
		while ((line = myReader.readLine()) != null) {
			SignalSet signals = new SignalSet(line);
			if (!gpsSecondFound) {
				if ((signals.getGps() != null) && (signals.getGps())) {
					myLastGPSEdge = signals.getTime();
					parser.newGPSSecond(myLastGPSEdge);
					gpsSecondFound = true;
				}
			} else {
				if ((signals.getGps() != null) && (signals.getGps()) && (signals.getTime() - myLastGPSEdge > 1000000L)) {
					myLastGPSEdge = signals.getTime();
					parser.newGPSSecond(myLastGPSEdge);
				}
				for (int i = 0 ; i < SignalSet.NUM_MSF_RECEIVERS ; i++) {
					if (signals.getMsf(i) != null) {
						parser.newMSFSignal(signals.getTime(), i, signals.getMsf(i));
					}
				}
			}
		}
	}

	/**
	 * Close the VCD file
	 * @throws IOException I/O exception on close
	 */
	public void close() throws IOException {
		myReader.close();
	}

}
