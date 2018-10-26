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

import javax.swing.JPanel;

import java.awt.Dimension;

import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Component;

import javax.swing.border.LineBorder;


import com.typicalprojects.TronMachine.popup.ChannelSelectPopup;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;
import com.typicalprojects.TronMachine.util.SimpleJList;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.JList;

public class PrefPaneBuilder extends JPanel {


	private static final long serialVersionUID = -6622171153906374924L;
	private SimpleJList<OutputOption> listOutputROI;
	private SimpleJList<OutputOption> listOptionsROI;
	private SimpleJList<OutputOption> listOutputObj;
	private SimpleJList<OutputOption> listOptionsObj;
	private JButton mvRightObj;
	private AbstractButton mvLeftObj;
	private JButton mvRightROI;
	private JButton mvLeftROI;
	private ChannelSelectPopup chanSelectPopup;

	/**
	 * Create the panel.
	 */
	public PrefPaneBuilder() {
		setPreferredSize(new Dimension(518, 384));
		
		this.chanSelectPopup = new ChannelSelectPopup();
		
		JPanel panel = new JPanel();
		panel.setFont(new Font("Arial", Font.PLAIN, 13));
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBackground(new Color(211, 211, 211));
		
		JLabel lblObjectSelectionOutput = new JLabel("Object Selection Output");
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 492, Short.MAX_VALUE)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblObjectSelectionOutput, GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 23, Short.MAX_VALUE)
				.addComponent(lblObjectSelectionOutput, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
		);
		panel.setLayout(gl_panel);
		
		JScrollPane scrOptionsObj = new JScrollPane();
		
		JLabel lblOptions2 = new JLabel("Options");
		
		mvRightObj = new JButton(">>");
		mvRightObj.setMargin(new Insets(0, -30, 0, -30));
		
		mvLeftObj = new JButton("<<");
		mvLeftObj.setMargin(new Insets(0, -30, 0, -30));
		
		JScrollPane scrOutputObj = new JScrollPane();
		
		JLabel lblOutput2 = new JLabel("Output");
		
		JPanel panel_1 = new JPanel();
		panel_1.setFont(new Font("Arial", Font.PLAIN, 13));
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setBackground(new Color(211, 211, 211));
		
		JLabel lblRoiLineSelection = new JLabel("ROI Line Selection Output");
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 492, Short.MAX_VALUE)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblRoiLineSelection, GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 23, Short.MAX_VALUE)
				.addComponent(lblRoiLineSelection, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
		);
		panel_1.setLayout(gl_panel_1);
		
		JScrollPane scrOptionsROI = new JScrollPane();
		
		JLabel lblOptions3 = new JLabel("Options");
		
		mvRightROI = new JButton(">>");
		mvRightROI.setMargin(new Insets(0, -30, 0, -30));
		
		mvLeftROI = new JButton("<<");
		mvLeftROI.setMargin(new Insets(0, -30, 0, -30));
		
		JScrollPane scrOutputROI = new JScrollPane();
		
		JLabel lblOutput3 = new JLabel("Output");

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblOptions2, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
							.addGap(235)
							.addComponent(lblOutput2, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(scrOptionsObj, GroupLayout.PREFERRED_SIZE, 224, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(mvRightObj, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
								.addComponent(mvLeftObj, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(scrOutputObj, GroupLayout.PREFERRED_SIZE, 225, GroupLayout.PREFERRED_SIZE))
						.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblOptions3, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
							.addGap(235)
							.addComponent(lblOutput3, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(scrOptionsROI, GroupLayout.PREFERRED_SIZE, 224, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(mvRightROI, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
								.addComponent(mvLeftROI, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(scrOutputROI, GroupLayout.PREFERRED_SIZE, 225, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblOptions2)
								.addComponent(lblOutput2))
							.addGap(6)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(scrOptionsObj, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
								.addComponent(scrOutputObj, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(mvRightObj, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(mvLeftObj, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addGap(44)))
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblOptions3)
								.addComponent(lblOutput3))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(scrOutputROI, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
								.addComponent(scrOptionsROI, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
							.addGap(20))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(mvRightROI, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(mvLeftROI, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addGap(58))))
		);
		OutputOptionRenderer<OutputOption> renderer = new OutputOptionRenderer<OutputOption>();
		MouseAdapter mouseAdaptor = new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        @SuppressWarnings("unchecked")
				SimpleJList<OutputOption> list = (SimpleJList<OutputOption>)evt.getSource();
		        if (evt.getClickCount() == 2) {

		            // Double-click detected
		            int index = list.locationToIndex(evt.getPoint());
		            if (index > -1) {
		            		OutputOption output = list.getElementAt(index);
		            		chanSelectPopup.prompt(output.includedChannels, Preferences2.SINGLETON_FRAME);
		            		output.includedChannels = chanSelectPopup.getSelected();
		            }
		        }
		    }
		};
		listOutputROI = new SimpleJList<OutputOption>();
		listOutputROI.setCellRenderer(renderer);
		listOutputROI.addMouseListener(mouseAdaptor);
		scrOutputROI.setViewportView(listOutputROI);
		listOptionsROI = new SimpleJList<OutputOption>();
		listOptionsROI.setCellRenderer(renderer);
		listOptionsROI.addMouseListener(mouseAdaptor);
		scrOptionsROI.setViewportView(listOptionsROI);
		
		listOutputObj = new SimpleJList<OutputOption>();
		listOutputObj.setCellRenderer(renderer);
		listOutputObj.addMouseListener(mouseAdaptor);
		scrOutputObj.setViewportView(listOutputObj);
		
		listOptionsObj = new SimpleJList<OutputOption>();
		listOptionsObj.setCellRenderer(renderer);
		listOptionsObj.addMouseListener(mouseAdaptor);
		scrOptionsObj.setViewportView(listOptionsObj);
		setLayout(groupLayout);
		
		this.mvLeftObj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				OutputOption option = listOutputObj.getSelectedValue();
				if (option != null) {
					listOutputObj.removeItem(option);
					listOptionsObj.addItem(option);
				}
				
			}
		});
		this.mvRightObj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				OutputOption option = listOptionsObj.getSelectedValue();
				if (option != null) {
					listOptionsObj.removeItem(option);
					listOutputObj.addItem(option);
				}
				
			}
		});
		this.mvLeftROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				OutputOption option = listOutputROI.getSelectedValue();
				if (option != null) {
					listOutputROI.removeItem(option);
					listOptionsROI.addItem(option);
				}
				
			}
		});
		this.mvRightROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				OutputOption option = listOptionsROI.getSelectedValue();
				if (option != null) {
					listOptionsROI.removeItem(option);
					listOutputROI.addItem(option);
				}
				
			}
		});
		

	}
	
	public static class OutputOption {
		
		List<Channel> includedChannels = new ArrayList<Channel>();
		Option option;
		
		public OutputOption(Option option) {
			this.option = option;
		}
		
		public void addChannel(Channel c) {
			includedChannels.add(c);
			includedChannels.sort(null);
		}
		
		public void removeChannel(Channel c) {
			includedChannels.remove(c);
		}
		
		public enum Option {
			D("Display", "Condensed");
			private String display;
			private String condensed;
			private Option(String display, String condensed) {
				this.display = display;
				this.condensed = condensed;
			}
			public String getDisplay() {
				return this.display;
			}
			public String getCondensed() {
				return this.condensed;
			}
			public static Option fromCondensed(String string) {
				for (Option option : Option.values()) {
					if (option.getCondensed().equals(string))
						return option;
				}
				return null;
			}
		}
		
	}
	
	private static class OutputOptionRenderer<K> implements ListCellRenderer<K> {

		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {


			JLabel renderer = (JLabel) defaultRenderer
					.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);

			if (value instanceof OutputOption) {
				OutputOption option = (OutputOption) value;
				renderer.setText(option.option.getDisplay() + " <" + combineChans(option.includedChannels) + ">");
			} else if (value != null) {
				renderer.setText(value.toString());
			}

			return renderer;


		}
		
		private String combineChans(List<Channel> chans) {
			String result = "";
			String delim = "";
			for (Channel chan : chans) {
				result = result.concat(delim).concat(chan.getAbbreviation());
				delim = ",";
			}
			return result;
		}

	}

}
