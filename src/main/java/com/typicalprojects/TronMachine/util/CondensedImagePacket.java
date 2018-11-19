package com.typicalprojects.TronMachine.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.typicalprojects.TronMachine.neuronal_migration.OutputOption;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;

import ij.ImagePlus;
import ij.measure.Calibration;

public class CondensedImagePacket {
	
	public ImageContainer ips = null;
	
	public CondensedImagePacket() {
		
	}
	
	/*private void writeObject(ObjectOutputStream stream)
			throws IOException {
		
		stream.defaultWriteObject();
		stream.writeInt(ips.size());
		for (ImagePlus ip : ips) {
			stream.writeDouble();
		}
		stream.writeDouble(cal.pixelWidth);
		stream.writeDouble(cal.pixelHeight);
		int numImages = 0;
		for (Map<Channel, ImagePlus> images : this.images.values()) {
			numImages = numImages + images.size();
		}
		stream.writeInt(numImages);
		for (Entry<OutputOption, Map<Channel,ImagePlus>> imagesEn : this.images.entrySet()) {
			for (Entry<Channel, ImageP>)
			stream.writeChars(imagesEn.getKey().getCondensed());
			stream.writeObject(imagesEn.);
			stream.write
		}
		
	}

	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		this.cal = new Calibration();
		this.cal.xOrigin = 0;
		this.cal.yOrigin = 0;
		this.cal.pixelWidth = stream.readDouble();
		this.cal.pixelHeight = stream.readDouble();
		this.timeOfRun = GUI.dateString;
		this.outputLocation = GUI.settings.outputLocation;
		this.imageFile = null;
	}*/
	
}
