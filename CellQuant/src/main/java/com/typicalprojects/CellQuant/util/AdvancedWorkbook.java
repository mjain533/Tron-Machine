package com.typicalprojects.CellQuant.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import ij.measure.ResultsTable;


public class AdvancedWorkbook {

	private XSSFWorkbook workbook = new XSSFWorkbook();
	
	public AdvancedWorkbook() {
		
	}

	public void addSheetFromResultTable(String name, ResultsTable table){
		
		try {
			XSSFSheet sheet = workbook.createSheet(name);

			XSSFRow firstRow = sheet.createRow(0);
			
			int counter = 0;
			for (String heading : table.getHeadings()) {
				firstRow.createCell((short) counter).setCellValue(new XSSFRichTextString(heading));
				counter++;
			}
			List<double[]> columns = new ArrayList<double[]>();
			for (int i = 0; i < table.getHeadings().length; i++) {
				columns.add(table.getColumnAsDoubles(i));
			}
			int numberOfRows = columns.get(0).length;
			int numberOfColumns = columns.size();
			for (int row = 0; row < numberOfRows; row++) {
				XSSFRow xlRow = sheet.createRow(row + 1);
				
				for (short col = 0; col < numberOfColumns; col++) {
					xlRow
					.createCell(col)
					.setCellValue(
							columns.get(col)
							[row]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public XSSFSheet getSheet(String name) {
		return workbook.getSheet(name);
	}
	
	public boolean save(File saveFile) {
		try {
			saveFile.createNewFile();
			workbook.write(new FileOutputStream(saveFile));
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}
