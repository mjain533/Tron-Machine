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
package com.typicalprojects.TronMachine.neuronal_migration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;


public class Settings {

	/*
	 * This will only be true if we did NOT force load defaults, and the custom did not exist or did not match defaults.
	 */
	public boolean needsUpdate = false;

	public Map<Integer, ImageContainer.Channel> channelMap = new HashMap<Integer, ImageContainer.Channel>();
	public List<Channel> channelsToProcess = null;
	public File outputLocation = null;
	public List<File> recentOpenFileLocations = null;
	public List<File> recentOpenAnalysisOutputLocations = null;
	public boolean calculateBins = true;
	public Channel primaryRoiDrawChannel = null;
	public boolean drawBinLabels = true;
	public int numberOfBins = -1;
	public boolean excludePtsOutsideBin = true;
	public boolean includePtsNearestBin = false;
	public int processingMinThreshold = 0;
	public int processingUnsharpMaskRadius = 20;
	public double processingUnsharpMaskWeight = 0.8;
	public double processingGaussianSigma = 0.5;
	public List<String> calibrations = null;
	public int calibrationNumber = -1;
	public LinkedHashMap<OutputOption, OutputParams> enabledOptions = new LinkedHashMap<OutputOption, OutputParams>();
	public LinkedHashMap<OutputOption, OutputParams> disabledOptions = new LinkedHashMap<OutputOption, OutputParams>();


	public static void main(String[] args) throws FileNotFoundException, SecurityException, IOException {
		SettingsLoader.loadSettings(false);
		
	}
	
	public List<Channel> getChannels() {
		List<Integer> chanNums = new ArrayList<Integer>(channelMap.keySet());
		chanNums.sort(null);
		List<Channel> list = new ArrayList<Channel>();
		for (Integer i : chanNums) {
			list.add(channelMap.get(i));
		}
		return list;
	}

	protected Settings() {} // restrict access
	
	public static class SettingsLoader {

		private static final String keyChanMap = "ChannelMapping";
		private static final String keyOutputLocation = "OutputLocation";
		private static final String keyChansToProcess = "ChannelsToProcess";
		private static final String keyChanDraw = "RoiPrimaryDrawChan";
		private static final String keyRecentOpenLocations = "RecentOpens";
		private static final String keyRecentAnalysisOpenLocations = "RecentAnalysisOpen";
		private static final String keyBinEnabled = "BinningEnabled";
		private static final String keyBinDrawLabels = "BinningDrawLabels";
		private static final String keyBinNum = "BinningNumber";
		private static final String keyBinExclude = "BinningExcludeOutliers";
		private static final String keyBinIncludeNear = "BinningIncludeNearest";
		private static final String keyProcessingThreshMin = "MinimumThreshold";
		private static final String keyProcessingUnsharpMaskRadius = "UnsharpMaskRadius";
		private static final String keyProcessingUnsharpMaskWeight = "UnsharpMaskWeight";
		private static final String keyProcessingGaussianSigma = "GaussianSigma";
		private static final String keyCalibrations = "Calibrations";
		private static final String keySelectedCalibration = "SelectedCalibration";
		private static final String keyOutputOptionsEnabled = "OutputOptionsEnabled";
		private static final String keyOutputOptionsDisabled = "OutputOptionsDisabled";



		public static final String settingsFilePath = "Neuronal_Migration_Resources" + File.separator + "settings.yml";
		public static final String settingsMainPath = "Neuronal_Migration_Resources";

		// NOTE: Does not currently supporting nesting.
		@SuppressWarnings("unchecked")
		public static Settings loadSettings(boolean forceLoadDefault) throws FileNotFoundException, SecurityException, IOException {

			//ClassLoader classLoader = Settings.class.getClassLoader();
			
			//File defaultSettings = new File(classLoader.getResource("default_settings.yml").getFile());
			InputStream defaultDataSource = /*new FileInputStream(defaultSettings)*/Settings.class.getClassLoader().getResourceAsStream("default_settings.yml");
			LinkedHashMap<String, Object> defaultData = (LinkedHashMap<String, Object>) (new Yaml(new SafeConstructor())).load(defaultDataSource);

			LinkedHashMap<String, Object> customData = null;
			boolean changed = false;
			if (!forceLoadDefault) {
				if (new File(settingsFilePath).exists()) {
					InputStream customDataSource = new FileInputStream(settingsFilePath);
					customData = (LinkedHashMap<String, Object>) (new Yaml(new SafeConstructor())).load(customDataSource);

				} else {
					changed = true;
				}
			}

			LinkedHashMap<String, Object> dataToParse = null;
			if (customData != null) {
				LinkedHashMap<String, Object> potentiallyChangedMerge = _merge(customData, defaultData);
				if (potentiallyChangedMerge != null) {
					dataToParse = potentiallyChangedMerge;
					changed = true;
				} else {
					dataToParse = customData;

				}
			} else {
				dataToParse = defaultData;

			}

			Settings settings = new Settings();
			settings.needsUpdate = changed;

			// Channel map
			List<String> list = (List<String>) dataToParse.get(keyChanMap);
			for (String channelMappingString : list) {
				String[] channelMap = channelMappingString.split(":");
				if (!channelMap[1].equals("None")) {
					settings.channelMap.put(Integer.valueOf(channelMap[0]), Channel.parse(channelMap[1]));
				}
			}
			
			// Channel select
			List<String> listChansProcess = (List<String>) dataToParse.get(keyChansToProcess);
			settings.channelsToProcess = new ArrayList<Channel>();
			for (String channelMappingString : listChansProcess) {
				settings.channelsToProcess.add(Channel.parse(channelMappingString));
			}
			
			// Roi draw
			settings.primaryRoiDrawChannel = Channel.parse((String) dataToParse.get(keyChanDraw));

			// Output location
			String path = (String) dataToParse.get(keyOutputLocation);
			if (path.equals("None")) {
				settings.outputLocation = null;
			} else {
				File file = new File(path);
				if (file.exists()) {
					settings.outputLocation = file;
				} else {
					settings.outputLocation = null;
					settings.needsUpdate = true;
				}
			}

			// Recent open locations
			List<String> listOpens = (List<String>) dataToParse.get(keyRecentOpenLocations);
			List<File> listOpensFiles = new ArrayList<File>();
			for (String recentLocation : listOpens) {
				File file = new File(recentLocation);
				if (file.exists()) {
					listOpensFiles.add(file);
				} else {
					settings.needsUpdate = true;
				}
			}
			settings.recentOpenFileLocations = listOpensFiles;

			// Recent analysis open locations
			List<String> listAnalysisOpens = (List<String>) dataToParse.get(keyRecentAnalysisOpenLocations);
			List<File> listAnalysisOpensFiles = new ArrayList<File>();
			for (String recentLocation : listAnalysisOpens) {
				File file = new File(recentLocation);
				if (file.exists()) {
					listAnalysisOpensFiles.add(file);
				} else {
					settings.needsUpdate = true;
				}
			}
			settings.recentOpenAnalysisOutputLocations = listAnalysisOpensFiles;
			
			// Bins
			settings.calculateBins = (boolean) dataToParse.get(keyBinEnabled);
			settings.drawBinLabels = (boolean) dataToParse.get(keyBinDrawLabels);
			settings.numberOfBins = (Integer) dataToParse.get(keyBinNum);
			settings.excludePtsOutsideBin = (boolean) dataToParse.get(keyBinExclude);
			settings.includePtsNearestBin = (boolean) dataToParse.get(keyBinIncludeNear);
			
			// Processing
			settings.processingMinThreshold = (Integer) dataToParse.get(keyProcessingThreshMin);
			settings.processingUnsharpMaskRadius = (Integer) dataToParse.get(keyProcessingUnsharpMaskRadius);
			settings.processingUnsharpMaskWeight = (Double) dataToParse.get(keyProcessingUnsharpMaskWeight);
			settings.processingGaussianSigma = (Double) dataToParse.get(keyProcessingGaussianSigma);

			// Calibrations
			settings.calibrations = (List<String>) dataToParse.get(keyCalibrations);
			settings.calibrationNumber = (Integer) dataToParse.get(keySelectedCalibration);
			
			// OutputOptions
			Map<OutputOption, OutputParams> enabledOptionsTemp = new HashMap<OutputOption, OutputParams>();
			for (String outputOptionString : (List<String>) dataToParse.get(keyOutputOptionsEnabled)) {
				OutputOption option = OutputOption.fromCondensed(outputOptionString.substring(0, outputOptionString.indexOf("(")));
				if (option == null)
					throw new NullPointerException("Invalid Settings Configuration");
				OutputParams outputOption = new OutputParams(option);
				String chanString = outputOptionString.substring(outputOptionString.indexOf("(") + 1, outputOptionString.indexOf(")"));
				if (chanString != null && !chanString.equals("")) {
					for (char c : chanString.toCharArray()) {
						Channel chan = Channel.getChannelByAbbreviation(c + "");
						if (chan == null) {
							settings.needsUpdate = true;
						} else {
							outputOption.addChannel(chan);
						}
					}

				}
				enabledOptionsTemp.put(option, outputOption);
			}
			Map<OutputOption, OutputParams> disabledOptionsTemp = new HashMap<OutputOption, OutputParams>();
			for (String outputOptionString : (List<String>) dataToParse.get(keyOutputOptionsDisabled)) {
				OutputOption option = OutputOption.fromCondensed(outputOptionString.substring(0, outputOptionString.indexOf("(")));
				if (option == null)
					throw new NullPointerException("Invalid Settings Configuration");
				OutputParams outputOption = new OutputParams(option);
				String chanString = outputOptionString.substring(outputOptionString.indexOf("(") + 1, outputOptionString.indexOf(")"));
				if (chanString != null && !chanString.equals("")) {
					for (char c : chanString.toCharArray()) {
						Channel chan = Channel.getChannelByAbbreviation(c + "");
						if (chan == null) {
							settings.needsUpdate = true;
						} else {
							outputOption.addChannel(chan);
						}
					}

				}
				disabledOptionsTemp.put(option, outputOption);
			}
			for (OutputOption option : OutputOption.values()) {
				if (enabledOptionsTemp.containsKey(option)) {
					settings.enabledOptions.put(option, enabledOptionsTemp.get(option));
				} else if (disabledOptionsTemp.containsKey(option)) {
					settings.disabledOptions.put(option, disabledOptionsTemp.get(option));
				} else {
					settings.disabledOptions.put(option, new OutputParams(option));
					settings.needsUpdate = true;
				}
			}

			
			return settings;


		}

		public static boolean saveSettings(Settings settings) {
			LinkedHashMap<String, Object> newSettings = new LinkedHashMap<String, Object>();
			List<String> chanMapString = new ArrayList<String>();
			for (Entry<Integer, Channel> en : settings.channelMap.entrySet()) {
				chanMapString.add(en.getKey() + ":" + en.getValue().toReadableString());
			}
			newSettings.put(keyChanMap, chanMapString);
			if (settings.outputLocation != null) {
				newSettings.put(keyOutputLocation, settings.outputLocation.getPath());
			} else {
				newSettings.put(keyOutputLocation, "None");
			}
			List<String> chansToProcess = new ArrayList<String>();
			for (Channel chan : settings.channelsToProcess) {
				chansToProcess.add(chan.toReadableString());
			}
			newSettings.put(keyChansToProcess, chansToProcess);
			
			// Roi primary
			newSettings.put(keyChanDraw, settings.primaryRoiDrawChannel.toReadableString());

			// Recent open locations
			List<String> listOpens = new ArrayList<String>();
			for (File recentLocation : settings.recentOpenFileLocations) {
				listOpens.add(recentLocation.getPath());
			}
			newSettings.put(keyRecentOpenLocations, listOpens);

			// Recent analysis open locations
			List<String> listAnalysisOpens = new ArrayList<String>();
			for (File recentLocation : settings.recentOpenAnalysisOutputLocations) {
				listOpens.add(recentLocation.getPath());
			}
			newSettings.put(keyRecentAnalysisOpenLocations, listAnalysisOpens);
			
			// bins
			newSettings.put(keyBinEnabled, settings.calculateBins);
			newSettings.put(keyBinDrawLabels, settings.drawBinLabels);
			newSettings.put(keyBinNum, settings.numberOfBins);
			newSettings.put(keyBinExclude, settings.excludePtsOutsideBin);
			newSettings.put(keyBinIncludeNear, settings.includePtsNearestBin);

			// Processing
			newSettings.put(keyProcessingThreshMin, settings.processingMinThreshold);
			newSettings.put(keyProcessingUnsharpMaskRadius, settings.processingUnsharpMaskRadius);
			newSettings.put(keyProcessingUnsharpMaskWeight, settings.processingUnsharpMaskWeight);
			newSettings.put(keyProcessingGaussianSigma, settings.processingGaussianSigma);
			
			// Calibration
			newSettings.put(keyCalibrations, settings.calibrations);
			newSettings.put(keySelectedCalibration, settings.calibrationNumber);
			
			// Settings
			List<String> outputOptionEnabledStrings = new ArrayList<String>();
			List<String> outputOptionDisabledStrings = new ArrayList<String>();
			for (Entry<OutputOption, OutputParams> en : settings.enabledOptions.entrySet()) {
				StringBuilder sb = new StringBuilder();
				sb.append("");
				String delim = "";
				for (Channel chan : en.getValue().includedChannels) {
					sb.append(delim).append(chan.getAbbreviation());
					delim = ",";
				}
				outputOptionEnabledStrings.add(en.getKey().getCondensed() + "(" + sb.toString() + ")");
			}
			for (Entry<OutputOption, OutputParams> en : settings.disabledOptions.entrySet()) {
				StringBuilder sb = new StringBuilder();
				sb.append("");
				String delim = "";
				for (Channel chan : en.getValue().includedChannels) {
					sb.append(delim).append(chan.getAbbreviation());
					delim = ",";
				}
				outputOptionDisabledStrings.add(en.getKey().getCondensed() + "(" + sb.toString() + ")");
			}
			newSettings.put(keyOutputOptionsEnabled, outputOptionEnabledStrings);
			newSettings.put(keyOutputOptionsDisabled, outputOptionDisabledStrings);

		    DumperOptions options = new DumperOptions();
		    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		    Yaml yaml = new Yaml(options);
		    String output = yaml.dump(newSettings);

		    BufferedWriter writer = null;
			try {
				File file1 = new File(settingsMainPath);
				file1.mkdir();
				File file = new File(settingsFilePath);
				file.createNewFile();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write(output);
				writer.close();
			    
			} catch (IOException e) {
				e.printStackTrace();
			    return false;
			} finally {

			}
			return true;
		}

		private static LinkedHashMap<String, Object> _merge(LinkedHashMap<String, Object> query, LinkedHashMap<String, Object> reference) {

			LinkedHashMap<String, Object> newMap = new LinkedHashMap<String, Object>();
			boolean hasChanges = false;

			Iterator<Entry<String, Object>> referenceItr = reference.entrySet().iterator();

			while (referenceItr.hasNext()) {
				Entry<String, Object> referenceEn = referenceItr.next();

				Object valueSomewhere = query.get(referenceEn.getKey());
				if (valueSomewhere == null || !_looselyTestEquality(valueSomewhere, referenceEn.getValue())) {
					newMap.put(referenceEn.getKey(), referenceEn.getValue());
					hasChanges = true;
				} else {
					newMap.put(referenceEn.getKey(), valueSomewhere);
				}			

			}

			if (!hasChanges) {
				// ensure correct order if aren't changes (if there are changes, we force set order anyways)

				referenceItr = reference.entrySet().iterator();
				Iterator<Entry<String, Object>> queryItr = query.entrySet().iterator();

				while (referenceItr.hasNext() || queryItr.hasNext()) {
					if (queryItr.hasNext() ^ queryItr.hasNext()) {
						hasChanges = true;
						break;
					}
					if (!referenceItr.next().getKey().equals(queryItr.next().getKey())) {
						hasChanges = true;
						break;
					}
				}
			}


			if (hasChanges)
				return newMap;
			else
				return null;

		}

		private static boolean _looselyTestEquality(Object object1, Object object2) {
			if (object1 == null || object2 == null)
				return false;

			if (object1 instanceof List) {
				return object2 instanceof List;
			} else if (object1 instanceof Boolean) {
				return object2 instanceof Boolean;
			} else if (object1 instanceof Number) {
				return object2 instanceof Number;
			} else {
				return object2 instanceof String;
			}
		}


	}

}


