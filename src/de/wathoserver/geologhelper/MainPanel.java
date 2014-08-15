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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;

@SuppressWarnings("serial")
public class MainPanel extends JPanel {

	private static final String[] AUXSORT = new String[] { "", "01", "02", "03", "04", "05", "06", "07", "08", "09",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27",
			"28", "29", "30" };
	private static final String[] ACCESS = new String[] { "", "hike.gif", "bike.gif", "car.gif", "bus.gif", "couch.gif" };
	private JTextField filterField;
	private JCheckBox newCheckbox;
	private Box statusBarLeft;
	private JLabel actionStatus;
	private JLabel tableStatus;
	private final GeocacheTableModel tmodel;
	private boolean showOnlyNewCaches;
	private String filterString;
	private RowFilter<GeocacheTableModel, Integer> newFilter;
	private RowFilter<GeocacheTableModel, Integer> searchFilter;
	private TableRowSorter<GeocacheTableModel> sorter;
	private JTable table;
	private PropertiesDialog propDialog;
	private JPanel batchPanel;
	private JComboBox auxSortComboBox;
	private JComboBox accessComboBox;
	private JComboBox teamComboBox;
	private JComboBox batchTeam;
	private JComboBox batchAuxSort;
	private JComboBox batchAccess;
	private boolean clearing;

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		} catch (final InstantiationException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		} catch (final UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final JFrame f = new JFrame("Geolog Helper 0.1.1");
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				final MainPanel m = new MainPanel();
				m.loadProperties();
				m.createGui();
				f.add(m);
				f.setSize(1000, 800);
				f.setVisible(true);
				m.loadData();
			}
		});
	}

	public MainPanel() {
		tmodel = new GeocacheTableModel();
	}

	private void loadProperties() {
		propDialog = new PropertiesDialog();
		propDialog.loadPrefs();
		if (!propDialog.validateProps()) {
			JOptionPane
			.showMessageDialog(
					this,
					"Wahrscheinlich startest du das Programm das erste Mal.\nEs müssen einige Einstellungen vorgenommen werden.",
					"Erster Programmstart", JOptionPane.WARNING_MESSAGE);
			propDialog.setVisible(true);
			if (!propDialog.validateProps()) {
				JOptionPane.showMessageDialog(this,
						"Beim Einlesen der Einstellungen ist ein Fehler aufgetreten.\nDas Programm wird beendet.",
						"Fehler", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		}
	}

	private void createGui() {
		setLayout(new BorderLayout());
		final JToolBar toolbar = new JToolBar();
		final Action openAction = new AbstractAction() {
			{
				putValue(Action.NAME, "Einstellungen");
				putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, 1);
				// putValue(Action.SMALL_ICON, smallIcon);
				// putValue(Action.LARGE_ICON_KEY, largeIcon);
			}

			@Override
			public void actionPerformed(final ActionEvent e) {
				propDialog.setVisible(true);
				addTeams();
			}
		};
		toolbar.add(openAction);
		add(toolbar, BorderLayout.PAGE_START);
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(createControlPanel(), BorderLayout.PAGE_START);
		mainPanel.add(createBatchPanel(), BorderLayout.PAGE_END);

		table = new JTable(tmodel);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					clearing = true;
					if (batchAccess != null) {
						batchAccess.setSelectedIndex(0);
					}
					if (batchAuxSort != null) {
						batchAuxSort.setSelectedIndex(0);
					}
					if (batchTeam != null) {
						batchTeam.setSelectedIndex(0);
					}
					clearing = false;
				}
			}
		});
		sorter = new TableRowSorter<GeocacheTableModel>(tmodel);
		// rowsorter for found date
		sorter.setComparator(3, new Comparator<String>() {
			@Override
			public int compare(final String date1, final String date2) {
				final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
				try {
					return sdf.parse(date1).compareTo(sdf.parse(date2));
				} catch (final ParseException e) {
					return 0;
				}
			}
		});
		// rowsorter for auxsort 01, 02, ...
		sorter.setComparator(4, new Comparator<String>() {
			@Override
			public int compare(final String aux1, final String aux2) {
				try {
					return Integer.parseInt(aux1) - Integer.parseInt(aux2);
				} catch (final NumberFormatException e) {
					return 0;
				}
			}
		});
		newFilter = new RowFilter<GeocacheTableModel, Integer>() {

			@Override
			public boolean include(
					final javax.swing.RowFilter.Entry<? extends GeocacheTableModel, ? extends Integer> entry) {
				final GeocacheTableModel model = entry.getModel();
				final Geocache gc = model.getGeocache(entry.getIdentifier().intValue());
				return gc.isNew();
			}
		};
		searchFilter = new RowFilter<GeocacheTableModel, Integer>() {
			@Override
			public boolean include(final Entry<? extends GeocacheTableModel, ? extends Integer> entry) {
				final GeocacheTableModel gcModel = entry.getModel();
				final Geocache cache = gcModel.getGeocache(entry.getIdentifier().intValue());
				boolean matches = false;
				final Pattern p = Pattern.compile(filterString + ".*", Pattern.CASE_INSENSITIVE);

				final String name = cache.getName();
				if (name != null) {
					// Returning true indicates this row should be shown.
					matches = p.matcher(name).matches();
				}
				final String gccode = cache.getCacheAttribute(Geocache.GCID);
				if (gccode != null) {
					// Returning true indicates this row should be shown.
					matches = matches || p.matcher(gccode).matches();
				}
				final String ocid = cache.getCacheAttribute(Geocache.OCID);
				if (ocid != null) {
					// Returning true indicates this row should be shown.
					matches = matches || p.matcher(ocid).matches();
				}
				return matches;
			}
		};
		// rowSorter.setRowFilter(newFilter);
		sorter.setSortable(3, true);
		// bring newest caches to first row
		final List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
		sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
		sorter.setSortKeys(sortKeys);
		// rowSorter.toggleSortOrder(3);
		// rowSorter.toggleSortOrder(3);
		table.setRowSorter(sorter);
		table.setColumnModel(tmodel.createColumnModel());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		final TableCellEditor auxSortEditor = new DefaultCellEditor(getAuxSortComboBox());
		table.getColumnModel().getColumn(4).setCellEditor(auxSortEditor);
		final TableCellEditor accessEditor = new DefaultCellEditor(getAccessComboBox());
		table.getColumnModel().getColumn(5).setCellEditor(accessEditor);
		addTeams();
		mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		mainPanel.add(createStatusBar(), BorderLayout.SOUTH);
		newCheckbox.setSelected(true);
	}

	private JComboBox getAuxSortComboBox() {
		if (auxSortComboBox == null) {
			auxSortComboBox = new JComboBox(AUXSORT);
			auxSortComboBox.setEditable(true);
		}
		return auxSortComboBox;
	}

	private void addTeams() {
		final ArrayList<String> teams = propDialog.readTeams();
		teams.add(0, "");
		teamComboBox = new JComboBox(teams.toArray());
		teamComboBox.setEditable(true);
		final TableCellEditor teamEditor = new DefaultCellEditor(teamComboBox);
		table.getColumnModel().getColumn(6).setCellEditor(teamEditor);
		batchTeam.setModel(new JComboBox(teams.toArray()).getModel());
	}

	protected JPanel createControlPanel() {
		final JPanel controlPanel = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();
		controlPanel.setLayout(gridbag);

		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.insets = new Insets(20, 10, 0, 10);
		c.anchor = GridBagConstraints.SOUTHWEST;
		final JLabel searchLabel = new JLabel("Suche nach Code und Name");
		controlPanel.add(searchLabel, c);

		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1.0;
		c.insets.top = 0;
		c.insets.bottom = 12;
		c.anchor = GridBagConstraints.SOUTHWEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		filterField = new JTextField(24);
		filterField.getDocument().addDocumentListener(new SearchFilterListener());
		controlPanel.add(filterField, c);

		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		// c.insets.right = 24;
		// c.insets.left = 12;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		newCheckbox = new JCheckBox("Zeige nur neue Einträge an");
		newCheckbox.setToolTipText("Zeigt nur Geocaches an, die noch keinen AuxSort-Eintrag haben.");
		newCheckbox.addChangeListener(new ShowNewListener());
		controlPanel.add(newCheckbox, c);

		return controlPanel;
	}

	protected JPanel createBatchPanel() {
		batchPanel = new JPanel();
		batchPanel.setBorder(BorderFactory.createTitledBorder("Massenbearbeitung"));
		batchPanel.setToolTipText("Werte für alle ausgewählte Caches setzen");
		batchPanel.add(new JLabel("AuxSort:"));
		batchAuxSort = new JComboBox(AUXSORT);
		batchAuxSort.putClientProperty("key", Geocache.AUXSORT);
		batchAuxSort.addItemListener(new BatchItemListener());
		batchAuxSort.setEditable(true);
		batchPanel.add(batchAuxSort);
		batchPanel.add(new JLabel("Access:"));
		batchAccess = new JComboBox(ACCESS);
		batchAccess.setEditable(true);
		batchAccess.addItemListener(new BatchItemListener());
		batchAccess.putClientProperty("key", Geocache.ACCESS);
		batchPanel.add(batchAccess);
		batchPanel.add(new JLabel("Team:"));
		batchTeam = new JComboBox();
		batchTeam.setEditable(true);
		batchTeam.addItemListener(new BatchItemListener());
		batchTeam.putClientProperty("key", Geocache.OPTIONAL1);
		batchPanel.add(batchTeam);
		return batchPanel;
	}

	protected Container createStatusBar() {
		final Box statusBar = Box.createHorizontalBox();

		// Left status area
		statusBar.add(Box.createRigidArea(new Dimension(10, 22)));
		statusBarLeft = Box.createHorizontalBox();
		statusBar.add(statusBarLeft);
		actionStatus = new JLabel("Daten nicht geladen");
		actionStatus.setHorizontalAlignment(SwingConstants.LEADING);
		statusBarLeft.add(actionStatus);

		// Middle (should stretch)
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(Box.createVerticalGlue());

		// Right status area
		tableStatus = new JLabel();
		statusBar.add(tableStatus);
		statusBar.add(Box.createHorizontalStrut(12));

		tmodel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(final TableModelEvent e) {
				updateTableStatus();
			}
		});
		return statusBar;
	}

	public void setShowOnlyNew(final boolean showOnlyNew) {
		final boolean oldShowOnlyNew = showOnlyNewCaches;
		showOnlyNewCaches = showOnlyNew;
		configureFilters();
		updateTableStatus();
		firePropertyChange("showOnlyNew", oldShowOnlyNew, showOnlyNew);
	}

	private void updateTableStatus() {
		tableStatus.setText(table.getRowCount() + " von " + tmodel.getRowCount() + " Geocaches");
	}

	public boolean getShowOnlyNew() {
		return showOnlyNewCaches;
	}

	public void setFilterString(final String filterString) {
		final String oldFilterString = this.filterString;
		this.filterString = filterString;
		configureFilters();
		firePropertyChange("filterString", oldFilterString, filterString);
	}

	protected boolean hasFilterString() {
		return filterString != null && !filterString.equals("");
	}

	protected void configureFilters() {
		if (showOnlyNewCaches && hasFilterString()) {
			final List<RowFilter<GeocacheTableModel, Integer>> filters = new ArrayList<RowFilter<GeocacheTableModel, Integer>>(
					2);
			filters.add(newFilter);
			filters.add(searchFilter);
			final RowFilter<GeocacheTableModel, Integer> comboFilter = RowFilter.andFilter(filters);
			sorter.setRowFilter(comboFilter);
		} else if (showOnlyNewCaches) {
			sorter.setRowFilter(newFilter);
		} else if (hasFilterString()) {
			sorter.setRowFilter(searchFilter);
		} else {
			sorter.setRowFilter(null);
		}
		updateTableStatus();

	}

	private class ShowNewListener implements ChangeListener {
		@Override
		public void stateChanged(final ChangeEvent event) {
			setShowOnlyNew(newCheckbox.isSelected());
		}
	}

	// <snip>Setup search filter
	protected class SearchFilterListener implements DocumentListener {
		protected void changeFilter(final DocumentEvent event) {
			final Document document = event.getDocument();
			try {
				setFilterString(document.getText(0, document.getLength()));

			} catch (final Exception ex) {
				ex.printStackTrace();
				System.err.println(ex);
			}
		}

		@Override
		public void changedUpdate(final DocumentEvent e) {
			changeFilter(e);
		}

		@Override
		public void insertUpdate(final DocumentEvent e) {
			changeFilter(e);
		}

		@Override
		public void removeUpdate(final DocumentEvent e) {
			changeFilter(e);
		}
	}

	// <snip>Use SwingWorker to asynchronously load the data
	public void loadData() {
		// create SwingWorker which will load the data on a separate thread
		final GeocacheLoader loader = new GeocacheLoader(propDialog.getRootDirFile(), tmodel);
		actionStatus.setText("Lade Geocaches ");
		// display progress bar while data loads
		final JProgressBar progressBar = new JProgressBar();
		statusBarLeft.add(progressBar);
		loader.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if (event.getPropertyName().equals("progress")) {
					final int progress = ((Integer) event.getNewValue()).intValue();
					progressBar.setValue(progress);

					if (progress == 100) {
						statusBarLeft.remove(progressBar);
						actionStatus.setText("");
						revalidate();
					}
				}
			}
		});
		loader.execute();
	}

	private JComboBox getAccessComboBox() {
		if (accessComboBox == null) {
			accessComboBox = new JComboBox(ACCESS);
			getAccessComboBox().setEditable(true);
		}
		return accessComboBox;
	}

	private class BatchItemListener implements ItemListener {

		@Override
		public void itemStateChanged(final ItemEvent e) {
			if (!clearing) {
				final JComboBox selectedChoice = (JComboBox) e.getSource();
				if (e.getStateChange() == ItemEvent.SELECTED) {
					for (final int tableIndex : table.getSelectedRows()) {
						final int modelIndex = table.convertRowIndexToModel(tableIndex);
						final GeocacheTableModel model = (GeocacheTableModel) table.getModel();
						model.getGeocache(modelIndex).setCacheAttribute(
								(String) selectedChoice.getClientProperty("key"),
								(String) selectedChoice.getSelectedItem());
						model.fireTableRowsUpdated(modelIndex, modelIndex);
					}
				}
			}
		}
	}

	private class GeocacheLoader extends SwingWorker<List<Geocache>, Geocache> {
		private final File gcData;
		private final GeocacheTableModel gcModel;
		private final List<Geocache> caches = new ArrayList<Geocache>();

		private GeocacheLoader(final File gcFile, final GeocacheTableModel gcModel) {
			gcData = gcFile;
			this.gcModel = gcModel;
		}

		@Override
		public List<Geocache> doInBackground() {
			try {
				final FoundReader reader = new FoundReader(gcData) {
					@Override
					protected void addGeocache(final Geocache cache) {
						caches.add(cache);
						publish(cache);
						setProgress(100 * caches.size() / this.getNumberOfFounds());
					}
				};

				reader.readFoundCaches();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return caches;
		}

		@Override
		protected void process(final List<Geocache> caches) {
			gcModel.add(caches);
		}

		@Override
		protected void done() {
			setProgress(100);
			updateTableStatus();
		}
	}
}
