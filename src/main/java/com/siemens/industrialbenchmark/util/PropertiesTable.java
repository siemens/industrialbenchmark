/**
Copyright 2017 Siemens AG.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.siemens.industrialbenchmark.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * This class is a JTable subclass that displays a table of Properties.
 */
public class PropertiesTable extends JTable {

	public PropertiesTable(final Properties properties) {

		setModel(new PropertiesTableModel(properties));

		// Tweak the appearance of the table by manipulating its column model
		TableColumnModel colmodel = getColumnModel();

		// Set column widths
		colmodel.getColumn(0).setPreferredWidth(200);
		colmodel.getColumn(1).setPreferredWidth(200);

		// Right justify the text in the first column
		TableColumn namecol = colmodel.getColumn(0);
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		namecol.setCellRenderer(renderer);
	}

	public Properties getProperties() {
		return ((PropertiesTableModel) getModel()).getProperties();
	}

	public void setProperty(final String key, final Object value) {

		final PropertiesTableModel model = (PropertiesTableModel) getModel();
		model.setValueAt(value, model.getPropertyRow(key), 1);
		repaint();
	}

	private static class PropertiesTableModel extends AbstractTableModel {

		// These are the names of the columns represented by this TableModel
		private static final String[] COLUMN_NAMES = new String[] {"Name", "Value"};

		// These are the types of the columns represented by this TableModel
		private static final Class[] COLUMN_TYPES = new Class[] {String.class, Object.class};

		private final Properties properties;
		private final List<String> propertiesKeys;
		private final List<Object> propertiesValues;

		public PropertiesTableModel(final Properties properties) {

			this.properties = properties;
			this.propertiesKeys = new ArrayList<>(properties.size());
			this.propertiesValues = new ArrayList<>(properties.size());
			for (final Map.Entry<Object, Object> propertyEntry : properties.entrySet()) {
				propertiesKeys.add((String) propertyEntry.getKey());
				propertiesValues.add(propertyEntry.getValue());
			}
		}

		public Properties getProperties() {
			return properties;
		}

		public int getPropertyRow(final String key) {
			return propertiesKeys.indexOf(key);
		}

		// These simple methods return basic information about the table
		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		@Override
		public int getRowCount() {
			return properties.size();
		}

		@Override
		public String getColumnName(final int column) {
			return COLUMN_NAMES[column];
		}

		@Override
		public Class getColumnClass(final int column) {
			return COLUMN_TYPES[column];
		}

		/**
		 * This method returns the value that appears at the specified row and
		 * column of the table
		 */
		@Override
		public Object getValueAt(final int row, final int column) {

			switch (column) {
				case 0:
					return propertiesKeys.get(row);
				case 1:
					return propertiesValues.get(row);
				default:
					return null;
			}
		}

		@Override
		public boolean isCellEditable(final int row, final int column) {

			switch (column) {
				case 0:
					return false;
				case 1:
					return true;
				default:
					throw new IllegalStateException();
			}
		}

		@Override
		public void setValueAt(final Object value, final int row, final int column) {

			switch (column) {
				case 0:
					throw new IllegalStateException();
				case 1:
					propertiesValues.set(row, value);
					properties.setProperty(propertiesKeys.get(row), String.valueOf(value));
					break;
				default:
					throw new IllegalStateException();
			}
		}
	}
}
