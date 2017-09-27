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
 * Normalized, linearly biased Goldstone Potential.
 * @author Alexander Hentschel, Michel Tokic
 */
public class NLGP {

	private static final double U0 = Math.cbrt(1 + Math.sqrt(2)) / Math.sqrt(3);
	private static final double R0 = U0 + 1. / (3.*U0);
	private static final double LMBD = 2. * R0 * R0 - Math.pow(R0, 4) + 8. * Math.sqrt(2. / 27.) * R0;
	private static final double NORM_ALPHA = 2. / LMBD;
	private static final double NORM_BETA = 1. / LMBD;
	private static final double NORM_KAPPA = -8. * Math.sqrt(2./27.) / LMBD;
	private static final double QH_B = -Math.sqrt(1./27.);

	/**
	 * Function value of normalized, linearly biased Goldstone Potential
	 * in polar coordinates.
	 * @param r in R
	 * @param phi angle in Radians
	 * @return resulting polar-NLGP function value
	 */
	public double polarNlgp(final double r, final double phi) {
		final double rsq = r*r;
		return -NORM_ALPHA * rsq + NORM_BETA * rsq*rsq + NORM_KAPPA * Math.sin(phi) * r;
	}

	/**
	 * Returns the radius r0 along the phi-axis where NLG has minimal function value.
	 * i.e. <code>r0 = argmin_{r} polarNlgp(r, phi)</code>.
	 * @param phi angle in radians
	 * @return r0 with minimal NLG
	 */
	public double globalMinimumRadius(final double phi) {
		final double qh = NORM_KAPPA * Math.abs(Math.sin(phi)) / (8. * NORM_BETA);

		final double r0;

		double signum_phi = Math.signum(Math.sin(phi));
		if (signum_phi == 0.0) {
			signum_phi = 1.0;
		}

		if (qh < QH_B) {
			final double u2 =  Math.cbrt(-signum_phi*qh + Math.sqrt(qh*qh - 1. / 27.));
			r0 = u2 + 1. / (3.*u2);
        } else {
			r0 = signum_phi * Math.sqrt(4./3.) * Math.cos(1./3. * Math.acos(-qh*Math.sqrt(27.)) );
        }
        return r0;
	}
}
