/*
 * (C) Copyright 2018 Justin Carrington.
 *
 *  This file is part of TronMachine.

 *  TronMachine is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TronMachine is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with TronMachine.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Justin Carrington
 *     Russell Taylor
 *     Kendra Taylor
 *     Erik Dent
 *     
 */
package com.typicalprojects.TronMachine.util;

import java.util.Arrays;

/**
 * Class used for many simple calculation in the TRON machine.
 * 
 * @author Justin Carrington
 *
 */
public class Analyzer {
	
	/**
	 * An enumeration with the types of calculations that the analyzer can perform.
	 * 
	 * @author Justin Carrington
	 *
	 */
	public enum Calculation {
		PERCENT_MIGRATION, 
		AVERAGE, 
		NUM, 
		STDEV, 
		SQ_DIST, 
		DIST
	}
	
	/**
	 * Performs a calculation with the supplied information.
	 * 
	 * @param calc		The calculation to perform.
	 * @param inputs		Inputs required for the calculation.
	 * @return			The result of the calculation
	 * @throws ArithmeticException	Any errors during calculation, including if the supplied inputs were
	 * incorrect.
	 */
	public static double calculate(Calculation calc, double... inputs) throws ArithmeticException {
		
		switch (calc) {
		case PERCENT_MIGRATION:
			try {
				return 100 - (100 * (inputs[0] / (inputs[0] - inputs[1])));
			} catch (Exception e) {
				throw new ArithmeticException("Incorrect inputs or divide by zero when calculating percent migration");
			}
		case AVERAGE:
			try {
				Arrays.sort(inputs);
				double sum = 0;
				for (double input : inputs) {
					sum+= input;
				}
				return sum / inputs.length;
			} catch (Exception e) {
				throw new ArithmeticException("Incorrect inputs or divide by zero when calculating average");
			}
		case NUM:
			return inputs.length;
		case STDEV:
			try {
				double sums = 0;
				double avg = calculate(Calculation.AVERAGE, inputs);
				for (double input : inputs) {
					sums+=Math.pow((input - avg), 2);
				}
				return Math.sqrt((sums / (inputs.length - 1)));
			} catch (Exception e) {
				throw new ArithmeticException("Null inputs or divide by zero when calculating standard deviation");
			}
		case SQ_DIST:
			try {
				return Math.pow((inputs[0] - inputs[1]), 2) + Math.pow((inputs[2] - inputs[3]), 2);
			} catch (Exception e) {
				throw new ArithmeticException("Not enough inputs when calculating squared distance.");
			}
		case DIST:
			try {
				return Math.sqrt(Math.pow((inputs[0] - inputs[1]), 2) + Math.pow((inputs[2] - inputs[3]), 2));
			} catch (Exception e) {
				throw new ArithmeticException("Not enough inputs when calculating distances.");
			}

		}
		
		
		return Double.NaN;
	}
	
	
}
