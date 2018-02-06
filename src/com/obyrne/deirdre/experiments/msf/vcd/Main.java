package com.obyrne.deirdre.experiments.msf.vcd;

import java.io.FileInputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.obyrne.deirdre.experiments.msf.vcd.parsers.GPSSecondIntervalsVCDFileParser;
import com.obyrne.deirdre.experiments.msf.vcd.parsers.MsfDecodeAlgorithmCheckerVCDFileParser;
import com.obyrne.deirdre.experiments.msf.vcd.parsers.MsfEdgesVCDFileParser;
import com.obyrne.deirdre.experiments.msf.vcd.parsers.MsfPulseWidthsVCDFileParser;
import com.obyrne.deirdre.experiments.msf.vcd.parsers.MsfSecondEdgesVCDFileParser;
import com.obyrne.deirdre.experiments.msf.vcd.parsers.MsfSignalSlicesSignalLevelVCDFileParser;
import com.obyrne.deirdre.experiments.msf.vcd.parsers.MsfTimecodeVCDFileParser;
import com.obyrne.deirdre.experiments.msf.vcd.parsers.VCDFileParser;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfTimecodeGenerator;
import com.obyrne.deirdre.experiments.msf.vcd.util.MsfSignalMillisecondSignalSlice;

/**
 * The main class, containing the main(String[] args) method which kicks off an analysis
 */
public class Main {
	
//	private static final String INPUT_FILE = "./data1.vcd";
	private static final String INPUT_FILE = "C:\\cygwin64\\home\\User\\msf-sigrok\\20180128225755.vcd";
//	public static final String OUTPUT_DIRECTORY = "./";
	public static final String OUTPUT_DIRECTORY = "C:\\cygwin64\\home\\User\\msf-sigrok\\";

	// The time of the first record in the input file
	private static final int YR = 2018;
	private static final int MON = 0;
	private static final int DAY = 28;
	private static final int HR = 22;
	private static final int MIN = 57;
	private static final int SEC = 54;
	
	private static Calendar CALENDAR = GregorianCalendar.getInstance();

	/**
	 * Returns an array of slices which it was thought might lead to a good algorithm
	 */
	public static MsfSignalMillisecondSignalSlice[] get20msSlices() {
		return new MsfSignalMillisecondSignalSlice[] {
				new MsfSignalMillisecondSignalSlice(0, 80),
				new MsfSignalMillisecondSignalSlice(160, 180),
				new MsfSignalMillisecondSignalSlice(260, 280),
				new MsfSignalMillisecondSignalSlice(360, 480),
				new MsfSignalMillisecondSignalSlice(560, 980)
		};
	}
	
	/**
	 * Returns an array of 100 millisecond slices which was used in the development of the final algorithm
	 */
	public static MsfSignalMillisecondSignalSlice[] get100msSlices() {
		return new MsfSignalMillisecondSignalSlice[] {
				new MsfSignalMillisecondSignalSlice(0, 100),
				new MsfSignalMillisecondSignalSlice(100, 200),
				new MsfSignalMillisecondSignalSlice(200, 300),
				new MsfSignalMillisecondSignalSlice(300, 500),
				new MsfSignalMillisecondSignalSlice(500, 800),
				new MsfSignalMillisecondSignalSlice(900, 1000)
		};
	}

	/**
	 * Returns an array of 10 x 100 millisecond slices
	 */
	public static MsfSignalMillisecondSignalSlice[] getAll100msSlices() {
		return new MsfSignalMillisecondSignalSlice[] {
				new MsfSignalMillisecondSignalSlice(0, 100),
				new MsfSignalMillisecondSignalSlice(100, 200),
				new MsfSignalMillisecondSignalSlice(200, 300),
				new MsfSignalMillisecondSignalSlice(300, 400),
				new MsfSignalMillisecondSignalSlice(400, 500),
				new MsfSignalMillisecondSignalSlice(500, 600),
				new MsfSignalMillisecondSignalSlice(600, 700),
				new MsfSignalMillisecondSignalSlice(700, 800),
				new MsfSignalMillisecondSignalSlice(800, 900),
				new MsfSignalMillisecondSignalSlice(900, 1000)
		};
	}
	
	/**
	 * Returns an array of slices which it was thought might lead to a good algorithm
	 */
	public static MsfSignalMillisecondSignalSlice[] getAll20msSlices() {
		return new MsfSignalMillisecondSignalSlice[] {
				new MsfSignalMillisecondSignalSlice(0, 80),
				new MsfSignalMillisecondSignalSlice(160, 180),
				new MsfSignalMillisecondSignalSlice(260, 280),
				new MsfSignalMillisecondSignalSlice(360, 400),
				new MsfSignalMillisecondSignalSlice(400, 480),
				new MsfSignalMillisecondSignalSlice(560, 600),
				new MsfSignalMillisecondSignalSlice(600, 700),
				new MsfSignalMillisecondSignalSlice(700, 800),
				new MsfSignalMillisecondSignalSlice(800, 900),
				new MsfSignalMillisecondSignalSlice(900, 980)
		};
	}

	public static void main(String[] args) throws Exception {
		VCDFile file = new VCDFile(new FileInputStream(INPUT_FILE));
		VCDFileParser parser;
		CALENDAR.set(YR, MON, DAY, HR, MIN, SEC);
		
		// uncomment one of the lines below to generate an analysis of the data
		
//		parser = new MsfEdges(new MsfSignalGenerator(CALENDAR));
//		parser = new MsfTimecode(new MsfSignalGenerator(CALENDAR));
//		parser = new MsfSecondEdges();
//		parser = new GPSSecondIntervals();
//		parser = new MsfPulseWidths();
//		parser = new MsfSignalSlices("msf-20ms.csv",get20msSlices(), new MsfSignalGenerator(CALENDAR));
//		parser = new MsfSignalSlices("msf-20ms.csv",getAll20msSlices(), new MsfSignalGenerator(CALENDAR));
//		parser = new MsfSignalSlices("msf-100ms.csv",get100msSlices(), new MsfSignalGenerator(CALENDAR));
//		parser = new MsfSignalSlices("msf-100ms.csv",getAll100msSlices(), new MsfSignalGenerator(CALENDAR));
		parser = new MsfDecodeAlgorithmCheckerVCDFileParser(getAll100msSlices(), new MsfTimecodeGenerator(CALENDAR));
//		parser = new MsfDecodeAlgorithmChecker(getAll20msSlices(), new MsfSignalGenerator(CALENDAR));
		
		
		file.parseInto(parser);
		parser.writeResults();
		file.close();
	}

}
