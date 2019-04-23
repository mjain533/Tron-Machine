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
package com.typicalprojects.TronMachine.neuronal_migration.panels;

import java.awt.BorderLayout;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.util.Logger;

/**
 * The panel used to display the log of all operations. Regularly cleared so it does not get too long.
 * On the left side of the GUI.
 * 
 * @author Justin Carrington
 */
public class PnlLog implements Logger {

	private JPanel rawPanel;
	private volatile JTextArea textLog;
	private volatile JScrollPane spLog;

	private volatile String task; // task currently being displayed
	private JLabel lblLog;
	private JLabel lblDisabled = new JLabel("");
	private final int maxLineCount = 400; // The maximum amount of lines allowed in the log before clearing

	/**
	 * Constructs the log panel for the GUI.
	 */
	public PnlLog() {


		rawPanel = new JPanel();
		rawPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		lblLog = new JLabel("Status:");
		lblLog.setFocusable(false);

		spLog = new JScrollPane();
		spLog.setFocusable(false);


		textLog = new JTextArea();
		textLog.setEditable(false);
		textLog.setEnabled(true);
		textLog.setFocusable(false);
		spLog.setViewportView(textLog);
		setDisplayState(false);

	}

	/**
	 * @return raw JPanel representing this object, which is wrapped within this object.
	 */
	public JPanel getRawPanel(){ 
		return this.rawPanel;
	}

	@Override
	public synchronized void setCurrentTask(final String newTask) {

		/*Runnable run = new Runnable() {
			@Override
			public void run() {*/
		task = newTask;
		if (task == null) {
			return;
		}
		if (!newTask.endsWith("."))
			task = task + ".";
		
		if (textLog.getLineCount() > maxLineCount) {
			try {
				textLog.replaceRange("", textLog.getLineEndOffset(textLog.getLineCount() - 5), textLog.getLineEndOffset(textLog.getLineCount() - 1));
			} catch (BadLocationException e) {
				// won't happen
			}
		}
		textLog.setText(task + '\n' + textLog.getText());
		spLog.getVerticalScrollBar().setValue(0);

		/*		}
		};
		SwingUtilities.invokeLater(run);*/


	}

	@Override
	public synchronized void setCurrentTaskCompleteWithError() {
		
		if (task == null || textLog.getLineCount() == 0)
			return;


		if (textLog.getLineCount() > maxLineCount) {
			try {
				textLog.replaceRange("", textLog.getLineEndOffset(textLog.getLineCount() - 5), textLog.getLineEndOffset(textLog.getLineCount() - 1));
			} catch (BadLocationException e) {
				// won't happen
			}

		}


		try {
			textLog.setText(task + " Error." + "\n" + textLog.getText().substring(textLog.getLineEndOffset(0)));
		} catch (BadLocationException e) {
			// won't happen
		}

		task = null;
		spLog.getVerticalScrollBar().setValue(0);
		
	}

	@Override
	public synchronized void setCurrentTaskComplete() {

		if (task == null || textLog.getLineCount() == 0)
			return;


		if (textLog.getLineCount() > maxLineCount) {
			try {
				textLog.replaceRange("", textLog.getLineEndOffset(textLog.getLineCount() - 5), textLog.getLineEndOffset(textLog.getLineCount() - 1));
			} catch (BadLocationException e) {
				// won't happen
			}

		}


		try {
			textLog.setText(task + " Done." + "\n" + textLog.getText().substring(textLog.getLineEndOffset(0)));
		} catch (BadLocationException e) {
			// won't happen
		}

		task = null;
		spLog.getVerticalScrollBar().setValue(0);


	}

	@Override
	public synchronized void setCurrentTaskProgress(int progress, int total) {
		/*Runnable run = new Runnable() {
			@Override
			public void run() {*/
		if (task == null)
			return;

		if (textLog.getLineCount() > maxLineCount) {
			try {
				textLog.replaceRange("", textLog.getLineEndOffset(textLog.getLineCount() - 5), textLog.getLineEndOffset(textLog.getLineCount() - 1));
			} catch (BadLocationException e) {
				// won't happen
			}

		}

		try {
			textLog.setText(task + " (" + progress + "/" + total + ")" + "\n" + textLog.getText().substring(textLog.getLineEndOffset(0)));
		} catch (BadLocationException e) {
			// won't happen
		}

		spLog.getVerticalScrollBar().setValue(0);
		/*	}
		};
		SwingUtilities.invokeLater(run);*/
	}
	
	/**
	 * Sets whether this panel is enabled (only enabled during a run). Basically whether or not the panel
	 * will be displayed to the user.
	 * 
	 * @param enabled true if enabled.
	 */
	public void setDisplayState(boolean enabled) {

		this.rawPanel.removeAll();
		if (enabled) {
			this.rawPanel.setBackground(GUI.colorPnlEnabled);
			GroupLayout gl_pnlLog = new GroupLayout(rawPanel);
			gl_pnlLog.setHorizontalGroup(
					gl_pnlLog.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_pnlLog.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_pnlLog.createParallelGroup(Alignment.LEADING)
									.addComponent(spLog, GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
									.addComponent(lblLog))
							.addContainerGap())
					);
			gl_pnlLog.setVerticalGroup(
					gl_pnlLog.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_pnlLog.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblLog)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(spLog, GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
							.addContainerGap())
					);
			this.rawPanel.setLayout(gl_pnlLog);

		} else {

			lblDisabled = new JLabel("<html><body><p style='width: 100px; text-align: center;'>Please select images using the interface above.</p></body></html>");
			lblDisabled.setHorizontalAlignment(SwingConstants.CENTER);
			this.rawPanel.setBackground(GUI.colorPnlDisabled);
			this.rawPanel.updateUI();
			this.rawPanel.setLayout(new BorderLayout(0,0));
			this.rawPanel.add(lblDisabled, BorderLayout.CENTER);
		}
	}

}
