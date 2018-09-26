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

import com.typicalprojects.TronMachine.util.ImageContainer.Channel;

import ij.measure.ResultsTable;


public class AdvancedWorkbook {

	private XSSFWorkbook workbook;

	public AdvancedWorkbook() {
		workbook = new XSSFWorkbook();
	}

	public AdvancedWorkbook(File file) throws InvalidFormatException, IOException {
		workbook = new XSSFWorkbook(file);
	}

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
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public XSSFSheet getSheet(String name) {

		return workbook.getSheet(name);
	}

	public LinkedHashMap<String, Map<Channel, double[]>> pullNeuronCounterData() {


		LinkedHashMap<Channel, XSSFSheet> sheets = new LinkedHashMap<Channel, XSSFSheet>();
		for (Channel chan : Channel.values()) {
			XSSFSheet sheet = getSheet(chan.name());
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

	/*public void writeSummaryStatsSheets(List<String> denseDataMapNames, List<Map<Channel, LinkedHashMap<String, double[]>>> denseDataMap) throws Exception {

		XSSFSheet summarySheet = workbook.createSheet("SUMMARY");

		int offset = 0;
		XSSFFont boldFont = new XSSFFont(workbook.getFontAt((short)0).getCTFont());
		boldFont.setBold(true);


		for (int i = 0; i < denseDataMapNames.size(); i++) {

			String name = denseDataMapNames.get(i);
			int numChans = denseDataMap.get(0).size();
			//setup
			summarySheet.addMergedRegion(new CellRangeAddress(0,0,offset + 0, offset + (numChans * 3)));
			Cell titleCell = _getCell(summarySheet, 0, offset + 0);
			_setTextCenteredAndFont(titleCell, boldFont, name);

			Map<Channel, LinkedHashMap<String, double[]>> summaryContent = denseDataMap.get(i);
			_getCell(summarySheet, 2, offset + 0).setCellValue(new XSSFRichTextString("File Name"));;

			int chanTitleOffset = 1;
			for (Channel chan : Channel.values()) {
				LinkedHashMap<String, double[]> chanSummary = summaryContent.get(chan);
				if (chanSummary == null)
					continue;

				summarySheet.addMergedRegion(new CellRangeAddress(1, 1, offset + chanTitleOffset, offset + chanTitleOffset + 2));

				Cell chanTitleCell = _getCell(summarySheet, 1, offset + chanTitleOffset);
				_setTextCenteredAndFont(chanTitleCell, boldFont, chan.name());
				_getCell(summarySheet, 2, offset + chanTitleOffset).setCellValue(new XSSFRichTextString("Mean"));
				_getCell(summarySheet, 2, offset + chanTitleOffset + 1).setCellValue(new XSSFRichTextString("SD"));
				_getCell(summarySheet, 2, offset + chanTitleOffset + 2).setCellValue(new XSSFRichTextString("N"));

				int rowCounter = 3;
				for (Entry<String, double[]> en : chanSummary.entrySet()) {
					_getCell(summarySheet, rowCounter, offset).setCellValue(new XSSFRichTextString(en.getKey()));
					_getCell(summarySheet, rowCounter, offset + chanTitleOffset).setCellValue(en.getValue()[0]);
					_getCell(summarySheet, rowCounter, offset + chanTitleOffset + 1).setCellValue(en.getValue()[1]);
					_getCell(summarySheet, rowCounter, offset + chanTitleOffset + 2).setCellValue((int) en.getValue()[2]);
					rowCounter++;
				}

				chanTitleOffset = chanTitleOffset + 3;
			}


			offset = offset + (3 * numChans) + 3;
		}

	}*/

	public void writeSummaryStatsSheets(Map<Channel, LinkedHashMap<String, double[]>> denseDataMap) throws Exception {

		XSSFSheet summarySheet = workbook.createSheet("SUMMARY");

		//XSSFFont boldFont = new XSSFFont(workbook.getFontAt((short)0).getCTFont());
		//boldFont.setBold(true);



		//setup
		summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 6));
		Cell titleCell = _getCell(summarySheet, 0, 1);
		_setTextCenterUppercase(titleCell, "Distance from First Line Drawn");

		_getCell(summarySheet, 2, 0).setCellValue(new XSSFRichTextString("File Name"));;

		int chanTitleOffset = 1;
		for (Channel chan : Channel.values()) {
			LinkedHashMap<String, double[]> chanSummary = denseDataMap.get(chan);
			if (chanSummary == null)
				continue;

			summarySheet.addMergedRegion(new CellRangeAddress(1, 1, chanTitleOffset, chanTitleOffset + 2));

			Cell chanTitleCell = _getCell(summarySheet, 1, chanTitleOffset);
			_setTextCenterUppercase(chanTitleCell, chan.name());
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

	public Cell _getCell(XSSFSheet sheet, int rowNum, int colNum) {
		Row row = sheet.getRow(rowNum);
		if (row == null)
			row = sheet.createRow(rowNum);

		Cell cell = row.getCell(colNum);
		if (cell == null)
			cell = row.createCell(colNum);

		return cell;
	}

}
