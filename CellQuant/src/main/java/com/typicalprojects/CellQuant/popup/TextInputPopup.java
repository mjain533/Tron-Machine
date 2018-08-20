package com.typicalprojects.CellQuant.popup;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;

@SuppressWarnings("serial")
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
		JLabel lblPrompt = new JLabel(prompt);
		lblPrompt.setFont(new Font("PingFang TC", Font.BOLD, 14));
		setBounds(100, 100, 410, 160);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		
		
		txtInput = new JTextField();
		txtInput.setFont(new Font("PingFang TC", Font.BOLD, 13));
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
		getRootPane().setDefaultButton(this.btnDone);
		setUIFont(new Font("PingFang TC", Font.BOLD, 13));

	}
	
	public static void setUIFont (java.awt.Font f){
		java.util.Enumeration<Object> keys = UIManager.getLookAndFeelDefaults().keys();
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
		this.txtInput.grabFocus();
		
		this.setVisible(true);
	}

}
