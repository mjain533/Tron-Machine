package com.typicalprojects.TronMachine.popup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.TronMachine.util.SimpleJList;

import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.JList;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Set;
import java.awt.event.ActionEvent;

public class MultiSelectPopup<K extends Displayable> extends JDialog {

	private static final long serialVersionUID = 1450099735919482223L;
	private final JPanel contentPanel = new JPanel();
	private JLabel lblInstructions;
	private SimpleJList<K> lstOptions;
	private boolean cancelled = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MultiSelectPopup<Displayable> dialog = new MultiSelectPopup<Displayable>();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public MultiSelectPopup() {
		setResizable(false);
		setBounds(100, 100, 300, 250);
		setModal(true);
		setAlwaysOnTop(true);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				
				removeDisplay(true);

			}
		});
		
		lblInstructions = new JLabel("Label");

		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
				gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
						.addComponent(lblInstructions)
						.addContainerGap(253, Short.MAX_VALUE))
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
				);
		gl_contentPanel.setVerticalGroup(
				gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
						.addComponent(lblInstructions)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
				);
		
		lstOptions = new SimpleJList<K>(new CheckboxListCellRenderer<K>());

		lstOptions.setFocusable(false);

		lstOptions.setSelectionModel(new DefaultListSelectionModel() {

			private static final long serialVersionUID = -3774844431873784205L;
			private int i0 = -1;
		    private int i1 = -1;

		    public void setSelectionInterval(int index0, int index1) {
		        if(i0 == index0 && i1 == index1){
		            if(getValueIsAdjusting()){
		                 setValueIsAdjusting(false);
		                 setSelection(index0, index1);
		            }
		        }else{
		            i0 = index0;
		            i1 = index1;
		            setValueIsAdjusting(false);
		            setSelection(index0, index1);
		        }
		    }
		    private void setSelection(int index0, int index1){
		        if(super.isSelectedIndex(index0)) {
		            super.removeSelectionInterval(index0, index1);
		        }else {
		            super.addSelectionInterval(index0, index1);
		        }
		    }
			/*@Override
		    public void setSelectionInterval(int index0, int index1) {
		        if(super.isSelectedIndex(index0)) {
		            super.removeSelectionInterval(index0, index1);
		        }
		        else {
		            super.addSelectionInterval(index0, index1);
		        }
		    }*/
		});
		scrollPane.setViewportView(lstOptions);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new EmptyBorder(0, 0, 7, 7));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnOK = new JButton("OK");
				btnOK.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeDisplay(false);
					}
				});
				btnOK.setActionCommand("OK");
				buttonPane.add(btnOK);
				getRootPane().setDefaultButton(btnOK);
			}
			{
				JButton btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeDisplay(true);
					}
				});
				btnCancel.setActionCommand("Cancel");
				buttonPane.add(btnCancel);
			}
		}
	}
	
	public List<K> getUserInput(Component relative, String instructions, List<K> options, Set<K> selected) {
		this.lblInstructions.setText(instructions);
		this.cancelled = false;
		this.lstOptions.setItems(options);
		for (Displayable option : selected) {
			this.lstOptions.setSelectedValue(option, false);
		}
		pack();
		setLocationRelativeTo(relative);
		setVisible(true);
		
		if (cancelled) {
			return null;
		} else {
			return this.lstOptions.getSelectedMult();
		}
	}
	
	public void removeDisplay(boolean cancel) {
		this.cancelled = cancel;
		setVisible(false);
	}
	
	public class CheckboxListCellRenderer<T extends Displayable> extends JCheckBox implements ListCellRenderer<T> {

		private static final long serialVersionUID = -912156212712462547L;

		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, 
	            boolean isSelected, boolean cellHasFocus) {
			
			
	        setComponentOrientation(list.getComponentOrientation());
	        setBackground(list.getBackground());
	        setSelected(isSelected);
	        setEnabled(list.isEnabled());

	        setText(value == null ? "" : value.getListDisplayText());
	        setFont(value == null ? list.getFont() : value.getListDisplayFont());
	        setForeground(value == null ? list.getForeground() : value.getListDisplayColor());
	        
	        
	        return this;
	    }
	}
	

}



