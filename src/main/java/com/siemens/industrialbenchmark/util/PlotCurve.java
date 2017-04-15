/**
Copyright 2016 Siemens AG.

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

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * This class is a simple curve plotter.
 * @author Michel Tokic
 *
 */
public class PlotCurve {

	public static void plot (String title, String xlabel, String ylabel, double[] data) {

		//////////////////////////////////
		// Create a chart:
		//////////////////////////////////
		Chart2D chart = new Chart2D();

		// Create an ITrace:
		ITrace2D trace = new Trace2DSimple();

		// Add the trace to the chart. This has to be done before adding points
		chart.addTrace(trace);
		// Add all points, as it is static:
		for (int i = 0; i < data.length; i++) {
			trace.addPoint(i, data[i]);
		}
		chart.getAxisX().setAxisTitle(new AxisTitle(xlabel));
		chart.getAxisY().setAxisTitle(new AxisTitle(ylabel));

		// Make it visible:
		// Create a frame.
		JFrame frame = new JFrame(title);
		// add the chart to the frame:
		frame.getContentPane().add(chart);
		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, 400);

		// Enable the termination button [cross on the upper right edge]:
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
	}
}

