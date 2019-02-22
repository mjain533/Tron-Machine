package com.typicalprojects.TronMachine.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.BevelBorder;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ColorChooserUI extends JDialog {

	private static final long serialVersionUID = 4857844040841895166L;
	private final JPanel contentPanel = new JPanel();
	private volatile Color chosenColor = null;
	private JColorChooser colorChooser;
	private JLabel lblInstructions;

	/**
	 * Launch the application.
	 */
	/*public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

			ColorChooserUI dialog = new ColorChooserUI("Please select a color.");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	/**
	 * Create the dialog.
	 */
	public ColorChooserUI(String instruction) {
		setBounds(100, 100, 650, 400);
		setModal(true);
		setTitle("Pick Color");
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		colorChooser = new JColorChooser();
        AbstractColorChooserPanel[] panels = colorChooser.getChooserPanels();
        for (AbstractColorChooserPanel accp : panels) {
            if (accp.getDisplayName().equals("HSL")) {
            		colorChooser.removeChooserPanel(accp);
            }
            
        }
        
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		JPanel pnlInstructions = new JPanel();
		pnlInstructions.setBackground(new Color(211, 211, 211));

		pnlInstructions.setBorder(new LineBorder(new Color(0, 0, 0)));
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.TRAILING)
				.addComponent(pnlInstructions, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 790, Short.MAX_VALUE)
				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 790, Short.MAX_VALUE)
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_contentPanel.createSequentialGroup()
					.addComponent(pnlInstructions, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
					.addGap(3))
		);
		
		lblInstructions = new JLabel(instruction);
		lblInstructions.setFont(GUI.mediumBoldFont);

		GroupLayout gl_pnlInstructions = new GroupLayout(pnlInstructions);
		gl_pnlInstructions.setHorizontalGroup(
			gl_pnlInstructions.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlInstructions.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblInstructions)
					.addContainerGap(620, Short.MAX_VALUE))
		);
		gl_pnlInstructions.setVerticalGroup(
			gl_pnlInstructions.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlInstructions.createSequentialGroup()
					.addGap(2)
					.addComponent(lblInstructions, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(2))
		);
		pnlInstructions.setLayout(gl_pnlInstructions);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(colorChooser, BorderLayout.CENTER);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						chosenColor = colorChooser.getColor();
						removeDisplay();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						chosenColor = null;
						removeDisplay();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public void removeDisplay() {
		setVisible(false);
	}
	
	public Color display(Component parent, String instruction) {
		this.chosenColor = null;
		this.colorChooser.setColor(Color.BLACK);
		this.lblInstructions.setText(instruction);
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
		return getChosenColor();
	}
	
	private Color getChosenColor() {
		if (this.chosenColor == null) { 
			return null;
		}
		
		return new Color(this.chosenColor.getRed(), this.chosenColor.getGreen(), this.chosenColor.getBlue());
	}
	
}
