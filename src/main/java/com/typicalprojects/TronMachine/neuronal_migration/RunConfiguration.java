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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;

public class RunConfiguration implements Serializable {
	
	private static final long serialVersionUID = 7663254682478791055L; // For serialization
	public transient Map<Integer, ImageContainer.Channel> channelMap = null;
	public transient List<Channel> channelsToProcess = null;
	public transient Channel primaryRoiDrawChannel = null;
	
	protected RunConfiguration(Settings settings) {
		this.channelMap = settings.channelMap;
		this.channelsToProcess = settings.channelsToProcess;
		this.primaryRoiDrawChannel = settings.primaryRoiDrawChannel;
	}
	
	private void writeObject(ObjectOutputStream stream)
			throws IOException {
		
		// NOTE: this may need to be adjusted for future updates.
		
		stream.defaultWriteObject();
		Map<Integer, String> channelMapStrings = new HashMap<Integer,String>();
		for (Entry<Integer, Channel> en : this.channelMap.entrySet()) {
			channelMapStrings.put(en.getKey(), en.getValue().toReadableString());
		}
		stream.writeObject(channelMapStrings);
		List<String> channelToProcessString = new ArrayList<String>();
		for (Channel chan : this.channelsToProcess) {
			channelToProcessString.add(chan.toReadableString());
		}
		stream.writeObject(channelToProcessString);
		stream.writeObject(this.primaryRoiDrawChannel.toReadableString());

		
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		
		// NOTE: this may need to be adjusted for future updates.

		stream.defaultReadObject();
		Map<Integer, String> channelMapStrings = (Map<Integer,String>) stream.readObject();
		this.channelMap = new HashMap<Integer, Channel>();
		for (Entry<Integer, String> en : channelMapStrings.entrySet()) {
			this.channelMap.put(en.getKey(), Channel.parse(en.getValue()));
		}
		List<String> channelToProcessString = (List<String>) stream.readObject();
		this.channelsToProcess = new ArrayList<Channel>();
		for (String string : channelToProcessString) {
			this.channelsToProcess.add(Channel.parse(string));
		}
		this.primaryRoiDrawChannel = Channel.parse((String) stream.readObject());
	}
	
}
