/*
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
package com.siemens.industrialbenchmark;

import com.siemens.industrialbenchmark.util.PropertiesTable;
import com.siemens.industrialbenchmark.datavector.state.MarkovianStateDescription;
import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;
import com.siemens.industrialbenchmark.util.PlotCurve;
import info.monitorenter.gui.chart.Chart2D;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * A comfortable GUI to play around with a random simulation.
 */
public class TrialGuiMain extends javax.swing.JFrame {

	private static final String PRIMARY_INPUT_VAR = "STATIONARY_SETPOINT";
	private static final String PRIMARY_OUTPUT_VAR = "RewardTotal";

	private static final String PREF_SAVE_FILE_TEMPLATE = "saveFileTemplate";
	private static final String SAVE_FILE_TEMPLATE_DEFAULT
			= System.getProperty("user.home")
			+ "/industrialBenchmarkSimResults_"
			+ "SetPoint${STATIONARY_SETPOINT}_"
			+ "Seed${SEED}_"
			+ "Time${time:yyyy-MM-dd_HH:mm:ss:SSS}.csv"; // see http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html

	private static final String PREF_AUTO_SAVE = "autoSave";
	private static final boolean AUTO_SAVE_DEFAULT = false;

	private static final String PREF_SIMULATION_STEPS = "simulationSteps";
	private static final Integer SIMULATION_STEPS_DEFAULT = 1500;

	private static final String PREF_SHOWN_OUTPUT = "shownOutput";
	private static final String SHOWN_OUTPUT_DEFAULT = PRIMARY_OUTPUT_VAR;

	private Map<String, List<Double>> states;
	private final PropertiesTable simPropsTable;
	private final Preferences preferences;

	public TrialGuiMain() {

		this.states = null;
		this.preferences = Preferences.userNodeForPackage(TrialGuiMain.class);

		initComponents();

		this.setSize(1024, 600);

		final Properties simulationProps = loadDefaultSimulationProperties();
		this.simPropsTable = new PropertiesTable(simulationProps);
		this.simPropsSP.getViewport().add(simPropsTable);

		final DefaultComboBoxModel<String> inputVarsKeysModel = new DefaultComboBoxModel<>();
		inputVarsKeysModel.removeAllElements();
		final Enumeration<Object> simulationPropKeys = simulationProps.keys();
		while (simulationPropKeys.hasMoreElements()) {
			inputVarsKeysModel.addElement((String) simulationPropKeys.nextElement());
		}
		this.propNumberSliderCB.setModel(inputVarsKeysModel);
		final InputVarKeyChangeListener inputVarKeyChangeListener = new InputVarKeyChangeListener();
		inputVarKeyChangeListener.setFromTo(PRIMARY_INPUT_VAR, 0, 100); // HACK
		this.propNumberSliderCB.addItemListener(inputVarKeyChangeListener);
		this.propNumberSliderCB.setSelectedItem(PRIMARY_INPUT_VAR);

		final SimulationRunAction simulationRunAction = new SimulationRunAction();

		this.propNumberSliderS.addChangeListener(new InputVarSliderChangeListener(simulationRunAction));
		this.propNumberSliderS.setMajorTickSpacing(10);
		this.propNumberSliderS.setMinorTickSpacing(5);

		final FromToChangeListener fromToChangeListener = new FromToChangeListener();
		addChangeListener(this.propNumberSliderFrom, fromToChangeListener);
		addChangeListener(this.propNumberSliderTo, fromToChangeListener);

		this.runB.setAction(simulationRunAction);

		final DefaultComboBoxModel<String> outputStateKeysModel = new DefaultComboBoxModel<>();
		outputStateKeysModel.removeAllElements();
		for (final String key : MarkovianStateDescription.getNonConvolutedInternalVariables()) {
			outputStateKeysModel.addElement(key);
		}
		this.shownStateKeyCB.setModel(outputStateKeysModel);
		this.shownStateKeyCB.setSelectedItem(PRIMARY_OUTPUT_VAR);
		this.shownStateKeyCB.addItemListener(new OutputVarKeyChangeListener());

		final String safeFileTemplate = this.preferences.get(PREF_SAVE_FILE_TEMPLATE, SAVE_FILE_TEMPLATE_DEFAULT);
		final StringBuilder tt = new StringBuilder(1024 + (simulationProps.size() * 64));
		tt
				.append("<html>\n"
						+ "<h3>File-Name template for the results in CSV format</h3>\n"
						+ "The file-name has to end in \".csv\",<br>\n"
						+ " and may use any of the following variables,<br>\n"
						+ " which will be substituted to create the actual file name:<br>\n"
						+ "<ul>\n"
						+ "<li>${time:<i>format</i>} (special), gets replaced with the time at the end of the simulation,<br>\n"
						+ " according to the given <i>format</i><br>\n"
						+ " (see <a href=\"http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html\">the Javadoc</a> for formatting details).<br>\n"
						+ " example: \"${time:yyyy-MM-dd_HH:mm:ss:SSS}\"\n"
						+ "</li>\n");
		for (final String key : simulationProps.stringPropertyNames()) {
			tt.append("<li>${").append(key).append("}</li>\n");
		}
		tt
				.append("</ul>\n"
						+ "</html>\n");
		this.saveFileNameTF.setToolTipText(tt.toString());
		this.saveFileNameTF.setText(safeFileTemplate);
		this.saveFileNameTF.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(final DocumentEvent evt) {
				textChanged();
			}

			@Override
			public void removeUpdate(final DocumentEvent evt) {
				textChanged();
			}

			@Override
			public void insertUpdate(final DocumentEvent evt) {
				textChanged();
			}

			private void textChanged() {
				preferences.put(PREF_SAVE_FILE_TEMPLATE, saveFileNameTF.getText());
			}
		});

		this.saveFileChooserB.setAction(new ChooseSaveFileAction());
		this.saveAutoCB.setAction(new AutoSaveAction(new JComponent[] {saveFileNameTF, saveFileChooserB}));

		final boolean autoSave = this.preferences.getBoolean(PREF_AUTO_SAVE, AUTO_SAVE_DEFAULT);
		this.saveAutoCB.setSelected(autoSave);
		this.saveFileNameTF.setEnabled(autoSave);
		this.saveFileChooserB.setEnabled(autoSave);

		final int nSimSteps = this.preferences.getInt(PREF_SIMULATION_STEPS, SIMULATION_STEPS_DEFAULT);
		this.nSimStepsS.setValue(nSimSteps);

		final String shownOutputVar = this.preferences.get(PREF_SHOWN_OUTPUT, SHOWN_OUTPUT_DEFAULT);
		this.shownStateKeyCB.setSelectedItem(shownOutputVar);

		simulationRunAction.actionPerformed(null);
	}

	public static String formatSaveFileName(final String saveFileNameTemplate, final Properties inputParams) {

		final Date now = new Date();
		String saveFileName = saveFileNameTemplate;
		while (saveFileName.contains("${")) {
			final int startIndex = saveFileName.indexOf("${");
			final int endIndex = saveFileName.indexOf('}', startIndex) + 1;
			final String replaceVarRaw = saveFileName.substring(startIndex, endIndex);
			final String replaceVar = replaceVarRaw.substring(2, replaceVarRaw.length() - 1);
			final String replaceValue;
			if (replaceVar.startsWith("time:")) {
				final String timeFormat = replaceVar.substring(replaceVar.indexOf(':') + 1);
				final String formattedTime = new SimpleDateFormat(timeFormat).format(now);
				replaceValue = formattedTime;
			} else if (inputParams.containsKey(replaceVar)) {
				replaceValue = inputParams.getProperty(replaceVar);
			} else {
				// do nothing
				replaceValue = replaceVarRaw;
				break;
			}
			saveFileName = saveFileName.replace(replaceVarRaw, replaceValue);
		}
		return saveFileName;
	}

	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document}, and a
	 * {@link java.beans.PropertyChangeListener} on the text component to detect
	 * if the {@code Document} itself is replaced.
	 *
	 * @param text any text component, such as a {@link javax.swing.JTextField}
	 * or {@link javax.swing.JTextArea}
	 * @param changeListener a listener to receive {@link ChangeEvent}s when
	 * the text is changed; the source object for the events will be the text
	 * component
	 * @throws NullPointerException if either parameter is null
	 */
	public static void addChangeListener(final JTextComponent text, final ChangeListener changeListener) {
		Objects.requireNonNull(text);
		Objects.requireNonNull(changeListener);
		final DocumentListener docLst = new DocumentListener() {
			private int lastChange = 0, lastNotifiedChange = 0;

			@Override
			public void insertUpdate(final DocumentEvent evt) {
				changedUpdate(evt);
			}

			@Override
			public void removeUpdate(final DocumentEvent evt) {
				changedUpdate(evt);
			}

			@Override
			public void changedUpdate(final DocumentEvent evt) {
				lastChange++;
				SwingUtilities.invokeLater(() -> {
					if (lastNotifiedChange != lastChange) {
						lastNotifiedChange = lastChange;
						changeListener.stateChanged(new ChangeEvent(text));
					}
				});
			}
		};
		text.addPropertyChangeListener("document", (final PropertyChangeEvent evt) -> {
			final Document doc1 = (Document) evt.getOldValue();
			final Document doc2 = (Document) evt.getNewValue();
			if (doc1 != null) {
				doc1.removeDocumentListener(docLst);
			}
			if (doc2 != null) {
				doc2.addDocumentListener(docLst);
			}
			docLst.changedUpdate(null);
		});
		final Document doc = text.getDocument();
		if (doc != null) {
			doc.addDocumentListener(docLst);
		}
	}

	private static Properties loadDefaultSimulationProperties() {

		// configuration of the properties file
		final String simPropsFilePath = ExampleMain.DEFALT_SIM_PROPS_FILE_PATH; // default filepath
		System.out.println("Using config file: '" + simPropsFilePath + "'");

		// set-point configuration parameters
		try {
			return PropertiesUtil.loadSetPointProperties(new File(simPropsFilePath));
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Properties getSimulationProperties() {
		return simPropsTable.getProperties();
	}

	private int getSimulationSteps() {
		return (Integer) nSimStepsS.getValue();
	}

	private class InputVarKeyChangeListener implements ItemListener {

		private final Map<String, Map<Object, Object>> propKeyFromTo;

		InputVarKeyChangeListener() {

			this.propKeyFromTo = new HashMap<>();
		}

		public void setFromTo(final String propName, final Object from, final Object to) {

			propKeyFromTo.put(propName, Collections.singletonMap(from, to));
		}

		@Override
		public void itemStateChanged(final ItemEvent event) {

			if (event.getStateChange() != ItemEvent.SELECTED) {
				return;
			}

			final String previouslySelectedKey = (String) propNumberSliderCB.getSelectedItem();
			setFromTo(previouslySelectedKey, propNumberSliderFrom.getText(), propNumberSliderTo.getText());

			final String newlySelectedKey = (String) event.getItem();
			final Map<Object, Object> fromTo
					= propKeyFromTo.containsKey(newlySelectedKey)
					? propKeyFromTo.get(newlySelectedKey)
					: Collections.singletonMap(0, 100);
			final Map.Entry<Object, Object> fromToEntry = fromTo.entrySet().iterator().next();
			propNumberSliderFrom.setText(fromToEntry.getKey().toString());
			propNumberSliderTo.setText(fromToEntry.getValue().toString());
//			propNumberSliderS.getModel().setRangeProperties(vale, 1, from, to, false);
			final String valueRaw = simPropsTable.getProperties().getProperty(newlySelectedKey);
			boolean sliderSuport;
			try {
				propNumberSliderS.setValue(Double.valueOf(valueRaw).intValue());
				sliderSuport = true;
			} catch (final NumberFormatException ex) {
				ex.printStackTrace();
				sliderSuport = false;
			}
			propNumberSliderFrom.setEnabled(sliderSuport);
			propNumberSliderS.setEnabled(sliderSuport);
			propNumberSliderTo.setEnabled(sliderSuport);
		}
	}

	private class InputVarSliderChangeListener implements ChangeListener {

		private final Action action;

		InputVarSliderChangeListener(final Action action) {

			this.action = action;
		}

		@Override
		public void stateChanged(final ChangeEvent event) {

			if (propNumberSliderS.getValueIsAdjusting()) {
				return;
			}

			final int newValue = propNumberSliderS.getValue();
			final String selectedProp = (String) propNumberSliderCB.getSelectedItem();
			simPropsTable.setProperty(selectedProp, String.valueOf(newValue));

			action.actionPerformed(null);
		}
	}

	private class FromToChangeListener implements ChangeListener {

		@Override
		public void stateChanged(final ChangeEvent event) {

			try {
				final Double from = Double.valueOf(propNumberSliderFrom.getText());
				final Double to = Double.valueOf(propNumberSliderTo.getText());
				propNumberSliderS.setMinimum((int) Math.ceil(from));
				propNumberSliderS.setMaximum((int) Math.floor(to));

				propNumberSliderS.setMajorTickSpacing((int) ((to - from) / 10));
				propNumberSliderS.setMinorTickSpacing((int) ((to - from) / 20));
			} catch (final NumberFormatException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage(), "Failed to set from & to", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private class OutputVarKeyChangeListener implements ItemListener {

		@Override
		public void itemStateChanged(final ItemEvent event) {

			if (states == null) {
				return;
			}

			if (event.getStateChange() != ItemEvent.SELECTED) {
				return;
			}

			final String newlySelectedKey = (String) event.getItem();
			preferences.put(PREF_SHOWN_OUTPUT, newlySelectedKey);
			final List<Double> shownValues = states.get(newlySelectedKey);
			final Chart2D chart = PlotCurve.plotChart("t", newlySelectedKey, shownValues);
			chartSP.getViewport().removeAll();
			chartSP.getViewport().add(chart);
		}
	}

	private class SimulationRunAction extends AbstractAction {

		private final ExecutorService simulationThreadPool;

		SimulationRunAction() {

			this.simulationThreadPool = Executors.newSingleThreadExecutor();
			putValue(NAME, "Run Simulation");
		}

		@Override
		public void actionPerformed(final ActionEvent evt) {

			chartSP.getViewport().removeAll();
			states = null;

			final Properties simulationProperties = getSimulationProperties();
			final int simulationSteps = getSimulationSteps();
			final BoundedRangeModel progessModel = new DefaultBoundedRangeModel(0, 0, 0, simulationSteps);
			simulationProgressPB.setModel(progessModel);
			states = null;
			File csvSaveFile = null;
			if (saveAutoCB.isSelected()) {
				final String saveFileNameTemplate = saveFileNameTF.getText();
				final String saveFileNameFormatted = formatSaveFileName(saveFileNameTemplate, simPropsTable.getProperties());
				csvSaveFile = new File(saveFileNameFormatted);
			}

			preferences.putInt(PREF_SIMULATION_STEPS, getSimulationSteps());

			try {
				final RandomSimulation randomSimulation = new RandomSimulation(simulationSteps, simulationProperties, progessModel, csvSaveFile);
				final Callable<Map<String, List<Double>>> simulationWrapper = new Callable<Map<String, List<Double>>>() {
					@Override
					public Map<String, List<Double>> call() throws Exception {

						final Map<String, List<Double>> result = randomSimulation.call();
						states = result;

						final String shownKey = (String) shownStateKeyCB.getSelectedItem();
						shownStateKeyCB.setSelectedItem(null);
						shownStateKeyCB.setSelectedItem(shownKey);

						return result;
					}
				};
				simulationThreadPool.submit(simulationWrapper);
			} catch (final IOException | PropertiesException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private class ChooseSaveFileAction extends AbstractAction {

		private final JFileChooser fileChooser;

		ChooseSaveFileAction() {

			this.fileChooser = new JFileChooser();
			this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			this.fileChooser.setAcceptAllFileFilterUsed(false);
			this.fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", ".csv"));
			putValue(NAME, "...");
		}

		@Override
		public void actionPerformed(final ActionEvent evt) {

			fileChooser.setSelectedFile(new File(saveFileNameTF.getText()));

			final int usersChoice = fileChooser.showOpenDialog(TrialGuiMain.this);

			if (usersChoice == JFileChooser.APPROVE_OPTION) {
				final File file = fileChooser.getSelectedFile();
				saveFileNameTF.setText(file.getAbsolutePath());
			}
		}
	}

	private class AutoSaveAction extends AbstractAction {

		private final JComponent[] relatedComponents;

		AutoSaveAction(final JComponent... relatedComponents) {

			this.relatedComponents = relatedComponents;
			putValue(NAME, "Auto Save");
		}

		@Override
		public void actionPerformed(final ActionEvent evt) {

			final boolean activated = ((JCheckBox) evt.getSource()).isSelected();
			preferences.putBoolean(PREF_AUTO_SAVE, activated);
			for (final JComponent relatedComponent : relatedComponents) {
				relatedComponent.setEnabled(activated);
			}
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		mainSP = new javax.swing.JSplitPane();
		westP = new javax.swing.JPanel();
		simPropsP = new javax.swing.JPanel();
		simPropsSP = new javax.swing.JScrollPane();
		propNumberSliderP = new javax.swing.JPanel();
		propNumberSliderCB = new javax.swing.JComboBox<>();
		propNumberSliderFrom = new javax.swing.JTextField();
		propNumberSliderS = new javax.swing.JSlider();
		propNumberSliderTo = new javax.swing.JTextField();
		outputP = new javax.swing.JPanel();
		chartSP = new javax.swing.JScrollPane();
		shownStateKeyP = new javax.swing.JPanel();
		shownStateKeyCB = new javax.swing.JComboBox<>();
		bottomP = new javax.swing.JPanel();
		simulationProgressPB = new javax.swing.JProgressBar();
		controllsP = new javax.swing.JPanel();
		nSimStepsS = new javax.swing.JSpinner();
		runB = new javax.swing.JButton();
		saveP = new javax.swing.JPanel();
		saveActionsP = new javax.swing.JPanel();
		saveAutoCB = new javax.swing.JCheckBox();
		saveFileP = new javax.swing.JPanel();
		saveFileNameTF = new javax.swing.JFormattedTextField();
		saveFileChooserB = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Industrial Benchmark - Random Simulation");

		westP.setLayout(new java.awt.BorderLayout());

		simPropsP.setBorder(javax.swing.BorderFactory.createTitledBorder("Simulation Properties"));
		simPropsP.setLayout(new java.awt.BorderLayout());
		simPropsP.add(simPropsSP, java.awt.BorderLayout.CENTER);

		propNumberSliderP.setLayout(new java.awt.BorderLayout());

		propNumberSliderCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
		propNumberSliderP.add(propNumberSliderCB, java.awt.BorderLayout.NORTH);

		propNumberSliderFrom.setText("0");
		propNumberSliderFrom.setPreferredSize(new java.awt.Dimension(60, 23));
		propNumberSliderP.add(propNumberSliderFrom, java.awt.BorderLayout.WEST);

		propNumberSliderS.setPaintLabels(true);
		propNumberSliderS.setPaintTicks(true);
		propNumberSliderP.add(propNumberSliderS, java.awt.BorderLayout.CENTER);

		propNumberSliderTo.setText("100");
		propNumberSliderTo.setPreferredSize(new java.awt.Dimension(60, 23));
		propNumberSliderP.add(propNumberSliderTo, java.awt.BorderLayout.EAST);

		simPropsP.add(propNumberSliderP, java.awt.BorderLayout.SOUTH);

		westP.add(simPropsP, java.awt.BorderLayout.CENTER);

		mainSP.setLeftComponent(westP);

		outputP.setLayout(new java.awt.BorderLayout());
		outputP.add(chartSP, java.awt.BorderLayout.CENTER);

		shownStateKeyCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
		shownStateKeyCB.setToolTipText("Shown Output Value");
		shownStateKeyP.add(shownStateKeyCB);

		outputP.add(shownStateKeyP, java.awt.BorderLayout.SOUTH);

		mainSP.setRightComponent(outputP);

		getContentPane().add(mainSP, java.awt.BorderLayout.CENTER);

		bottomP.setLayout(new java.awt.BorderLayout());

		simulationProgressPB.setToolTipText("Simulation Progress");
		bottomP.add(simulationProgressPB, java.awt.BorderLayout.NORTH);

		nSimStepsS.setModel(new javax.swing.SpinnerNumberModel(1500, 1, null, 500));
		nSimStepsS.setToolTipText("Simulation Steps");
		nSimStepsS.setPreferredSize(new java.awt.Dimension(100, 24));
		controllsP.add(nSimStepsS);

		runB.setText("Run");
		controllsP.add(runB);

		bottomP.add(controllsP, java.awt.BorderLayout.CENTER);

		saveP.setBorder(javax.swing.BorderFactory.createTitledBorder("Save results to CSV"));
		saveP.setLayout(new java.awt.BorderLayout());

		saveAutoCB.setText("Auto Save");
		saveAutoCB.setToolTipText("save the results automatically after every simulation run");
		saveActionsP.add(saveAutoCB);

		saveP.add(saveActionsP, java.awt.BorderLayout.CENTER);

		saveFileP.setLayout(new java.awt.BorderLayout());

		saveFileNameTF.setText("jFormattedTextField1");
		saveFileNameTF.setEnabled(false);
		saveFileP.add(saveFileNameTF, java.awt.BorderLayout.CENTER);

		saveFileChooserB.setText("...");
		saveFileChooserB.setEnabled(false);
		saveFileP.add(saveFileChooserB, java.awt.BorderLayout.EAST);

		saveP.add(saveFileP, java.awt.BorderLayout.NORTH);

		bottomP.add(saveP, java.awt.BorderLayout.SOUTH);

		getContentPane().add(bottomP, java.awt.BorderLayout.SOUTH);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args the command line arguments
	 */
	public static void main(final String[] args) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(TrialGuiMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new TrialGuiMain().setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel bottomP;
	private javax.swing.JScrollPane chartSP;
	private javax.swing.JPanel controllsP;
	private javax.swing.JSplitPane mainSP;
	private javax.swing.JSpinner nSimStepsS;
	private javax.swing.JPanel outputP;
	private javax.swing.JComboBox<String> propNumberSliderCB;
	private javax.swing.JTextField propNumberSliderFrom;
	private javax.swing.JPanel propNumberSliderP;
	private javax.swing.JSlider propNumberSliderS;
	private javax.swing.JTextField propNumberSliderTo;
	private javax.swing.JButton runB;
	private javax.swing.JPanel saveActionsP;
	private javax.swing.JCheckBox saveAutoCB;
	private javax.swing.JButton saveFileChooserB;
	private javax.swing.JFormattedTextField saveFileNameTF;
	private javax.swing.JPanel saveFileP;
	private javax.swing.JPanel saveP;
	private javax.swing.JComboBox<String> shownStateKeyCB;
	private javax.swing.JPanel shownStateKeyP;
	private javax.swing.JPanel simPropsP;
	private javax.swing.JScrollPane simPropsSP;
	private javax.swing.JProgressBar simulationProgressPB;
	private javax.swing.JPanel westP;
	// End of variables declaration//GEN-END:variables
}
