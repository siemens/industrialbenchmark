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
package com.siemens.industrialbenchmark.datavector.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import com.siemens.industrialbenchmark.properties.PropertiesException;
import com.siemens.industrialbenchmark.properties.PropertiesUtil;

public class EffectiveAction {

	ActionAbsolute action;

	final double alpha;
	final double effectiveA;
	final double effectiveB;
	final double beta;
	final double setpoint;

	public EffectiveAction(ActionAbsolute action, double setpoint) {
		this.action = action;
		this.setpoint = setpoint;

		effectiveA = calcEffectiveA(action.getVelocity(), setpoint);
		effectiveB = calcEffectiveB(action.getGain(), setpoint);
		alpha = calcAlphaScaled(action.getVelocity(), action.getGain(), setpoint);
		beta = calcBetaScaled(action.getGain(), setpoint);
	}

	public double getEffectiveA() {
		return this.effectiveA;
	}
	public double getEffectiveB() {
		return this.effectiveB;
	}

	private double calcAlphaScaled(double a, double b, double setpoint) {
		final double minAlphaUnscaled = calcAlphaUnscaled(calcEffectiveA(100, setpoint), calcEffectiveB(0, setpoint));
		final double maxAlphaUnscaled = calcAlphaUnscaled(calcEffectiveA(0, setpoint), calcEffectiveB(100, setpoint));
		final double alphaUnscaled = calcAlphaUnscaled(calcEffectiveA(a, setpoint), calcEffectiveB(b, setpoint));

		return (alphaUnscaled - minAlphaUnscaled) / (maxAlphaUnscaled - minAlphaUnscaled);
	}

	private double calcBetaScaled(double b, double setpoint) {
		final double minBetaUnscaled = calcBetaUnscaled(calcEffectiveB(100, setpoint));
		final double maxBetaUnscaled = calcBetaUnscaled(calcEffectiveB(0, setpoint));
		final double betaUnscaled = calcBetaUnscaled(calcEffectiveB(b, setpoint));

		return (betaUnscaled - minBetaUnscaled) / (maxBetaUnscaled - minBetaUnscaled);
	}

	private double calcEffectiveA(double a, double setpoint) {
		return a + 101.f - setpoint;
	}

	private double calcEffectiveB(double b, double setpoint) {
		return b + 1.f + setpoint;
	}

	private double calcAlphaUnscaled(double effectiveA, double effectiveB) {
		return (effectiveB + 1.0f) / effectiveA;
	}

	private double calcBetaUnscaled(double effectiveB) {
		return 1.0f / effectiveB;
	}

	public double getVelocityAlpha() {
		return this.alpha;
	}

	public double getGainBeta() {
		return this.beta;
	}

	public static void main(String[] args) {

		final float steps = 50;
		final int min=0;
		final int max=100;
		final float step = (max-min) / steps;

		try {
			File f = new File("output.txt");
			PrintWriter writer = null;
			writer = new PrintWriter(f);

			// evaluate EffectiveAction class
			final int setpoint = 100;
			Properties props;
			props = PropertiesUtil.setpointProperties(new File("src/main/resources/sim.properties"));
			writer.println("x,y,alpha,beta");

			for (float x=min; x<=max; x+=step) {
				for (float y=min; y<=max; y+=step) {
					EffectiveAction action = new EffectiveAction(new ActionAbsolute(x, y, 0.0f, props), setpoint);
					writer.println(x + "," + y + "," + action.getVelocityAlpha() + "," + action.getGainBeta());
				}
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (PropertiesException e) {
			e.printStackTrace();
		}
	}
}
