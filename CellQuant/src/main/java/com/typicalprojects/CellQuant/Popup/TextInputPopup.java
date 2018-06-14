package com.typicalprojects.CellQuant.Popup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.CellQuant.GUI;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;

public class TextInputPopup extends JFrame {

	private JPanel contentPane;
	private JTextField txtInput;
	private JButton btnDone;
	private final TextInputPopupReceiver receiver;


	/**
	 * Create the frame.
	 */
	public TextInputPopup(String prompt, TextInputPopupReceiver receiverr) {
		this.receiver = receiverr;
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 350, 140);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		
		JLabel lblPrompt = new JLabel(prompt);
		lblPrompt.setFont(new Font("PingFang TC", Font.PLAIN, 14));
		
		txtInput = new JTextField();
		txtInput.setFont(new Font("PingFang TC", Font.PLAIN, 13));
		txtInput.setColumns(10);
		
		btnDone = new JButton("Done");
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(txtInput, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
						.addComponent(lblPrompt)
						.addComponent(btnDone, Alignment.TRAILING))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblPrompt)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtInput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnDone)
					.addContainerGap(22, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
		setVisible(false);
		
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				receiver.processInputFromTextPopup(null);
			}
		});
		
		this.btnDone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				receiver.processInputFromTextPopup(txtInput.getText());
				setVisible(false);
			}
		});
		
		setUIFont(GUI.smallFont);

	}
	
	public static void setUIFont (java.awt.Font f){
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value != null && value instanceof java.awt.Font)
				UIManager.put (key, f);
		}
	}
	
	public void display(Component component) {
		this.txtInput.setText("");
		this.setLocationRelativeTo(component);
		this.setVisible(true);
	}

}
