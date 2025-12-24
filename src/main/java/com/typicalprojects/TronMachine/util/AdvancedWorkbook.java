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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;

/**
 * Basically a wrapper of an Excel workbook that allows reading and writing to the notebook in a less
 * tedius way. The excel files are generated as the output of the TRON machine.
 * 
 * @author Justin Carrington
 *
 */
public class AdvancedWorkbook {

	private XSSFWorkbook workbook;

	/**
	 * Construct a new workbook by creating an excel workbook object.
	 */
	public AdvancedWorkbook() {
		workbook = new XSSFWorkbook();
	}

	/**
	 * Creates a new workbook by loading an excel workbook object from an existing excel file sheet on the
	 * user's file system.
	 * 
	 * @param file	The .xlsx file (excel file) to load the Excel workbook object from
	 * @throws InvalidFormatException The CSV file is not correctly formatted like an excel file.
	 * @throws IOException Any read errors that while loading the excel sheet.
	 */
	public AdvancedWorkbook(File file) throws InvalidFormatException, IOException {
		workbook = new XSSFWorkbook(file);
	}

	/**
	 * Creates an excel sheet from a given table of results from the neuron object counter. Basically just
	 * dumps the results table into an excel sheet. The ResultsTable MUST have headings set for every column
	 * or this method will fail.
	 * 
	 * @param name	The name of the excel sheet to be added to the current workbook
	 * @param table	The table form which to draw data
	 */	
	public void addSheetFromNeuronCounterResultTable(String name, ResultsTable table){

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
			for (int i = 0; i < numberOfColumns; i++) {
				sheet.autoSizeColumn(i);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * @param name the unique name of a sheet within this workbook.
	 * @return the sheet in question. Null if the sheet does not exist.
	 */
	public XSSFSheet getSheet(String name) {

		return workbook.getSheet(name);
	}
	
	/**
	 * Pulls the data that was saved from the neuron counter specifically.
	 * 
	 * @param chanManager	The channel manager which was used at the time of save, which is necessary because
	 * 						otherwise we would not know the title of the sheet which contains the data.
	 * @return Data from neuron counter.
	 */
	public LinkedHashMap<String, Map<Channel, double[]>> pullNeuronCounterData(ChannelManager chanManager) {


		LinkedHashMap<Channel, XSSFSheet> sheets = new LinkedHashMap<Channel, XSSFSheet>();
		for (Channel chan : chanManager.getChannels()) {
			XSSFSheet sheet = getSheet(chan.getName());
			if (sheet != null)
				sheets.put(chan, sheet);
		}

		LinkedHashMap<String, Map<Channel, double[]>> newOutput = new LinkedHashMap<String, Map<Channel, double[]>>();

		for (Entry<Channel, XSSFSheet> sheetEN : sheets.entrySet()) {

			XSSFSheet sheet = sheetEN.getValue();

			for (int numColToCalc = 0; numColToCalc < 200; numColToCalc++) {
				Row headingRow = sheet.getRow(0);
				if (headingRow == null)
					break;
				Cell cell = headingRow.getCell(3 + numColToCalc);
				if (cell == null || !cell.getStringCellValue().startsWith("Distance from")) {
					break;
				} else {
					Map<Channel, double[]> values = null;
					if (!newOutput.containsKey(cell.getStringCellValue())) {
						values = new HashMap<Channel, double[]>();
						newOutput.put(cell.getStringCellValue(), values);
					} else {
						values = newOutput.get(cell.getStringCellValue());
					}
					double[] doubleVals = new double[sheet.getLastRowNum()];
					for (int row = 1; row <= sheet.getLastRowNum(); row++) {
						doubleVals[row - 1] = sheet.getRow(row).getCell(3 + numColToCalc).getNumericCellValue();
					}
					values.put(sheetEN.getKey(), doubleVals);
				}

			}
			



		}

		return newOutput;
	}
	
	/**
	 * Saves the virtual excel sheet to an actual file on the user's system.
	 * 
	 * @param saveFile the File to save to. If the file does not yet exist, it will first be created.
	 * @return true if the save was successful, false if there was an error during saving.
	 */
	public boolean save(File saveFile) {
		try {
			if (!saveFile.getParentFile().exists()) {
				saveFile.getParentFile().mkdirs();
			}
			saveFile.createNewFile();
			
			FileOutputStream fileOut = new FileOutputStream(saveFile);
			try {
				workbook.write(fileOut);
			} finally {
				fileOut.close();
			}
			
			System.out.println("Successfully saved: " + saveFile.getAbsolutePath());
			return true;
		} catch (IOException e) {
			System.err.println("Failed to save file: " + saveFile.getAbsolutePath());
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Writes some simple summary data based on an input excel file.
	 * 
	 * @param denseDataMap	Data map which is returned after calculation of distances of neuron migration.
	 * @param chanMap		The mapping of data
	 * @throws Exception		If tehre are any errors in writing.
	 */
	public void writeSummaryStatsSheets(Map<Channel, LinkedHashMap<String, double[]>> denseDataMap, ChannelManager chanMap) throws Exception {

		XSSFSheet summarySheet = workbook.createSheet("SUMMARY");


		//setup
		summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 6));
		Cell titleCell = _getCell(summarySheet, 0, 1);
		_setTextCenterUppercase(titleCell, "Distance from First Line Drawn");

		_getCell(summarySheet, 2, 0).setCellValue(new XSSFRichTextString("File Name"));;

		int chanTitleOffset = 1;
		for (Channel chan : chanMap.getChannels()) {
			LinkedHashMap<String, double[]> chanSummary = denseDataMap.get(chan);
			if (chanSummary == null)
				continue;

			summarySheet.addMergedRegion(new CellRangeAddress(1, 1, chanTitleOffset, chanTitleOffset + 2));

			Cell chanTitleCell = _getCell(summarySheet, 1, chanTitleOffset);
			_setTextCenterUppercase(chanTitleCell, chan.getName());
			_getCell(summarySheet, 2, chanTitleOffset).setCellValue(new XSSFRichTextString("Mean"));
			_getCell(summarySheet, 2, chanTitleOffset + 1).setCellValue(new XSSFRichTextString("SD"));
			_getCell(summarySheet, 2, chanTitleOffset + 2).setCellValue(new XSSFRichTextString("N"));

			int rowCounter = 3;
			for (Entry<String, double[]> en : chanSummary.entrySet()) {
				_getCell(summarySheet, rowCounter, 0).setCellValue(new XSSFRichTextString(en.getKey()));
				_getCell(summarySheet, rowCounter, chanTitleOffset).setCellValue(en.getValue()[0]);
				_getCell(summarySheet, rowCounter, chanTitleOffset + 1).setCellValue(en.getValue()[1]);
				_getCell(summarySheet, rowCounter, chanTitleOffset + 2).setCellValue((int) en.getValue()[2]);
				rowCounter++;
			}

			chanTitleOffset = chanTitleOffset + 3;
		}

	}

	private void _setTextCenterUppercase(Cell cell, String text) {
		XSSFCellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		cell.setCellStyle(style);
		RichTextString content = new XSSFRichTextString(text.toUpperCase());
		//content.applyFont(font);
		cell.setCellValue(content);

	}
	
	/**
	 * Helper method in order to retrieve a cell (i.e. just returns the cell, but if it does not exist yet,
	 * first creates the cell.
	 * 
	 * @param sheet	The sheet in question
	 * @param rowNum	Row in question
	 * @param colNum	ObjectColumn in question
	 * @return XSSF cell
	 */
	private Cell _getCell(XSSFSheet sheet, int rowNum, int colNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null)
			row = sheet.createRow(rowNum);

		Cell cell = row.getCell(colNum);
		if (cell == null)
			cell = row.createCell(colNum);

		return cell;
	}

}
