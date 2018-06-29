package com.typicalprojects.CellQuant.neuronal_migration.processing;


public class Analyzer {
	
	public enum Calculation {
		PERCENT_MIGRATION
	}
	
	public static double calculate(Calculation calc, double... inputs) throws ArithmeticException {
		
		if (calc.equals(Calculation.PERCENT_MIGRATION)) {
			try {
				return 100 - (100 * (inputs[0] / (inputs[0] - inputs[1])));
			} catch (Exception e) {
				throw new ArithmeticException("Null inputs or divide by zero when calculation percent migration");
			}
		}
		
		return Double.NaN;
	}
	
	
}
