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

/**
 * Reward function for normalized, linearly biased Goldstone Potential
 *
 * @author Alexander Hentschel, Michel Tokic
 */
public class PenaltyFunction {

	private final DoubleFunction reward_function;
	private final double optimum_radius;
	private final double optimum_value;
	
	/**
	 * Generates the reward function for fixed phi
	 * @param phi angle in radians
	 * @param max_required_step the max. required step by the optimal policy; must be positive
	 */
	public PenaltyFunction(double phi, double max_required_step) {
		reward_function = reward_function_factory(phi, max_required_step);
		optimum_radius = compute_optimal_radius(phi, max_required_step);
		optimum_value = reward_function.apply(optimum_radius);
	}
	
	public double reward (double r) {
		return reward_function.apply(r);
	}
	
	/**
	 * Computes radius for the optimum (global minimum).
	 * @param phi
	 * @param max_required_step
	 * @return
	 */
	private double compute_optimal_radius (double phi, double max_required_step) {
		double signum_phi = Math.signum(Math.sin(phi));
        if(signum_phi==0.0){
        	signum_phi = 1.0;
        }
    	return signum_phi*Math.max(Math.abs(Math.sin(phi)), max_required_step);
	}

	/**
     * Generates the reward function for fixed phi. Works ONLY WITH SCALAR inputs. 
	 * @param p angle in Radians
	 * @param max_required_step the max. required step by the optimal policy; must be positive
	 * @return
	 */
	public DoubleFunction reward_function_factory(double phi, double max_required_step) {
		final NLGP l = new NLGP();

		double opt_rad = compute_optimal_radius(phi, max_required_step);
		double r0 = l.global_minimum_radius(phi);

		return new DoubleFunction() {
			@Override
			public double apply(double x) {
				double result = Double.NaN;
				if (Math.abs(x)<=Math.abs(opt_rad)) {
					result = x*Math.abs(r0)/Math.abs(opt_rad);
				}else{
					final double exponent = (2.0-Math.abs(opt_rad)) / (2.0-Math.abs(r0));
					final double scaling = (2.0-Math.abs(r0)) / Math.pow((2.0-Math.abs(opt_rad)), exponent);
					result = Math.signum(x)*(Math.abs(r0) + scaling * Math.pow(Math.abs(x)-Math.abs(opt_rad), exponent));
				}
				return l.polar_nlgp(result, phi);
			}
		};
	}

	public double getOptimumRadius() {
		return optimum_radius;		
	}
	public double getOptimumValue() {
		return optimum_value;
	}
}
