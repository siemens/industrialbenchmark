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
package com.siemens.industrialbenchmark.dynamics.goldstone;

import com.google.common.base.Preconditions;

/**
 * Reward function for normalized, linearly biased Goldstone Potential
 *
 * @author Alexander Hentschel, Michel Tokic
 */
public class PenaltyFunction {

	private final double phi;
	private final double max_required_step;
	private final DoubleFunction reward_function;
	private final double optimum_radius;
	private final double optimum_value;

	/**
	 * generates the reward function for fixed phi
       works ONLY WITH SCALAR inputs
	 * @param phi angle in radians
	 * @param max_required_step the max. required step by the optimal policy; must be positive
	 */
	public PenaltyFunction(double phi, double max_required_step) {

		Preconditions.checkArgument(max_required_step > 0,
				"max_required_step must be > 0, but was %s", max_required_step);
		this.phi = phi;
		this.max_required_step = max_required_step;

		reward_function = reward_function_factory(phi, max_required_step);
		optimum_radius = compute_optimal_radius(phi, max_required_step);
		optimum_value = reward_function.apply(optimum_radius);
	}

	public double reward (double r) {
		return reward_function.apply(r);
	}

	/**
	 * vectorized version of reward(double)
	 * @param r
	 * @return
	 */
	public double[] reward (double r[]) {
		double ret[] = new double [r.length];
		for (int i=0; i<r.length; i++) {
			ret[i] = reward(r[i]);
		}
		return ret;
	}

	// ################################################################################### #
	// #                           Radius Transformation                                   #
	// # --------------------------------------------------------------------------------- #
	// # ################################################################################# #
	private DoubleFunction transfer_function_factory(final double r0, double chi, double xi) {
		final double exponent = chi / xi;
		final double scaling = xi / Math.pow(chi, exponent);

		return new DoubleFunction() {
			@Override
			public double apply(double value) {
				return r0 + scaling * Math.pow(value, exponent);
			}
		};
	}

	private DoubleFunction rad_transformation_factory(double x0, double r0, double x1, double r1) {

		Preconditions.checkArgument(
				(x0<=0 && r0<=0 && x1<=0 && r1<=0) ||
				(x0>=0 && r0>=0 && x1>=0 && r1>=0),
				"x0, r0, x1, r1 must be either all positive or all negative, " +
				"but was x0=%s, r0=%s, x1=%s, r1=%s", x0, r0, x1, r1);

		x0 = Math.abs(x0);
		r0 = Math.abs(r0);
		x1 = Math.abs(x1);
		r1 = Math.abs(r1);

		Preconditions.checkArgument(x0>0 && r0>0,
				"x0 and r0 must be positive, but was x0=%s and r0=%s", x0, r0);
		Preconditions.checkArgument(x1>=x0 && r1>=r0,
				"required: (x0, r0) < (x1, r1), but was x0=%s, r0=%s x1=%s, r1=%s", x0, r0, x1, r1);

		final double rscaler = r0 / x0;

		final double final_r0 = r0;
		final double final_x0 = x0;
		final double final_x1 = x1;
		final double final_r1 = r1;

		final DoubleFunction seg2_tsf =
				transfer_function_factory(final_r0, final_x1-final_x0, final_r1-final_r0);

		return new DoubleFunction() {
			@Override
			public double apply(double x) {
				final double s = Math.signum(x);
				x = Math.abs(x);

				if (x<=final_x0) {
					return s*rscaler*x;
				}
				if (x<final_x1){
					return s*seg2_tsf.apply(x-final_x0);
				}
				return s*(x-final_x1+final_r1);
			}
		};
	}

	// # ################################################################################# #
	// #                         Radius transforming NLGP                                  #
	// # --------------------------------------------------------------------------------- #
	// # ################################################################################# #
	/**
	 * """
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
        """
	 *
	 * @param phi
	 * @param max_required_step
	 * @return
	 */
	private double compute_optimal_radius (double phi, double max_required_step) {
    	phi = phi % (2*Math.PI);
    	double opt = Math.max(Math.abs(Math.sin(phi)), max_required_step);
    	if (phi>=Math.PI) {
    		opt *= -1;
    	}
    	return opt;
	}

	/**
       Generates the reward function for fixed phi. Works ONLY WITH SCALAR inputs.
	 * @param p angle in Radians
	 * @param max_required_step the max. required step by the optimal policy; must be positive
	 * @return
	 */
	private DoubleFunction reward_function_factory(double phi, double max_required_step) {

		final NLGP l = new NLGP();
		final double pTmp = phi % (2*Math.PI);
		final double p = pTmp < 0 ? pTmp+(2*Math.PI) :  pTmp;

		double opt_rad = compute_optimal_radius(p, max_required_step);
		double r0 = l.global_minimum_radius(p);

		final DoubleFunction tr = rad_transformation_factory(opt_rad, r0, Math.signum(opt_rad)*2, Math.signum(r0)*2);

		return new DoubleFunction() {
			@Override
			public double apply(double r) {
				return l.polar_nlgp(tr.apply(r), p);
			}
		};
	}

	public double getOptimumRadius() {
		return optimum_radius;
	}
	public double getOptimumValue() {
		return optimum_value;
	}
	public double getPhi() {
		return this.phi;
	}
	public double getMaxRequiredStep() {
		return this.max_required_step;
	}
}
