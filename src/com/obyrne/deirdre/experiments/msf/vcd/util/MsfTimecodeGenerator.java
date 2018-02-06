package com.obyrne.deirdre.experiments.msf.vcd.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MsfTimecodeGenerator {
	
	public static final int A0B0 = 0;
	public static final int A0B1 = 1;
	public static final int A1B0 = 2;
	public static final int A1B1 = 3;
	public static final int MIN = 4;
	
	private boolean[] A = new boolean[60];
	private boolean[] PARITIES = new boolean[4];
	
	private Calendar myCurrentTime;
	
	public MsfTimecodeGenerator(Calendar calendar) {
		myCurrentTime = calendar;
		myCurrentTime.add(Calendar.MINUTE, 1); // MSF broadcasts the time for the next minute
		recalculateTimecode();
	}

	private boolean getParity(int min, int max) {
		boolean ans = true;
		for (int i = min ; i <= max ; i++) {
			if (A[i]) ans = !ans;
		}
		return ans;
	}
	
	private void add2DigitBcd(int value, int index, int maxFieldValue) {
		while (maxFieldValue >= 10) {
			if (value >= maxFieldValue) {
				A[index++] = true;
				value -= maxFieldValue;
			} else {
				A[index++] = false;
			}
			maxFieldValue /= 2;
		}
		maxFieldValue = 8;
		for (int i = 0 ; i < 4 ; i++) {
			if (value >= maxFieldValue) {
				A[index++] = true;
				value -= maxFieldValue;
			} else {
				A[index++] = false;
			}
			maxFieldValue /= 2;
		}
	}

	public void nextSec() {
		myCurrentTime.add(Calendar.SECOND, 1);
		if (myCurrentTime.get(Calendar.SECOND) == 0) recalculateTimecode();
	}

	public int getMsfTimecode() {
		int sec = myCurrentTime.get(Calendar.SECOND);
		if (sec == 0) return MIN;
		if (sec < 3) return A0B1;
		if (sec < 17) return A0B0;
		if (sec < 53) {
			return A[sec] ? A1B0 : A0B0;
		}
		// TODO Leap seconds are inserted hereabouts...
		// TODO BUG This should give the summer time warning!
		if (sec == 53) return A1B0;
		if (sec < 58) {
			return (PARITIES[sec-54] ? A1B1 : A1B0); 
		}
		if (sec == 58) {
			return myCurrentTime.get(Calendar.DST_OFFSET) != 0 ? A1B1 : A1B0;
		}
		return A0B0;
	}

	private void recalculateTimecode() {
		int i;
		A[0]=true;
		for (i = 1 ; i < 17 ; i++) A[i] = false;
		add2DigitBcd(myCurrentTime.get(Calendar.YEAR) % 100, 17, 80);
		add2DigitBcd(myCurrentTime.get(Calendar.MONTH) + 1, 25, 10);
		add2DigitBcd(myCurrentTime.get(Calendar.DAY_OF_MONTH), 30, 20);
		i = myCurrentTime.get(Calendar.DAY_OF_WEEK) - 1;
		A[36] = (i & 4) != 0;
		A[37] = (i & 2) != 0;
		A[38] = (i & 1) != 0;
		add2DigitBcd(myCurrentTime.get(Calendar.HOUR_OF_DAY), 39, 20);
		add2DigitBcd(myCurrentTime.get(Calendar.MINUTE), 45, 40);
		A[52]=false;
		A[53]=true; A[54]=true; A[55]=true; A[56]=true; A[57]=true; A[58]=true; A[59]=false;
		PARITIES[0]=getParity(17,24);
		PARITIES[1]=getParity(25,35);
		PARITIES[2]=getParity(36,38);
		PARITIES[3]=getParity(39,51);
	}
	
	public static void main(String[] args) throws Exception {
		Calendar cal = GregorianCalendar.getInstance();
		MsfTimecodeGenerator me;
		
		cal.set(2018,0,27,14,38,0);
		me = new MsfTimecodeGenerator(cal);
		for (int i = 0 ; i < 240 ; i++) {
			switch (me.getMsfTimecode()) {
			case 0: System.out.print(" 00"); break;
			case 1: System.out.print(" 01"); break;
			case 2: System.out.print(" 10"); break;
			case 3: System.out.print(" 11"); break;
			case 4: System.out.print("\nMIN"); break;
			}
			me.nextSec();
		}
	}
}
