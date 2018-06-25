package com.typicalprojects.CellQuant.neuronal_migration.panels;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.util.SynchronizedProgress.SynchronizedProgressReceiver;

public class PnlLog implements SynchronizedProgressReceiver {
	
	private JPanel rawPanel;
	private JTextArea textLog;
	private JScrollPane spLog;
	
	public PnlLog(GUI gui) {
		
		
		rawPanel = new JPanel();
		rawPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		JLabel lblLog = new JLabel("Status:");
		lblLog.setFocusable(false);
		
		spLog = new JScrollPane();
		spLog.setFocusable(false);
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
		
		textLog = new JTextArea();
		textLog.setEditable(false);
		textLog.setEnabled(true);
		textLog.setFocusable(false);
		spLog.setViewportView(textLog);
		rawPanel.setLayout(gl_pnlLog);
		

	}
	
	public JPanel getRawPanel(){ 
		return this.rawPanel;
	}
	


	public synchronized void applyProgress(String oldTask, String task, int progressSoFar, int totalProgress) {
		
		if (textLog.getLineCount() > 90) {
			try {
				textLog.replaceRange("", textLog.getLineEndOffset(textLog.getLineCount() - 5), textLog.getLineEndOffset(textLog.getLineCount() - 1));
			} catch (BadLocationException e) {
				// won't happen
			}
			
		}
		String progressPart = "";
		if (oldTask != null && oldTask.equals(task) && totalProgress != -1) {
			progressPart = " ("+ progressSoFar + "/" + totalProgress +")";
			String currText = textLog.getText();
			
			String newText = task + progressPart + currText.substring(currText.indexOf('\n'));
			textLog.setText(newText);
			spLog.getVerticalScrollBar().setValue(0);
			return;
		}
		if (progressSoFar <= totalProgress && progressSoFar != -1 && totalProgress != -1) {
			progressPart = " ("+ progressSoFar + "/" + totalProgress +")";
		}
		
		if (textLog.getText().equals("")) {
			textLog.setText(task + progressPart);
		} else {
			textLog.setText(task + progressPart + "\n" + textLog.getText());

		}
		spLog.getVerticalScrollBar().setValue(0);
		
	}
	
}
