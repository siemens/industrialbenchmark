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

import com.google.common.base.Preconditions;

/**
 * Reward function for normalized, linearly biased Goldstone Potential
 *
 * @author Alexander Hentschel, Michel Tokic
 */
public class PenaltyFunction {

	private final double phi;
	private final double maxRequiredStep;
	private final transient DoubleFunction rewardFunction;
	private final double optimumRadius;
	private final double optimumValue;

	/**
	 * generates the reward function for fixed phi
	 * works ONLY WITH SCALAR inputs
	 * @param phi angle in radians
	 * @param maxRequiredStep the max. required step by the optimal policy; must be positive
	 */
	public PenaltyFunction(final double phi, final double maxRequiredStep) {

		Preconditions.checkArgument(maxRequiredStep > 0,
				"max_required_step must be > 0, but was %s", maxRequiredStep);
		this.phi = phi;
		this.maxRequiredStep = maxRequiredStep;

		this.rewardFunction = rewardFunctionFactory(phi, maxRequiredStep);
		this.optimumRadius = computeOptimalRadius(phi, maxRequiredStep);
		this.optimumValue = rewardFunction.apply(optimumRadius);
	}

	public double reward(final double r) {
		return rewardFunction.apply(r);
	}

	public double[] reward(final double[] r) {
		final double[] ret = new double[r.length];
		for (int i = 0; i < r.length; i++) {
			ret[i] = reward(r[i]);
		}
		return ret;
	}

	// ################################################################################### #
	// #                           Radius Transformation                                   #
	// # --------------------------------------------------------------------------------- #
	// # ################################################################################# #
	private static DoubleFunction transferFunctionFactory(final double r0, final double chi, final double xi) {
		final double exponent = chi / xi;
		final double scaling = xi / Math.pow(chi, exponent);

		return new DoubleFunction() {
			@Override
			public double apply(final double value) {
				return r0 + scaling * Math.pow(value, exponent);
			}
		};
	}

	private static DoubleFunction radTransformationFactory(final double x0, final double r0, final double x1, final double r1) {

		Preconditions.checkArgument(
				(x0 <= 0 && r0 <= 0 && x1 <= 0 && r1 <= 0)
				|| (x0 >= 0 && r0 >= 0 && x1 >= 0 && r1 >= 0),
				"x0, r0, x1, r1 must be either all positive or all negative, "
				+ "but was x0=%s, r0=%s, x1=%s, r1=%s", x0, r0, x1, r1);

		final double absX0 = Math.abs(x0);
		final double absR0 = Math.abs(r0);
		final double absX1 = Math.abs(x1);
		final double absR1 = Math.abs(r1);

		Preconditions.checkArgument(absX0 > 0 && absR0 > 0,
				"x0 and r0 must be positive, but was x0=%s and r0=%s", absX0, absR0);
		Preconditions.checkArgument(absX1 >= absX0 && absR1 >= absR0,
				"required: (x0, r0) < (x1, r1), but was x0=%s, r0=%s x1=%s, r1=%s", absX0, absR0, absX1, absR1);

		final double rScaler = absR0 / absX0;

		final DoubleFunction seg2Tsf =
				transferFunctionFactory(absR0, absX1 - absX0, absR1 - absR0);

		return new DoubleFunction() {
			@Override
			public double apply(final double x) {
				final double sign = Math.signum(x);
				final double absX = Math.abs(x);

				if (absX <= absX0) {
					return sign * rScaler * absX;
				}
				if (absX < absX1){
					return sign * seg2Tsf.apply(absX - absX0);
				}
				return sign * (absX - absX1 + absR1);
			}
		};
	}

	/**
	 * Radius transforming NLGP.
		Computes radius for the optimum (global minimum). Note that
		for angles phi = 0, pi, 2pi, ... the Normalized Linear-biased
		Goldstone Potential has two global minima.
		Per convention, the desired sign of the return value is:
		   opt radius > 0 for phi in [0,pi)
		   opt radius < 0 for phi in [pi,2pi)
		Explanation:
		 * for very small angles, where the sin(phi) is smaller than max_required_step
		   the opt of the reward landscape should be per definition at
			 max_required_step if phi in [0,pi)
			-max_required_step if phi in [pi,2pi)
		   (max_required_step is assumed to be positive)
		 * for all cases phi != 0,pi,2pi
		   the sign is identical to the sign of the sin function
		 * but for phi = 0,pi,2pi the sig-function is zero and
		   provides no indication about the sign of the opt
	 *
	 * @param phi
	 * @param maxRequiredStep
	 * @return
	 */
	private static double computeOptimalRadius(final double phi, final double maxRequiredStep) {
		final double modPhi = phi % (2 * Math.PI);
		double opt = Math.max(Math.abs(Math.sin(modPhi)), maxRequiredStep);
		if (modPhi >= Math.PI) {
			opt *= -1;
		}
		return opt;
	}

	/**
	 * Generates the reward function for fixed phi. Works ONLY WITH SCALAR inputs.
	 * @param p angle in Radians
	 * @param maxRequiredStep the max. required step by the optimal policy; must be positive
	 * @return
	 */
	private static DoubleFunction rewardFunctionFactory(final double phi, final double maxRequiredStep) {

		final NLGP l = new NLGP();
		final double pTmp = phi % (2 * Math.PI);
		final double p = pTmp < 0 ? pTmp + (2 * Math.PI) : pTmp;

		final double optRad = computeOptimalRadius(p, maxRequiredStep);
		final double r0 = l.globalMinimumRadius(p);

		final DoubleFunction tr = radTransformationFactory(optRad, r0, Math.signum(optRad) * 2, Math.signum(r0) * 2);

		return new DoubleFunction() {
			@Override
			public double apply(final double r) {
				return l.polarNlgp(tr.apply(r), p);
			}
		};
	}

	public double getOptimumRadius() {
		return optimumRadius;
	}

	public double getOptimumValue() {
		return optimumValue;
	}

	public double getPhi() {
		return phi;
	}

	public double getMaxRequiredStep() {
		return maxRequiredStep;
	}
}

