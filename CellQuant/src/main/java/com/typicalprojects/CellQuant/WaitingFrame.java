package com.typicalprojects.CellQuant;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import java.awt.Font;

public class WaitingFrame extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WaitingFrame frame = new WaitingFrame();
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
	public WaitingFrame() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);

		setBounds(100, 100, 150, 80);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblPleaseWait = new JLabel("Please wait...");
		lblPleaseWait.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		lblPleaseWait.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPleaseWait.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblPleaseWait, BorderLayout.CENTER);
	}
	
	public void show(Component parent) {
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	public void disappear() {
		setVisible(false);
	}

}
