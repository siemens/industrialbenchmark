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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

/**
 * This is a simple curve plotter.
 * @author Michel Tokic
 */
public class PlotCurve {

	private PlotCurve() {}

	public static Chart2D plotChart(final String xlabel, final String ylabel, final List<Double> data) {

		// Create a chart
		final Chart2D chart = new Chart2D();

		// Create an ITrace
		final ITrace2D trace = new Trace2DSimple();

		// Add the trace to the chart. This has to be done before adding points
		chart.addTrace(trace);
		// Add all points, as it is static
		for (int di = 0; di < data.size(); di++) {
			trace.addPoint(di, data.get(di));
		}
		chart.getAxisX().setAxisTitle(new AxisTitle(xlabel));
		chart.getAxisY().setAxisTitle(new AxisTitle(ylabel));

		return chart;
	}

	public static void plot(final String title, final String xlabel, final String ylabel, final List<Double> data) {

		final Chart2D chart = plotChart(xlabel, ylabel, data);

		// Display the chart ...
		// Create a frame.
		final JFrame frame = new JFrame(title);
		// add the chart to the frame
		frame.getContentPane().add(chart);
		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, 400);

		// Enable the termination button (cross on the upper right edge)
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent evt) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
	}

	public static void plot(final String title, final String xlabel, final String ylabel, final double[] data) {

		final List<Double> dataList = new ArrayList<>(data.length);
		for (int di = 0; di < data.length; di++) {
			dataList.add(data[di]);
		}
		plot(title, xlabel, ylabel, dataList);
	}
}
