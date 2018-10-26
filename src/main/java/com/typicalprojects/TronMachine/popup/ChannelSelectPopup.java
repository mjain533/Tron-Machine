package com.typicalprojects.TronMachine.popup;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;

import com.typicalprojects.TronMachine.util.ImageContainer.Channel;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;

public class ChannelSelectPopup extends JDialog {

	private static final long serialVersionUID = 3084455565241038180L;
	private JCheckBox chckbxGreen;
	private JCheckBox chckbxRed;
	private JCheckBox chckbxBlue;
	private JCheckBox chckbxWhite;


	public ChannelSelectPopup() {
		setBounds(100, 100, 300, 120);
		setAlwaysOnTop(true);
		setResizable(false);
		setModal(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {

				setVisible(false);

			}
		});
		
		JLabel label = new JLabel("Select Channels:");
		label.setFont(new Font("PingFang TC", Font.BOLD, 13));
		
		chckbxGreen = new JCheckBox("Green");
		chckbxGreen.setForeground(new Color(57, 137, 23));
		chckbxGreen.setFont(new Font("PingFang TC", Font.BOLD, 13));
		chckbxGreen.setFocusable(false);
		chckbxRed = new JCheckBox("Red");
		chckbxRed.setForeground(Color.RED);
		chckbxRed.setFont(new Font("PingFang TC", Font.BOLD, 13));
		chckbxRed.setFocusable(false);
		chckbxBlue = new JCheckBox("Blue");
		chckbxBlue.setForeground(Color.BLUE);
		chckbxBlue.setFont(new Font("PingFang TC", Font.BOLD, 13));
		chckbxBlue.setFocusable(false);
		chckbxWhite = new JCheckBox("White");
		chckbxWhite.setForeground(Color.GRAY);
		chckbxWhite.setFont(new Font("PingFang TC", Font.BOLD, 13));
		chckbxWhite.setFocusable(false);
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		button.setMargin(new Insets(0, 10, 0, 10));
		button.setFocusable(false);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(chckbxGreen, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
							.addComponent(chckbxRed, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
							.addComponent(chckbxBlue, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
							.addComponent(chckbxWhite, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(128)
							.addComponent(button, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(label, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(68, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(label, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(chckbxGreen, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxRed, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxBlue, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxWhite, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
					.addGap(8)
					.addComponent(button, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
					.addGap(9))
		);
		getContentPane().setLayout(groupLayout);
	}
	
	public void prompt(List<Channel> defaultSelected, Component component) {
		if (defaultSelected.contains(Channel.GREEN)) {
			this.chckbxGreen.setSelected(true);
		} else {
			this.chckbxGreen.setSelected(false);
		}
		if (defaultSelected.contains(Channel.RED)) {
			this.chckbxRed.setSelected(true);

		} else {
			this.chckbxRed.setSelected(false);
		}
		if (defaultSelected.contains(Channel.BLUE)) {
			this.chckbxBlue.setSelected(true);

		} else {
			this.chckbxBlue.setSelected(false);
		}
		if (defaultSelected.contains(Channel.WHITE)) {
			this.chckbxWhite.setSelected(true);

		} else {
			this.chckbxWhite.setSelected(false);
		}
		setLocationRelativeTo(component);
		setVisible(true);
	}
	
	public List<Channel> getSelected() {
		List<Channel> chans = new ArrayList<Channel>();
		if (this.chckbxGreen.isSelected()) {
			chans.add(Channel.GREEN);
		}
		if (this.chckbxRed.isSelected()) {
			chans.add(Channel.RED);
		}
		if (this.chckbxWhite.isSelected()) {
			chans.add(Channel.WHITE);
		}
		if (this.chckbxBlue.isSelected()) {
			chans.add(Channel.BLUE);
		}
		return chans;
	}
}
