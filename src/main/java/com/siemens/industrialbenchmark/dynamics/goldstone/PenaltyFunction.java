/*
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
package com.siemens.industrialbenchmark.dynamics.goldstone;

/**
 * Reward function for normalized, linearly biased Goldstone Potential
 *
 * @author Alexander Hentschel, Michel Tokic
 */
public class PenaltyFunction {

	private final transient DoubleFunction rewardFunction;
	private final double optimumRadius;
	private final double optimumValue;

	/**
	 * Generates the reward function for fixed phi
	 * @param phi angle in radians
	 * @param maxRequiredStep the max. required step by the optimal policy; must be positive
	 */
	public PenaltyFunction(final double phi, final double maxRequiredStep) {

		this.rewardFunction = rewardFunctionFactory(phi, maxRequiredStep);
		this.optimumRadius = computeOptimalRadius(phi, maxRequiredStep);
		this.optimumValue = rewardFunction.apply(optimumRadius);
	}

	public double reward(final double r) {
		return rewardFunction.apply(r);
	}

	/**
	 * Computes radius for the optimum (global minimum).
	 * @param phi
	 * @param maxRequiredStep
	 * @return
	 */
	private static double computeOptimalRadius(final double phi, final double maxRequiredStep) {
		double signum_phi = Math.signum(Math.sin(phi));
		if (signum_phi == 0.0) {
			signum_phi = 1.0;
		}
		return signum_phi * Math.max(Math.abs(Math.sin(phi)), maxRequiredStep);
	}

	/**
	 * Generates the reward function for fixed phi. Works ONLY WITH SCALAR inputs.
	 * @param p angle in Radians
	 * @param maxRequiredStep the max. required step by the optimal policy; must be positive
	 * @return
	 */
	private static DoubleFunction rewardFunctionFactory(final double phi, final double maxRequiredStep) {
		final NLGP l = new NLGP();

		final double optRad = computeOptimalRadius(phi, maxRequiredStep);
		final double r0 = l.globalMinimumRadius(phi);

		return new DoubleFunction() {
			@Override
			public double apply(final double x) {
				double result = Double.NaN;
				if (Math.abs(x) <= Math.abs(optRad)) {
					result = x * Math.abs(r0) / Math.abs(optRad);
				} else {
					final double exponent = (2.0 - Math.abs(optRad)) / (2.0 - Math.abs(r0));
					final double scaling = (2.0 - Math.abs(r0)) / Math.pow((2.0 - Math.abs(optRad)), exponent);
					result = Math.signum(x) * (Math.abs(r0) + scaling * Math.pow(Math.abs(x) - Math.abs(optRad), exponent));
				}
				return l.polarNlgp(result, phi);
			}
		};
	}

	public double getOptimumRadius() {
		return optimumRadius;
	}

	public double getOptimumValue() {
		return optimumValue;
	}
}

