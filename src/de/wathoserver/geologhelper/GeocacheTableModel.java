/*******************************************************************************
 * Copyright 2011 WaTho
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.wathoserver.geologhelper;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

@SuppressWarnings("serial")
public class GeocacheTableModel extends AbstractTableModel {
	private final List<Geocache> items;
	private final Map<Integer, String> cols;

	public GeocacheTableModel() {
		items = new ArrayList<Geocache>();
		cols = new HashMap<Integer, String>();
		cols.put(0, Geocache.GCID);
		cols.put(1, Geocache.OCID);
		cols.put(2, Geocache.NAME);
		cols.put(3, Geocache.FOUND);
		cols.put(4, Geocache.AUXSORT);
		cols.put(5, Geocache.ACCESS);
		cols.put(6, Geocache.OPTIONAL1);
	}

	public void add(List<Geocache> newCaches) {
		int first = items.size();
		int last = first + newCaches.size() - 1;
		items.addAll(newCaches);
		fireTableRowsInserted(first, last);
	}

	public int getRowCount() {
		return items.size();
	}

	public int getColumnCount() {
		return cols.size();
	}

	// @Override
	// public Class getColumnClass(int column) {
	// return getValueAt(0, column).getClass();
	// }

	public Geocache getGeocache(int row) {
		return items.get(row);
	}

	@Override
	public Object getValueAt(int row, int column) {
		Geocache cache = items.get(row);
		try {
			return cache.getCacheAttribute(cols.get(column));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		String prop = cols.get(columnIndex);
		return prop.equals(Geocache.ACCESS) || prop.equals(Geocache.AUXSORT)
				|| prop.equals(Geocache.OPTIONAL1);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Geocache cache = items.get(rowIndex);
		try {
			cache.setCacheAttribute(cols.get(columnIndex), (String) aValue);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	// <snip>Initialize table columns
	public TableColumnModel createColumnModel() {
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();

		// TableCellRenderer cellRenderer = new
		// GeocacheTableModel.RowRenderer();

		TableColumn column = new TableColumn();
		column.setModelIndex(0);
		column.setHeaderValue(cols.get(0));
		column.setPreferredWidth(60);
		// column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumn();
		column.setModelIndex(1);
		column.setHeaderValue(cols.get(1));
		column.setPreferredWidth(60);
		// column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumn();
		column.setModelIndex(2);
		column.setHeaderValue(cols.get(2));
		column.setPreferredWidth(190);
		// column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumn();
		column.setModelIndex(3);
		column.setHeaderValue(cols.get(3));
		column.setPreferredWidth(100);
		// column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumn();
		column.setModelIndex(4);
		column.setHeaderValue(cols.get(4));
		column.setPreferredWidth(40);
		// column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumn();
		column.setModelIndex(5);
		column.setHeaderValue(cols.get(5));
		column.setPreferredWidth(80);
		// column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumn();
		column.setModelIndex(6);
		column.setHeaderValue("Team");
		column.setPreferredWidth(100);
		// column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);
		return columnModel;
	}

	public static class RowRenderer extends DefaultTableCellRenderer {
		private Color rowColors[];

		public RowRenderer() {
			// initialize default colors from look-and-feel
			rowColors = new Color[1];
			rowColors[0] = UIManager.getColor("Table.background");
		}

		public RowRenderer(Color colors[]) {
			super();
			setRowColors(colors);
		}

		public void setRowColors(Color colors[]) {
			rowColors = colors;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			Geocache gc = ((GeocacheTableModel) table.getModel())
					.getGeocache(row);
			if (gc.isChanged()) {
				setBackground(Color.RED);
			}
			return this;
		}

		public boolean isOpaque() {
			return true;
		}
	}
}
