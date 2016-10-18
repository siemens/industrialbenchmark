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
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;

/**
 * A comfortable GUI to play around with a random simulation.
 */
public class TrialGuiMain extends javax.swing.JFrame {

	private Map<String, List<Double>> states;
	private final PropertiesTable simPropsTable;

	public TrialGuiMain() {

		this.states = null;

		initComponents();

		this.setSize(1024, 600);

		this.simPropsTable = new PropertiesTable(loadDefaultSimulationProperties());
		this.simPropsSP.getViewport().add(simPropsTable);

		this.runB.setAction(new SimulationRunAction());

		final DefaultComboBoxModel<String> stateKeysModel = new DefaultComboBoxModel<>();
		stateKeysModel.removeAllElements();
		for (final String key : MarkovianStateDescription.getNonConvolutedInternalVariables()) {
			stateKeysModel.addElement(key);
		}
		this.shownStateKeyCB.setModel(stateKeysModel);
		this.shownStateKeyCB.addItemListener(new KeyChangeListener());
	}

	private static Properties loadDefaultSimulationProperties() {

		// configuration of the properties file
		final String simPropsFilePath = ExampleMain.DEFALT_SIM_PROPS_FILE_PATH; // default filepath
		System.out.println("Using config file: '" + simPropsFilePath + "'");

		// set-point configuration parameters
		Properties props = null;
		try {
			props = PropertiesUtil.setpointProperties(new File(simPropsFilePath));
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}

		return props;
	}

	private Properties getSimulationProperties() {
		return simPropsTable.getProperties();
	}

	private int getSimulationSteps() {
		return (Integer) nSimStepsS.getValue();
	}

	private class KeyChangeListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent event) {

			if (states == null) {
				return;
			}

			if (event.getStateChange() != ItemEvent.SELECTED) {
				return;
			}

			final String newlySelectedKey = (String) event.getItem();
			final List<Double> shownValues = states.get(newlySelectedKey);
			final Chart2D chart = PlotCurve.plotChart("t", newlySelectedKey, shownValues);
			chartSP.getViewport().removeAll();
			chartSP.getViewport().add(chart);
		}
	}

	private class SimulationRunAction extends AbstractAction {

		private final ExecutorService simulationThreadPool;

		public SimulationRunAction() {

			this.simulationThreadPool = Executors.newSingleThreadExecutor();
			putValue(NAME, "Run Simulation");
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			chartSP.getViewport().removeAll();
			states = null;

			final Properties simulationProperties = getSimulationProperties();
			final int simulationSteps = getSimulationSteps();
			final BoundedRangeModel progessModel = new DefaultBoundedRangeModel(0, 0, 0, simulationSteps);
			simulationProgressPB.setModel(progessModel);
			states = null;
			try {
				final RandomSimulation randomSimulation = new RandomSimulation(simulationSteps, simulationProperties, progessModel, null);
				final Callable<Map<String, List<Double>>> simulationWrapper = new Callable() {
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
			} catch (final IOException ex) {
				throw new RuntimeException(ex);
			} catch (final PropertiesException ex) {
				throw new RuntimeException(ex);
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
        chartSP = new javax.swing.JScrollPane();
        bottomP = new javax.swing.JPanel();
        simulationProgressPB = new javax.swing.JProgressBar();
        controllsP = new javax.swing.JPanel();
        nSimStepsS = new javax.swing.JSpinner();
        runB = new javax.swing.JButton();
        shownStateKeyCB = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Industrial Benchmark - Random Simulation");

        westP.setLayout(new java.awt.BorderLayout());

        simPropsP.setBorder(javax.swing.BorderFactory.createTitledBorder("Simulation Properties"));
        simPropsP.setLayout(new java.awt.BorderLayout());
        simPropsP.add(simPropsSP, java.awt.BorderLayout.CENTER);

        westP.add(simPropsP, java.awt.BorderLayout.CENTER);

        mainSP.setLeftComponent(westP);
        mainSP.setRightComponent(chartSP);

        getContentPane().add(mainSP, java.awt.BorderLayout.CENTER);

        bottomP.setLayout(new java.awt.BorderLayout());

        simulationProgressPB.setToolTipText("Simulation Progress");
        bottomP.add(simulationProgressPB, java.awt.BorderLayout.CENTER);

        nSimStepsS.setModel(new javax.swing.SpinnerNumberModel(1500, 1, null, 500));
        nSimStepsS.setToolTipText("Simulation Steps");
        nSimStepsS.setPreferredSize(new java.awt.Dimension(100, 24));
        controllsP.add(nSimStepsS);

        runB.setText("Run");
        controllsP.add(runB);

        shownStateKeyCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        shownStateKeyCB.setToolTipText("Shown Output Value");
        controllsP.add(shownStateKeyCB);

        bottomP.add(controllsP, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(bottomP, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(TrialGuiMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(TrialGuiMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(TrialGuiMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
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
    private javax.swing.JButton runB;
    private javax.swing.JComboBox<String> shownStateKeyCB;
    private javax.swing.JPanel simPropsP;
    private javax.swing.JScrollPane simPropsSP;
    private javax.swing.JProgressBar simulationProgressPB;
    private javax.swing.JPanel westP;
    // End of variables declaration//GEN-END:variables
}
