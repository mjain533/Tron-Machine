package com.typicalprojects.TronMachine.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/**
 * Wraps a JFileChooser in order to provide some modifications to it. Allows using an Open dialog to let
 * the user select files. Also stores a set of recently selected files.
 * 
 * @author Justin Carrington
 */
public class CustomFileChooser {
	
	private JFileChooser fileChooser = null;
	private RecentFileList recentFileList;
	private ArrayList<File> newRecents = new ArrayList<File>();
	
	/**
	 * Construct a new wrapper of a JFileChooser.
	 * 
	 * @param mode selection mode. 1 = only files, 2 = files and directories, 3 = directories only
	 * @param requiredExtensions	for modes 1 and 2, limits the files a user can select
	 * @param selectMultiple if multiple files can be selected
	 */
	public CustomFileChooser(int mode, List<String> requiredExtensions, boolean selectMultiple) {
		fileChooser = new JFileChooser();
		switch (mode) {
		case 1:
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			break;
		case 2:
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			break;
		case 3:
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			break;
		}
		disableTextComponent(fileChooser, true);



		
		fileChooser.setMultiSelectionEnabled(selectMultiple);

		recentFileList = new RecentFileList(fileChooser);
		
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Recents", recentFileList);
		tabbedPane.addTab("Find", new FindAccessory(fileChooser, 10));
		fileChooser.setAccessory(tabbedPane);
		if (requiredExtensions != null)
			fileChooser.setFileFilter(new FileNameExtensionFilter("Required Extensions", requiredExtensions.toArray(new String[requiredExtensions.size()])));

	}
	
	private static void disableTextComponent(Container parent, boolean hide) {
		Component[] c = parent.getComponents();
		for(int j = 0; j < c.length; j++) {
			if(unpack(c[j]).equals("MetalFileChooserUI$3")) {
				//System.out.println("MetalFileChooserUI$3 = " + c[j]);
				//System.out.println("MetalFileChooserUI$3 parent = " +
				//                    unpack(c[j].getParent()));
				if(hide) 
					c[j].getParent().setVisible(false);
				else {    // disable
					c[j].setEnabled(false);
				}
			}
			if(((Container)c[j]).getComponentCount() > 0) {
				disableTextComponent((Container)c[j], hide);
			}
		}
	}

	private static String unpack(Component c) {
		String s = c.getClass().getName();
		int dot = s.lastIndexOf(".");
		if(dot != -1)
			s = s.substring(dot+1);
		return s;
	}
	
	private static class RecentFileList extends JPanel {

		private static final long serialVersionUID = 7408038530211904312L;
		private final JList<File> list;
		private final FileListModel listModel;
		private final JFileChooser fileChooser;

		public RecentFileList(JFileChooser chooser) {
			fileChooser = chooser;
			listModel = new FileListModel();
			list = new JList<>(listModel);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setCellRenderer(new FileListCellRenderer());
			
			JScrollPane scrollPane = new JScrollPane(list);

			
			setLayout(new BorderLayout());
			add(scrollPane);

			list.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						File file = list.getSelectedValue();
						// You might like to check to see if the file still exists...
						fileChooser.setSelectedFile(file);
					}
				}
			});
		}
		
		/**
		 * clear recent files list
		 */
		public void clearList() {
			listModel.clear();
		}
		
		/**
		 * @param file file to add to recents list
		 */
		public void add(File file) {
			listModel.add(file);
		}
		
		/**
		 * @return size of recents list
		 */
		public int getListSize() {
			return listModel.getSize();
		}
		
		/**
		 * @param position placement in recents file list
		 * @return the item to retrieve
		 */
		public File getItemAt(int position) {
			return listModel.getElementAt(position);
		}

		private class FileListModel extends AbstractListModel<File> {

			private static final long serialVersionUID = 974019152541510352L;
			private List<File> files;

			private FileListModel() {
				files = new ArrayList<>();
			}

			
			private void add(File file) {
				if (!files.contains(file)) {
					if (files.isEmpty()) {
						files.add(file);
					} else {
						files.add(0, file);
					}
					fireIntervalAdded(this, 0, 0);
				}
			}


			private void clear() {
				int size = files.size() - 1;
				if (size >= 0) {
					files.clear();
					fireIntervalRemoved(this, 0, size);
				}
			}

			@Override
			public int getSize() {
				return files.size();
			}

			@Override
			public File getElementAt(int index) {
				return files.get(index);
			}
		}

		private class FileListCellRenderer extends DefaultListCellRenderer {

			private static final long serialVersionUID = -3952450893727404942L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof File) {
					File file = (File) value;
					Icon ico = FileSystemView.getFileSystemView().getSystemIcon(file);
					setIcon(ico);
					setToolTipText(file.getParent());
					setText(file.getName());
				}
				return this;
			}

		}

	}
	
	/**
	 * Displays the open file dialog. If recent files are specified, these will also be displayed.
	 * 
	 * @param relative	component on which the open dialog will be centered
	 * @param recents	set of recent files to offer the user
	 * @return list of selected file(s). Null if nothing was selected.
	 */
	public List<File> open(Component relative, List<File> recents) {
		newRecents = new ArrayList<File>();
		this.recentFileList.clearList();

		if (recents != null) {
			for (File file : recents) {
				this.recentFileList.add(file);
			}
		}
		Action detailss = fileChooser.getActionMap().get("viewTypeDetails");
		if (detailss != null) {
			detailss.actionPerformed(null);
		}
		List<File> selected = null;
		if (fileChooser.showOpenDialog(relative) == JFileChooser.APPROVE_OPTION) {
			if (fileChooser.isMultiSelectionEnabled()) {
				selected = Arrays.asList(fileChooser.getSelectedFiles());
			} else {
				selected = Arrays.asList(fileChooser.getSelectedFile());

			}
		}
		
		if (selected != null && !selected.isEmpty()) {
			newRecents.add(selected.get(0).getParentFile());
			for (int i = 0; i < recentFileList.getListSize() && i < 5; i++) {
				File fileRecent = recentFileList.getItemAt(i);
				if (fileRecent.exists() && !newRecents.contains(fileRecent)) {
					newRecents.add(recentFileList.getItemAt(i));
				}
			}
		}
		
		return selected;
		
	}
	
	/**
	 * @return new set of recent files from the last browsing session. Never null.
	 */
	public List<File> getRecents() {
		return this.newRecents;
	}
	
}
