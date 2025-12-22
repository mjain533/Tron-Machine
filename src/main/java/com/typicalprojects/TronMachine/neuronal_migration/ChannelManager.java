package com.typicalprojects.TronMachine.neuronal_migration;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlDisplay.PnlDisplayPage;
import com.typicalprojects.TronMachine.popup.Displayable;

public class ChannelManager implements Serializable, Cloneable {

	private static final long serialVersionUID = 7640873903984537264L;
	private final Map<String, Channel> currentChannels = new HashMap<String, Channel>();
	private final Map<Channel, Integer> indexMap = new HashMap<Channel, Integer>();
	
	// We need synchronization for anything involving these maps, because they're not internally async.
	private final LinkedHashMap<OutputOption, OutputParams> enabledOptions = new LinkedHashMap<OutputOption, OutputParams>();
	private final LinkedHashMap<OutputOption, OutputParams> disabledOptions = new LinkedHashMap<OutputOption, OutputParams>();

	private List<Channel> processChans = new ArrayList<Channel>();
	private Channel primaryROIDrawChan = null;
	private transient boolean hasChangesInOutputOptions = false;

	public ChannelManager() {}

	public synchronized Channel addChannel(String name, String abbrev, Color imgColor, Color txtColor) throws IllegalArgumentException {

		Channel newChan = new Channel(name, abbrev, imgColor, txtColor, this);
		currentChannels.put(name.toLowerCase(), newChan);
		indexMap.put(newChan, -1);
		return newChan;
	}

	public synchronized void removeChan(Channel chan) {
		Iterator<Entry<String, Channel>> chanItr = currentChannels.entrySet().iterator();
		while (chanItr.hasNext()) {
			if (chanItr.next().getValue().equals(chan)) {
				chanItr.remove();
			}
		}
		indexMap.remove(chan);
		processChans.remove(chan);
		if (primaryROIDrawChan != null && chan.equals(primaryROIDrawChan)) {
			primaryROIDrawChan = null;
		}
		
		for (OutputParams param : this.enabledOptions.values()) {
			param.removeChannel(chan);
		}
		for (OutputParams param : this.disabledOptions.values()) {
			param.removeChannel(chan);
		}
	}
	
	public int getNumberOfChannels(boolean includeInactive) {
		if (includeInactive) {
			return this.currentChannels.size();
		} else {
			Collection<Integer> channelIndices = new HashSet<Integer>(this.indexMap.values());
			channelIndices.remove(new Integer(-1));
			return channelIndices.size();
		}
	}
	
	protected void correctOutputParams() {
		for (Entry<OutputOption, OutputParams> en : this.enabledOptions.entrySet()) {
			Iterator<Channel> chanItr = en.getValue().getContainedChannels().iterator();
			while (chanItr.hasNext()) {
				Channel chan = chanItr.next();
				if (getEquivalentChannel(chan) == null || !this.indexMap.containsKey(chan) || this.indexMap.get(chan) == -1) {
					chanItr.remove();
				}
			}
		}
		for (Entry<OutputOption, OutputParams> en : this.disabledOptions.entrySet()) {
			Iterator<Channel> chanItr = en.getValue().getContainedChannels().iterator();
			while (chanItr.hasNext()) {
				Channel chan = chanItr.next();
				if (getEquivalentChannel(chan) == null || !this.indexMap.containsKey(chan) || this.indexMap.get(chan) == -1) {
					chanItr.remove();
				}
			}
		}
	}
	
	public String validate() {
		Set<Integer> containedInts = new HashSet<Integer>();		
		
		for (Integer i : this.indexMap.values()) {
			containedInts.add(i);
		}

		containedInts.remove(-1);
		if (containedInts.isEmpty()) {
			return "There must be at least one channel with # ≥ 0";
		}
		for (Integer i = 0; i < containedInts.size(); i++) {
			if (!containedInts.contains(i)) {
				return "The channel # cannot skip integers";
			}
		}
		
		if (this.primaryROIDrawChan == null)
			return "A channel for ROI drawing must be set";
		else if (this.processChans.isEmpty())
			return "At least one channel must be set for processing";
		else {
			Integer i = this.indexMap.get(this.primaryROIDrawChan);
			if (i == null || i == -1) {
				return "The channel for ROI draw must be assigned a #";
			}
			for (Channel chan : this.processChans) {
				i = this.indexMap.get(chan);
				if (i == null || i == -1) {
					return "All channels for processing must be assigned a #";
				}
			}
		}
		
		return null;
	}
	
	public synchronized boolean isActiveChannel(Channel chan) {
		
		Channel thisChan = parse(chan.getName());
		if (thisChan == null)
			return false;
		
		if (!this.indexMap.containsKey(thisChan))
			return false;
		
		return indexMap.get(thisChan) != -1;
	}
	
	
	
	public synchronized Channel getChannel(String name) {
		return parse(name);
	}
	
	public synchronized Channel getEquivalentChannel(Channel chan) {
		return parse(chan.getName());
	}
	
	public synchronized List<Channel> getEquivalentChannels(List<Channel> chans) {
		List<Channel> newChannels = new ArrayList<Channel>();
		for (Channel chan : chans) {
			newChannels.add(getEquivalentChannel(chan));
		}
		return newChannels;
	}
	
	public synchronized Set<Channel> getEquivalentChannels(Set<Channel> chans) {
		Set<Channel> newChannels = new HashSet<Channel>();
		for (Channel chan : chans) {
			newChannels.add(getEquivalentChannel(chan));
		}
		return newChannels;
	}

	public synchronized Channel parse(String query) {

		Channel chan = currentChannels.get(query.toLowerCase());
		if (chan != null)
			return chan;

		if (query.length() == 1) {
			char c = query.charAt(0);
			for (Channel prospectiveChan : currentChannels.values()) {
				if (prospectiveChan.abbrev == c)
					return prospectiveChan;
			}
		}
		return null;

	}
	
	/**
	 * Returns the mapped index of a channel. It is NOT guaranteed to be accurate unless first verified by
	 * the <code>validate()</code> method. If the channel passed does not exist in thie channel manager or
	 * there is not index associated with thie channel, -1 is returned. -1 indicates no mapped channel.
	 * 
	 * @param chan the channel whose index should be received
	 * @return index of this channel.
	 */
	public synchronized int getMappedIndex(Channel chan) {
		Channel thisChan = parse(chan.getName());
		if (thisChan == null || !this.indexMap.containsKey(thisChan))
			return -1;
		
		return indexMap.get(thisChan);
	}

	/**
	 * Sets the index of a channel. This does NOT validate the index passed. Two channels could be assigned
	 * the same index. To validate indices, use <code>validate()</code>
	 * 
	 * @param chan the channel whose index should be set
	 * @param index the index to set.
	 * @throws IllegalArgumentException if the channel doesn't exist
	 */
	public synchronized void setMappedIndex(Channel chan, int index) {
		
		Channel thisChan = parse(chan.getName());
		if (thisChan == null)
			throw new IllegalArgumentException();
		
		if (index < -1)
			index = -1;
		indexMap.put(thisChan, index);
	}
	
	private synchronized boolean adjustChannelName(String oldName, String newName) {
		oldName = oldName.toLowerCase();
		newName = newName.toLowerCase();
		if (oldName.equals(newName))
			return true;
		else if (parse(newName) != null)
			return false;
		
		Channel chan = this.currentChannels.remove(oldName);
		if (chan == null)
			return true;
		
		this.currentChannels.put(newName, chan);
		return true;
		
	}
	
	private synchronized boolean canAdjustAbbrev(Channel chan, char abbrev) {
		
		if (chan.abbrev == abbrev)
			return true;
		
		Channel existingChan = parse(abbrev + "");
		
		if (!existingChan.equals(chan))
			return false;
		else
			return true;
		
	}

	public synchronized Map<Channel, Integer> getChannelIndices() {
		Map<Channel, Integer> mapCopy = new HashMap<Channel, Integer>();
		mapCopy.putAll(this.indexMap);
		return mapCopy;

	}
	
	/**
	 * @return copy of active channels, ordered via their index.
	 */
	public synchronized List<Channel> getOrderedActiveChannels() {

		List<Integer> chanNums = new ArrayList<Integer>(new HashSet<Integer>(this.indexMap.values()));
		chanNums.sort(null);
		List<Channel> list = new ArrayList<Channel>();
		for (Integer i : chanNums) {
			for (Entry<Channel, Integer> en : indexMap.entrySet()) {
				if (i > -1 && en.getValue().equals(i)) {
					list.add(en.getKey());
				}
			}
		}
		return list;

	}
	
	/**

	 * @return copy of all channels, ordered via their index.
	 */
	public synchronized List<Channel> getOrderedChannels() {
		List<Integer> chanNums = new ArrayList<Integer>(this.indexMap.values());
		
		while (chanNums.remove(new Integer(-1))) {}
		
		chanNums.sort(null);
		List<Channel> list = new ArrayList<Channel>();
		for (Integer i : chanNums) {

			for (Entry<Channel, Integer> en : indexMap.entrySet()) {
				if (en.getValue().equals(i)) {
					list.add(en.getKey());
					break;
				}
			}
		}
		for (Entry<Channel, Integer> en : indexMap.entrySet()) {
			if (en.getValue().equals(-1)) {
				list.add(en.getKey());
			}
		}
		return list;
	}
	
	/**
	 * @return copy of all channels (including inactive), not ordered via their index.
	 */
	public synchronized Set<Channel> getUnorderedChannels() {
		return new HashSet<Channel>(this.currentChannels.values());
	}
	
	/**
	 * @return copy of active channels, in no particular ordered.
	 */
	public synchronized Set<Channel> getUnorderedActiveChanCopy() {
		Set<Channel> chans = new HashSet<Channel>();
		for (Entry<Channel, Integer> en : this.indexMap.entrySet()) {
			if (en.getValue() != -1)
				chans.add(en.getKey());
		}
		return chans;
	}
	
	/**
	 * Analogous to <code>getUnorderedChannels()</code>
	 */
	public synchronized Set<Channel> getChannels() {
		return getUnorderedActiveChanCopy();
	}
	
	public synchronized void addOutputOption(OutputOption output, OutputParams param, boolean enabled) {
		if (enabled) {
			this.enabledOptions.put(output, param);
		} else {
			this.disabledOptions.put(output, param);
		}
	}
	
	public synchronized void setOutputOptionEnabled(OutputOption output, OutputParams param, boolean enabled) {
		if (enabled) {
			this.disabledOptions.remove(output);
			this.enabledOptions.put(output, param);
		} else {
			this.enabledOptions.remove(output);
			this.disabledOptions.put(output, param);
		}
		this.hasChangesInOutputOptions = true;
	}
	
	/**
	 * Sets the channels for this output param
	 * 
	 * @param param for setting channel
	 * @param chans Channels to set.
	 * @throws IllegalArgumentException if a channel that is passed doesn't exist
	 */
	public synchronized void setOutputParamChannels(OutputParams param, Set<Channel> chans) {
		for (Channel chan : chans) {
			if (getEquivalentChannel(chan) == null)
				throw new IllegalArgumentException("Channel doesn't exist.");
		}
		
		this.hasChangesInOutputOptions = true;
		param.setContainedChannels(chans);

	}
	
	public synchronized List<Channel> getOutputParamChannels(OutputParams param) {
		return new ArrayList<Channel>(param.getContainedChannels());
	}
	
	protected synchronized LinkedHashMap<OutputOption, OutputParams> getCopyOfOutputParamMapping(boolean enabledParams) {
		if (enabledParams) {
			return new LinkedHashMap<OutputOption, OutputParams>(this.enabledOptions);
		} else {
			return new LinkedHashMap<OutputOption, OutputParams>(this.disabledOptions);
		}
	}
	
	public synchronized boolean hasOutput(OutputOption option, Channel chan) {
		return this.enabledOptions.containsKey(option) && this.enabledOptions.get(option).contains(chan);
	}
	
	public synchronized boolean hasOutput(OutputOption option) {
		return this.enabledOptions.containsKey(option);
	}
	
	public synchronized boolean hasOutput(Collection<OutputOption> options, Channel chan) {
		for (OutputOption option : options) {
			if (hasOutput(option, chan))
				return true;
		}
		return false;
	}
	
	public synchronized boolean hasChangesInOutputParams() {
		
		return this.hasChangesInOutputOptions;
	}
	
	public synchronized void setChangesInOutputParams(boolean changes) {
		this.hasChangesInOutputOptions = changes;
	}
	
	public synchronized void setProcessChannels(List<Channel> chans) {
		if (chans ==null)
			chans = new ArrayList<Channel>();
		for (Channel chan : chans) {
			if (this.currentChannels.values().contains(chan))
				throw new IllegalArgumentException();
		}
		this.processChans = chans;
	}
	
	/**
	 * @return a copy of the channels to process
	 */
	public synchronized List<Channel> getProcessChannels() {
		return new ArrayList<Channel>(this.processChans);
	}
	
	/**
	 * @param chan The channel to modify
	 * @param shouldProcess true if the channel should be processed for neurons
	 * @throws IllegalArugmentException if the chan object doesn't exist in this channel manager.
	 */
	public synchronized void setProcessChannel(Channel chan, boolean shouldProcess) {
		if (shouldProcess) {
			
			if (!this.currentChannels.values().contains(chan))
				throw new IllegalArgumentException();
			
			if (!this.processChans.contains(chan))
				this.processChans.add(chan);
		} else {
			this.processChans.remove(chan);
		}
	}
	
	public synchronized boolean isProcessChannel(Channel chan) {
		return this.processChans.contains(chan);
	}
	
	public synchronized void setPrimaryROIDrawChan(Channel chan) {
		if (!this.currentChannels.values().contains(chan))
			throw new IllegalArgumentException();
		
		this.primaryROIDrawChan = chan;
	}
	
	public synchronized Channel getPrimaryROIDrawChan() {
		return this.primaryROIDrawChan;
	}

	private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
		
		// Have to do this for intermediate, may load up a file which doesn't have all the output params
		stream.defaultReadObject();
		for (OutputOption option : OutputOption.values()) {
			if (!this.enabledOptions.containsKey(option) && !this.disabledOptions.containsKey(option))
				this.disabledOptions.put(option, new OutputParams(option));
		}
		
		Iterator<OutputOption> opItr = this.enabledOptions.keySet().iterator();
		while (opItr.hasNext()) {
			OutputOption curr = opItr.next();
			for (OutputOption value : OutputOption.values()) {
				if (value.equals(curr)) {
					curr = null;
					break;
				}
			}
			if (curr != null) {
				opItr.remove();
			}
		}
		
		opItr = this.disabledOptions.keySet().iterator();
		while (opItr.hasNext()) {
			OutputOption curr = opItr.next();
			for (OutputOption value : OutputOption.values()) {
				if (value.equals(curr)) {
					curr = null;
					break;
				}
			}
			if (curr != null) {
				opItr.remove();
			}
		}
		this.hasChangesInOutputOptions = false;
    }
	
	@Override
	public synchronized ChannelManager clone() {
		ChannelManager cm = new ChannelManager();
		for (Entry<String, Channel> en : this.currentChannels.entrySet()) {
			Channel newchan = en.getValue().clone();
			newchan.setManager(cm);
			cm.currentChannels.put(en.getKey(), newchan);
		}
		for (Entry<Channel, Integer> en : this.indexMap.entrySet()) {
			cm.indexMap.put(cm.getEquivalentChannel(en.getKey()), en.getValue());
		}
		cm.primaryROIDrawChan = cm.getEquivalentChannel(this.primaryROIDrawChan);
		cm.processChans = cm.getEquivalentChannels(this.processChans);
		
		for (Entry<OutputOption, OutputParams> en : this.enabledOptions.entrySet()) {
			cm.enabledOptions.put(en.getKey(), en.getValue().cloneLightly(cm));
		}
		for (Entry<OutputOption, OutputParams> en : this.disabledOptions.entrySet()) {
			cm.disabledOptions.put(en.getKey(), en.getValue().cloneLightly(cm));
		}
		
		return cm;
	}
	
	/**
	 * Determines whether the other channel manager has identical channels (all fields are the same), and
	 * if the channels set for processing and ROI select are also the same.
	 * 
	 * @param cm the other Channel Manager
	 * @return true if above criterion is met
	 */
	public synchronized boolean hasIdenticalChannels(ChannelManager cm) {
		return this.currentChannels.equals(cm.currentChannels) && this.indexMap.equals(cm.indexMap) && 
				this.primaryROIDrawChan.equals(cm.primaryROIDrawChan) && this.processChans.equals(cm.processChans);
		
	}
	
	
	public static class Channel implements Displayable, Serializable, Cloneable, PnlDisplayPage {
		
		private static final long serialVersionUID = -5782207945664373884L;
		private String name;
		private char abbrev;
		private String htmlColor;
		private Color imgColor;
		private Color txtColor;
		private ChannelManager cm;

		private Channel(String name, String abbrev, Color imgColor, Color txtColor, ChannelManager cm) {
			
			if (name == null || name.length() < 2) {
				throw new IllegalArgumentException("Channel name must be at least 2 characters!");
			} else if (abbrev == null || abbrev.length() != 1) {
				throw new IllegalArgumentException("Channel abbreviation must be 1 character in length!");
			} else if (!StringUtils.isAlphanumeric(name)) {
				throw new IllegalArgumentException("Channel name must be alphanumeric!");
			} else if (!StringUtils.isAlphanumeric(abbrev)) {
				throw new IllegalArgumentException("Channel abbreviation must be alphanumeric!");
			} else if (imgColor == null) {
				throw new IllegalArgumentException("Channel image color cannot be none!");
			} else if (txtColor == null) {
				throw new IllegalArgumentException("Channel text color cannot be none!");
			} else if (cm.parse(name) != null) {
				throw new IllegalArgumentException("This name conflicts with an existing channel!");
			} else if (cm.parse(abbrev) != null) {
				throw new IllegalArgumentException("This abbreviation conflicts with an existing channel!");
			}
			
			this.name = name;
			this.abbrev = abbrev.charAt(0);
			this.imgColor = imgColor;
			this.txtColor = txtColor;
			this.htmlColor = "rgb(" + txtColor.getRed() + "," + txtColor.getGreen() + "," + txtColor.getBlue()+ ")";
			this.cm = cm;
			
		}
		
		private void setManager(ChannelManager cm) {
			this.cm = cm;
		}

		public String getName() {
			return this.name;
		}
		
		public void setName(String name) {
			
			if (name == null || name.length() < 2)
				throw new IllegalArgumentException("Channel name must be at least 2 characters!");
			else if (!StringUtils.isAlphanumeric(name))
				throw new IllegalArgumentException("Channel name must be alphanumeric!");
			
			boolean adjusted = this.cm.adjustChannelName(this.name, name);
			
			if (!adjusted) throw new IllegalArgumentException("This name conflicts with an existing channel!");
			else this.name = name;
		}

		@Override
		public String toString() {
			return this.abbrev + "";
		}

		public char getAbbrev() {
			return this.abbrev;
		}
		
		public void setAbbrev(String abbrev) {
			
			if (abbrev == null || abbrev.length() != 1) {
				throw new IllegalArgumentException("Channel abbreviation must be one character in length!");
			} else if (!StringUtils.isAlphanumeric(abbrev))
				throw new IllegalArgumentException("Channel name must be alphanumeric!");
			
			if (cm.canAdjustAbbrev(this, abbrev.charAt(0))) {
				this.abbrev = abbrev.charAt(0);
			} else throw new IllegalArgumentException("This abbreviation conflicts with an existing channel!");
			
		}

		public Color getImgColor() {
			return this.imgColor;
		}
		
		public void setImgColor(Color color) {
			if (color == null)
				throw new IllegalArgumentException("Channel image color cannot be none!");
			this.imgColor = color;
		}
		
		public Color getTxtColor() {
			return this.txtColor;
		}
		
		public void setTxtColor(Color color) {
			if (color == null)
				throw new IllegalArgumentException("Channel text color cannot be none!");
			this.htmlColor = "rgb(" + txtColor.getRed() + "," + txtColor.getGreen() + "," + txtColor.getBlue()+ ")";
			this.txtColor = color;
		}

		public String getHtmlColor() {
			return this.htmlColor;
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		public boolean softEquals(Object other) {
			if (other == null || (!(other instanceof Channel)) ||
					!((Channel) other).name.equals(this.name))
				return false;
			else
				return true;
		}
		
		@Override
		public boolean equals(Object other) { // Do not edit this, need to check ALL FIELDS
			if (other == null || (!(other instanceof Channel)))
				return false;
			
			Channel chan = (Channel) other;
			if (chan.abbrev != this.abbrev || !chan.htmlColor.equals(this.htmlColor) ||
					!chan.imgColor.equals(this.imgColor) || !chan.name.equals(this.name) ||
					!chan.txtColor.equals(this.txtColor))
				return false;
			
			return true;
		}

		@Override
		public String getListDisplayText() {
			return this.name;
		}

		@Override
		public Color getListDisplayColor() {
			return this.txtColor;
		}

		@Override
		public Font getListDisplayFont() {
			return GUI.smallBoldFont;
		}
		
		@Override
		public Channel clone() {
			Channel chan;
			try {
				chan = (Channel) super.clone();
				chan.txtColor = this.txtColor;
				chan.imgColor = this.imgColor;
				chan.cm = this.cm;
				return chan;
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				return null;
			}
			
		}

		@Override
		public String getDisplayAbbrev() {
			return this.abbrev + "";
		}

	}

}
