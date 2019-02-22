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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;

public class OutputParams implements Serializable {

	private static final long serialVersionUID = -7386693999079593633L;
	private Set<Channel> includedChannels = new HashSet<Channel>();
	private OutputOption option;
	
	public OutputParams(OutputOption option) {
		this.option = option;
	}
	
	public OutputOption getOption() {
		return this.option;
	}
	
	/**
	 * @return channels in this instance; changes are backed (but NOT validated, this needs to be done with the channel manager)
	 */
	protected Set<Channel> getContainedChannels() {
		// If this is changed to return only a copy, this WILL affect behavior in channel manager which DIRECTLY
		// edits this list backed by this instance.
		return includedChannels;
	}
	
	protected void setContainedChannels(Set<Channel> channels) {
		this.includedChannels = channels;
	}
	
	protected void removeChannel(Channel chan) {
		this.includedChannels.remove(chan);
	}
	
	protected void addChannel(Channel chan) {
		this.includedChannels.add(chan);
	}
	
	protected boolean contains(Channel chan) {
		return this.includedChannels.contains(chan);
	}

	protected String concatChanAbbrevs() {
		StringBuilder sb = new StringBuilder();
		sb.append("");
		for (Channel chan : includedChannels) {
			sb.append(chan.getAbbrev());
		}
		return sb.toString();
	}
	
	protected OutputParams cloneLightly(ChannelManager cm) {
		OutputParams op = new OutputParams(this.option);
		op.includedChannels.addAll(cm.getEquivalentChannels(includedChannels));
		return op;
	}
	
	/**
	 * @return the HTML version. Is in form: <display_name_output_option> ( <channel_abbreviations_colored> )
	 */
	public String toString() {
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append(option.getDisplay());
			sb.append(" ( ");
			String delim = "";
			for (Channel chan : this.includedChannels) {
				sb.append(delim).append("<font color='").append(chan.getHtmlColor()).append("'>").append(chan.getAbbrev() + "").append("</font>");
				delim = ",";
			}
			sb.append(" )</html>");
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		

	}
	
}
