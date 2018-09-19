package com.typicalprojects.CellQuant.neuronal_migration;

import javax.swing.JPanel;

import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.border.LineBorder;
import java.awt.Font;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;

public class asdf extends JPanel {

	/**
	 * Create the panel.
	 */
		
		
	
	private static final long serialVersionUID = -6622171153906374924L;
	private JLabel lblError;
	private JTextField minThresh;
	private JTextField txtUnsharpRadius;
	private JTextField txtUnsharpWidth;
	private JTextField txtGaussianSigma;

	/**
	 * Create the panel.
	 */
	public asdf() {
		setPreferredSize(new Dimension(518, 384));

		JPanel pnlProcessingSettings = new JPanel();
		pnlProcessingSettings.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlProcessingSettings.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlProcessingSettings.setBackground(new Color(211, 211, 211));
		
		JLabel lblProcessingSettings = new JLabel("Parameter Values");
		
		GroupLayout gl_pnlProcessingSettings = new GroupLayout(pnlProcessingSettings);
		gl_pnlProcessingSettings.setHorizontalGroup(
			gl_pnlProcessingSettings.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlProcessingSettings.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblProcessingSettings, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_pnlProcessingSettings.setVerticalGroup(
			gl_pnlProcessingSettings.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblProcessingSettings, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
		);
		pnlProcessingSettings.setLayout(gl_pnlProcessingSettings);
		
		lblError = new JLabel("Error:");
		lblError.setFont(GUI.mediumFont);
		lblError.setForeground(Color.RED);
		
		JLabel lblMinimumThreshold = new JLabel("Auto-Threshold Minimum Value (0-255):");
		
		minThresh = new JTextField();
		minThresh.setText("0");
		minThresh.setColumns(10);
		
		JLabel lblUnsharpMaskRadius = new JLabel("Unsharp Mask Pixel Radius (1-1000):");
		
		txtUnsharpRadius = new JTextField();
		txtUnsharpRadius.setText("20");
		txtUnsharpRadius.setColumns(10);
		
		JLabel lblUnsharpMaskPixel = new JLabel("Unsharp Mask Pixel Width (0.1-0.9):");
		
		txtUnsharpWidth = new JTextField();
		txtUnsharpWidth.setColumns(10);
		
		JLabel lblGaussianBlurSigma = new JLabel("Gaussian Blur Sigma Value (0.01-100):");
		
		txtGaussianSigma = new JTextField();
		txtGaussianSigma.setColumns(10);
		

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(pnlProcessingSettings, GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblMinimumThreshold, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
								.addComponent(lblUnsharpMaskRadius, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
								.addComponent(lblUnsharpMaskPixel, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(minThresh, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
								.addComponent(txtUnsharpWidth, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
								.addComponent(txtUnsharpRadius, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE))
							.addGap(198))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblGaussianBlurSigma, GroupLayout.PREFERRED_SIZE, 257, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(txtGaussianSigma, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(195, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblError)
							.addContainerGap(470, Short.MAX_VALUE))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(pnlProcessingSettings, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblMinimumThreshold)
						.addComponent(minThresh, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtUnsharpRadius, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblUnsharpMaskRadius))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtUnsharpWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblUnsharpMaskPixel))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(2)
							.addComponent(lblGaussianBlurSigma))
						.addComponent(txtGaussianSigma, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(191)
					.addComponent(lblError)
					.addContainerGap())
		);
		setLayout(groupLayout);
		
		

	}
	
	
	
	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}
	
	public void removeError() {
		this.lblError.setVisible(false);
	}
	
	public boolean applyFields(Settings settings) {
		int minThresh = -1;
		int unsharpRadius = -1;
		double unsharpWeight = -1;
		double gaussianSigma = -1;

		try {
			String text = this.minThresh.getText();
			minThresh = Integer.parseInt(text);
			if (minThresh < 0 || minThresh > 255)
				throw new Exception();
		} catch (Exception e) {
			displayError("Thresholding minimum must be an integer between 0 and 255.");
			return false;
		}
		
		try {
			String text = this.txtUnsharpRadius.getText();
			unsharpRadius = Integer.parseInt(text);
			if (unsharpRadius < 1 || unsharpRadius > 1000)
				throw new Exception();
		} catch (Exception e) {
			displayError("Unsharp mask radius must be an integer between 1 and 1000.");
			return false;
		}
		
		try {
			String text = this.txtUnsharpWidth.getText();
			unsharpWeight = Double.parseDouble(text);
			if (unsharpWeight < 0.1 || unsharpWeight > 0.9)
				throw new Exception();
		} catch (Exception e) {
			displayError("Unsharp mask radius must be a decimal between 0.1 and 0.9.");
			return false;
		}
		
		try {
			String text = this.txtGaussianSigma.getText();
			gaussianSigma = Double.parseDouble(text);
			if (gaussianSigma < 0.01 || gaussianSigma > 100)
				throw new Exception();
		} catch (Exception e) {
			displayError("Gaussian blur sigma must be a decimal between 0.01 and 100.");
			return false;
		}
		settings.processingMinThreshold = minThresh;
		settings.processingUnsharpMaskRadius = unsharpRadius;
		settings.processingUnsharpMaskWeight = unsharpWeight;
		settings.processingGaussianSigma = gaussianSigma;

		removeError();
		return true;

	}
}
