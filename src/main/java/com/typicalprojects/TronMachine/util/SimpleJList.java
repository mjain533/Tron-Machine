/*
 * (C) Copyright 2018 Justin Carrington.
 *
 *  This file is part of TronMachine.

 *  TronMachine is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TronMachine is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with TronMachine.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Justin Carrington
 *     Russell Taylor
 *     Kendra Taylor
 *     Erik Dent
 *     
 */
package com.typicalprojects.TronMachine.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import java.awt.Point;


public class SimpleJList<K> extends JList<K>{

	private static final long serialVersionUID = -8945321883556091433L;
	private final MyListModel<K> listModel;
	private DropTarget dropTarget;
	private DropTargetHandler dropTargetHandler;
	private Point dragPoint;

	private boolean dragOver = false;
	private BufferedImage target;
	private boolean dragDropEnabled = false;
	private ListDropReceiver dropReceiver = null;

	public SimpleJList() {
		super();
		this.listModel = new MyListModel<K>();
		setModel(this.listModel);


	}
	
	class MyListModel<T> extends DefaultListModel<T>
	{

		private static final long serialVersionUID = -80559302127736463L;

		public void update() {
	        fireContentsChanged(this, 0, this.size() - 1);
	    }

	}

	public SimpleJList(ListDropReceiver listener) {
		this();
		this.dropReceiver = listener;
		if (listener != null) {
			this.dragDropEnabled = true;
		}


	}

	public SimpleJList(ListDropReceiver listener, ListCellRenderer<K> renderer) {
		this(listener);
		setCellRenderer(renderer);

	}
	
	public SimpleJList(ListCellRenderer<K> renderer) {
		this();
		setCellRenderer(renderer);

	}

	public void setSelectedIndexScroll(int index) {
		if (index < 0)
			return;
		if (index < this.listModel.size()) {
			setSelectedValue(this.listModel.get(index), true);
		}
	}
	
	public void refresh() {
		this.listModel.update();
	}

	public K getElementAt(int index) {
		return this.listModel.getElementAt(index);
	}

	public SimpleJList(SimpleJList<K> copy) {
		this();
		for (int i = 0; i < copy.listModel.size(); i++) {
			this.addItem(copy.listModel.getElementAt(i));
		}
	}

	public Enumeration<K> getElements() {
		return this.listModel.elements();
	}


	public List<K> toList() {
		List<K> list = new ArrayList<K>();
		Enumeration<K> elements = getElements();
		while (elements.hasMoreElements()) {
			list.add(elements.nextElement());
		}
		return list;
	}

	public void clear() {
		clearSelection();
		this.listModel.clear();
	}

	public List<K> getSelectedMult() {
		return getSelectedValuesList();
	}

	public void addItem(K item) {

		this.listModel.addElement(item);

	}

	public int getListSize() {
		return this.listModel.size();
	}

	public boolean isEmpty() {
		return this.listModel.isEmpty();
	}

	public void removeItem(K item) {

		boolean exists = true;
		while (exists) {
			exists = this.listModel.removeElement(item);
		}
	}

	public void removeItems(Collection<K> items) {
		for (K item : items) {
			removeItem(item);
		}
	}

	public void addItems(K[] items) {
		for (K item : items) {
			addItem(item);
		}
	}

	public void setItems(List<K> items) {

		clear();

		for (K item : items) {
			addItem(item);
		}

	}

	public void copyTo(SimpleJList<K> otherList, boolean keepSelection) {
		otherList.listModel.clear();
		for (int i=0; i < this.listModel.getSize(); i++) {
			otherList.listModel.addElement(this.listModel.elementAt(i));
		}
		if (keepSelection && this.getSelectedIndex() > -1) {
			otherList.setSelectedIndex(this.getSelectedIndex());
			otherList.scrollRectToVisible(this.getVisibleRect());

		}
	}

	public void setLastSelected() {
		if (!this.listModel.isEmpty()) {
			setSelectedIndexScroll(this.listModel.getSize() - 1);
		}
	}



	protected DropTarget getMyDropTarget() {
		if (dropTarget == null) {
			dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null);
		}
		return dropTarget;
	}

	protected DropTargetHandler getDropTargetHandler() {
		if (dropTargetHandler == null) {
			dropTargetHandler = new DropTargetHandler();
		}
		return dropTargetHandler;
	}

	@Override
	public void addNotify() {


		super.addNotify();

		if (dragDropEnabled) {
			try {
				getMyDropTarget().addDropTargetListener(getDropTargetHandler());
			} catch (TooManyListenersException ex) {
				ex.printStackTrace();
			}
		}

	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		if (dragDropEnabled) {
			getMyDropTarget().removeDropTargetListener(getDropTargetHandler());
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (dragOver) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setColor(new Color(0, 255, 0, 64));
			g2d.fill(new Rectangle(getWidth(), getHeight()));
			if (dragPoint != null && target != null) {
				int x = dragPoint.x - 12;
				int y = dragPoint.y - 12;
				g2d.drawImage(target, x, y, this);
			}
			g2d.dispose();
		}
	}

	protected void importFiles(final List<Object> objects) {
		Runnable run = new Runnable() {
			@Override
			public void run() {

				dropReceiver.dropped(objects);
			}
		};
		SwingUtilities.invokeLater(run);
	}

	protected class DropTargetHandler implements DropTargetListener {

		protected void processDrag(DropTargetDragEvent dtde) {
			if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				dtde.acceptDrag(DnDConstants.ACTION_COPY);
			} else {
				dtde.rejectDrag();
			}
		}

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			processDrag(dtde);
			SwingUtilities.invokeLater(new DragUpdate(true, dtde.getLocation()));
			repaint();
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			processDrag(dtde);
			SwingUtilities.invokeLater(new DragUpdate(true, dtde.getLocation()));
			repaint();
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			SwingUtilities.invokeLater(new DragUpdate(false, null));
			repaint();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void drop(DropTargetDropEvent dtde) {

			SwingUtilities.invokeLater(new DragUpdate(false, null));

			Transferable transferable = dtde.getTransferable();
			if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				dtde.acceptDrop(dtde.getDropAction());
				try {

					Object transferDataOb =  transferable.getTransferData(DataFlavor.javaFileListFlavor);

					if (transferDataOb instanceof List) {
						List<Object> transferData = (List<Object>) transferDataOb;
						if (transferData != null && transferData.size() > 0) {
							importFiles(transferData);
							dtde.dropComplete(true);
						}
					}


				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				dtde.rejectDrop();
			}
		}

		public class DragUpdate implements Runnable {

			private boolean dragOver;
			private Point dragPoint;

			public DragUpdate(boolean dragOver, Point dragPoint) {
				this.dragOver = dragOver;
				this.dragPoint = dragPoint;
			}


			@Override
			public void run() {
				SimpleJList.this.dragOver = dragOver;
				SimpleJList.this.dragPoint = dragPoint;
				SimpleJList.this.repaint();
			}
		}
	}

	public interface ListDropReceiver {

		public void dropped(List<Object> dropped);

	}
	
	public interface ReorderListener {
		
		public boolean reorderAllowed();
		
		public void handleSwap(int index, int newIndex);
		
	}

}

