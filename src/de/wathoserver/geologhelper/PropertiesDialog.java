package de.wathoserver.geologhelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class PropertiesDialog extends JDialog {
	private Preferences prefs;
	public static final String PROP_ROOTDIR = "rootdir";
	public static final String PROP_TEAM_PRAEFIX = "team";
	public static final String PROP_DELIMITER = ".";
	private JButton saveBtn;
	private JTextField dirField;
	private JTextArea area;

	public PropertiesDialog() {
		loadPrefs();
		setModal(true);
		setTitle("Einstellungen");
		// add(createInfoLabel(), BorderLayout.NORTH);
		add(createDirChoosePanel(), BorderLayout.PAGE_START);
		add(createTeamArea(), BorderLayout.CENTER);
		add(createBtnPanel(), BorderLayout.PAGE_END);
		setPreferredSize(new Dimension(500, 300));
		pack();
		updateUi();
	}

	private Component createBtnPanel() {
		JPanel panel = new JPanel();
		JButton cancelBtn = new JButton("Abbrechen");
		cancelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PropertiesDialog.this.dispose();
			}
		});
		panel.add(cancelBtn);
		saveBtn = new JButton("Speichern");
		saveBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				savePrefs();
				PropertiesDialog.this.dispose();
			}
		});
		panel.add(saveBtn);
		return panel;
	}

	private Component createDirChoosePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Verzeichnis"));
		panel.setLayout(new BorderLayout());
		panel.add(
				new JLabel(
						"<html><p>Damit das Programm ordnungsgemäß funktioniert, muss mindestens das Datenverzeichnis von Geolog angegeben werden.</p></html>"),
				BorderLayout.PAGE_START);
		dirField = new JTextField(prefs.get(PROP_ROOTDIR,
				"Datenverzeichnis auswählen"));
		panel.add(dirField, BorderLayout.CENTER);
		JButton openDir = new JButton("Öffnen");
		openDir.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (validateProps()) {
					fc.setCurrentDirectory(new File(dirField.getText()));
				}
				if (fc.showOpenDialog(PropertiesDialog.this) == JFileChooser.APPROVE_OPTION) {
					dirField.setText(fc.getSelectedFile().getAbsolutePath());
					updateUi();
				}

			}
		});
		panel.add(openDir, BorderLayout.LINE_END);
		return panel;
	}

	private Component createTeamArea() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Teams"));
		panel.setLayout(new BorderLayout());
		StringBuilder sb = new StringBuilder();
		for (String s : readTeams()) {
			sb.append(s).append("\n");
		}
		area = new JTextArea(sb.toString());
		panel.add(
				new JLabel(
						"<html><p>Die folgende Liste enthält zeilenweise alle Auswahlmöglichkeiten für das Teamauswahlfeld. Mehrere Teams werden durch Leerzeichen getrennt.</p><p>Vor jedem Teamname muss ein + stehen.</p></html>"),
				BorderLayout.PAGE_START);
		panel.add(new JScrollPane(area), BorderLayout.CENTER);
		return panel;
	}

	protected ArrayList<String> readTeams() {
		ArrayList<String> teams = new ArrayList<String>();
		try {
			for (String prefKey : prefs.keys()) {
				if (prefKey.startsWith(PROP_TEAM_PRAEFIX)) {
					String value = prefs.get(prefKey, null);
					if (value != null) {
						String[] s = prefKey.split("\\.");
						if (s != null && s.length > 1
								&& s[0].equals(PROP_TEAM_PRAEFIX)) {
							teams.add(prefs.get(prefKey, ""));
						}
					}
				}
			}
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (teams.isEmpty()) {
			teams.add("+DieApomatiker");
		}
		return teams;
	}

	private void updateUi() {
		saveBtn.setEnabled(validateProps());
	}

	public boolean validateProps() {
		String dir = dirField.getText();
		if (dir == null || dir.length() <= 0) {
			return false;
		}
		File root = new File(dir);
		if (root == null || !root.exists() || !root.isDirectory()) {
			return false;
		}
		if (!new File(root, "found").exists()
				|| !new File(root, "not-found").exists()) {
			return false;
		}
		return true;
	}

	public void loadPrefs() {
		prefs = Preferences.userNodeForPackage(this.getClass());
	}

	private void savePrefs() {
		if (prefs != null) {
			try {
				prefs.clear();
				prefs.put(PROP_ROOTDIR, dirField.getText());
				String[] teams = area.getText().split("\n");
				for (int i = 0; i < teams.length; i++) {
					String t = teams[i];
					prefs.put(PROP_TEAM_PRAEFIX + PROP_DELIMITER + i, t);
				}
				prefs.flush();
			} catch (BackingStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Preferences getPrefs() {
		return prefs;
	}

	public File getRootDirFile() {
		return new File(dirField.getText());
	}
}
