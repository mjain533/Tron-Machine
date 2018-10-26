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
