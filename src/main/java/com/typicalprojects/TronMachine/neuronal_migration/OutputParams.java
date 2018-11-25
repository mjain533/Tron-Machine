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

import java.util.ArrayList;
import java.util.List;

import com.typicalprojects.TronMachine.util.ImageContainer.Channel;

public class OutputParams {
	
	public List<Channel> includedChannels = new ArrayList<Channel>();
	OutputOption option;
	
	public OutputParams(OutputOption option) {
		this.option = option;
	}
	
	public void addChannel(Channel c) {
		includedChannels.add(c);
		includedChannels.sort(null);
	}
	
	public void removeChannel(Channel c) {
		includedChannels.remove(c);
	}
	
	
}
