package com.typicalprojects.CellQuant;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.LayoutStyle.ComponentPlacement;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8653687111169656048L;
	private JPanel contentPane;

	private static MainFrame SINGLETON = null;;
	//private static WaitingFrame waitingFrame = new WaitingFrame();
	private GUI guiWindow;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
				
				SINGLETON = new MainFrame();
				SINGLETON.setVisible(true);
				
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle("The TRON Machine");



		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 250);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		this.setResizable(false);

		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel = new JLabel("Please select a program.");
		lblNewLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
		lblNewLabel.setFont(lblNewLabel.getFont().deriveFont(lblNewLabel.getFont().getStyle() | Font.BOLD, lblNewLabel.getFont().getSize() + 3f));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblNewLabel, BorderLayout.NORTH);

		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentPane.add(panel, BorderLayout.CENTER);

		JButton btnQuantifyMigration = new JButton("<html><center>Quantify Neuronal Migration</center></html>")  ;
		btnQuantifyMigration.setOpaque(true);
		btnQuantifyMigration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							//waitingFrame.show(SINGLETON);
							setVisible(false);
							if (guiWindow == null) {
								guiWindow = new GUI(SINGLETON);
							}
							//waitingFrame.disappear();

							guiWindow.show();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}
		});
		btnQuantifyMigration.setFont(btnQuantifyMigration.getFont().deriveFont(btnQuantifyMigration.getFont().getStyle() | Font.BOLD));
		btnQuantifyMigration.setFocusable(false);
		btnQuantifyMigration.setIconTextGap(0);
		btnQuantifyMigration.setAlignmentY(0.0f);

		JButton btnQuantifyNeurites = new JButton("<html><center>Quantify Neurite Outgrowth</center></html>");
		btnQuantifyNeurites.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Neurite processor not yet finished. Coming soon.", "Incomplete Program", JOptionPane.WARNING_MESSAGE);
			}
		});;
		btnQuantifyNeurites.setIconTextGap(0);
		btnQuantifyNeurites.setFont(btnQuantifyNeurites.getFont().deriveFont(btnQuantifyNeurites.getFont().getStyle() | Font.BOLD));
		btnQuantifyNeurites.setFocusable(false);
		btnQuantifyNeurites.setAlignmentY(0.0f);

		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
						.addGap(35)
						.addComponent(btnQuantifyMigration, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnQuantifyNeurites, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE)
						.addGap(31))
				);
		gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
						.addGap(37)
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addComponent(btnQuantifyNeurites, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnQuantifyMigration, GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
						.addGap(37))
				);
		panel.setLayout(gl_panel);



	}

	public void reshow() {
		setVisible(true);
	}
}
