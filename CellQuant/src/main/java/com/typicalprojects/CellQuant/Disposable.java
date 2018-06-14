package com.typicalprojects.CellQuant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.CellQuant.Popup.TextInputPopup;

import Util.ImagePanel;
import Util.SimpleJList;

import javax.swing.border.BevelBorder;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JList;
import javax.swing.SpringLayout;
import javax.swing.JSlider;
import java.awt.Component;
import javax.swing.Box;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

public class Disposable extends JFrame {

	private JPanel contentPane;
	private JSlider sldrSlice;
	private JSlider sldrChan;
	private JLabel lblChanNum;
	private JLabel lblSliceNum;
	private ImagePanel pnlImage;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Disposable frame = new Disposable();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Disposable() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 408, 637);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentPane.add(panel, BorderLayout.CENTER);
		
		JPanel pnlSliderSlice = new JPanel();
		
		JPanel pnlSliderChan = new JPanel();
		pnlSliderChan.setLayout(null);
		
		JLabel lblChanSlider = new JLabel("Chan");
		lblChanSlider.setHorizontalTextPosition(SwingConstants.CENTER);
		lblChanSlider.setHorizontalAlignment(SwingConstants.CENTER);
		lblChanSlider.setBounds(0, 0, 35, 25);
		pnlSliderChan.add(lblChanSlider);
		
		sldrSlice = new JSlider();
		sldrSlice.setOrientation(SwingConstants.VERTICAL);
		sldrSlice.setBounds(0, 23, 35, 190);
		pnlSliderChan.add(sldrSlice);
		
		lblSliceNum = new JLabel("10");
		lblSliceNum.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSliceNum.setHorizontalAlignment(SwingConstants.CENTER);
		lblSliceNum.setBounds(0, 225, 35, 19);
		pnlSliderChan.add(lblSliceNum);
		
		pnlImage = new ImagePanel();
		pnlImage.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(pnlSliderChan, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
						.addComponent(pnlSliderSlice, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(pnlImage, GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(pnlImage, GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(pnlSliderSlice, GroupLayout.PREFERRED_SIZE, 247, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 95, Short.MAX_VALUE)
							.addComponent(pnlSliderChan, GroupLayout.PREFERRED_SIZE, 247, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		pnlSliderSlice.setLayout(null);
		
		JLabel lblSliceSlider = new JLabel("Slice");
		lblSliceSlider.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSliceSlider.setHorizontalAlignment(SwingConstants.CENTER);
		lblSliceSlider.setBounds(0, 0, 35, 25);
		pnlSliderSlice.add(lblSliceSlider);
		
		sldrChan = new JSlider();
		sldrChan.setBounds(0, 23, 35, 190);
		pnlSliderSlice.add(sldrChan);
		sldrChan.setOrientation(SwingConstants.VERTICAL);
		
		lblChanNum = new JLabel("10");
		lblChanNum.setHorizontalTextPosition(SwingConstants.CENTER);
		lblChanNum.setHorizontalAlignment(SwingConstants.CENTER);
		lblChanNum.setBounds(0, 225, 35, 19);
		pnlSliderSlice.add(lblChanNum);
		panel.setLayout(gl_panel);
		
	}
}
