package com.typicalprojects.TronMachine.neuronal_migration.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.RunConfiguration;
import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;
import com.typicalprojects.TronMachine.util.ResultsTable;

import ij.ImagePlus;
import ij.ImageStack;

public class PreprocessedEditableImage implements Serializable {
	

	private static final long serialVersionUID = 1307922074903665693L;
	private ImageContainer ic;
	private transient GUI gui;
	
	public PreprocessedEditableImage(ImageContainer ic, GUI gui) {
		this.ic = ic;
		this.gui = gui;
	}
	
	public RunConfiguration getRunConfig() {
		return this.ic.getRunConfig();
	}
	
	public ImageContainer getContainer() {
		return this.ic;
	}
	
	public ImagePlus getSlice(Channel chan, int slice, boolean duplicate) {
		return ic.getChannelSliceOrig(chan, slice, duplicate);
	}
	
	public int getOrigStackSize(Channel chan) {
		return ic.getChannelOrig(chan, false).getStackSize();
	}
	
	public void setSliceRegion(int lowSlice, int highSlice) {

		Map<Channel, ImagePlus> originals = this.ic.getOriginals();
		if (originals == null)
			throw new NullPointerException();

		for (Entry<Channel, ImagePlus> en : originals.entrySet()) {
			ImageStack is = en.getValue().getStack().duplicate();
			for (int s = (is.getSize() - highSlice); s > 0; s--) {
				is.deleteLastSlice();
			}

			for (int s = lowSlice; s > 1; s--) {
				is.deleteSlice(1);
			}
			en.setValue(new ImagePlus(en.getValue().getTitle(), is));
		}

	}
	
	public ObjectEditableImage convertToObjectEditableImage(Map<String, ResultsTable> objectResultMaps) {
		return new ObjectEditableImage(this.gui, this.ic, objectResultMaps);
	}
	
	public void deleteSerializedVersion(File serializeDir) {
		File serializeFile = new File(serializeDir.getPath() + File.separator + "postslicestate.ser");
		serializeFile.delete();
	}
	
	public static boolean savePreprocessedImage(PreprocessedEditableImage image, File serializeDir) {
		try {
			if (!serializeDir.isDirectory()) {
				serializeDir.mkdir();
			}
			File serializeFile = new File(serializeDir.getPath() + File.separator + "postslicestate.ser");
			FileOutputStream fileStream = new FileOutputStream(serializeFile); 
	        ObjectOutputStream out = new ObjectOutputStream(fileStream); 
	        out.writeObject(image); 
	        out.close(); 
	        fileStream.close();
	        return true;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static PreprocessedEditableImage loadPreprocessedImage(File serializeDir) {
		try {
			if (!serializeDir.isDirectory())
				return null;
			File serializedFile = new File(serializeDir.getPath() + File.separator + "postslicestate.ser");
			if (!serializedFile.exists())
				return null;

	        FileInputStream fileInput = new FileInputStream(serializedFile); 
            ObjectInputStream in = new ObjectInputStream(fileInput); 

            PreprocessedEditableImage loadedROIImage = (PreprocessedEditableImage) in.readObject(); 
            loadedROIImage.gui = GUI.SINGLETON;
            //this.gui.getPanelDisplay().setImage(object1.get, zoom, clickX, clickY);
            in.close(); 
            fileInput.close(); 
            
            return loadedROIImage;

		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
