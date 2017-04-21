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
 *
 * Normalization:
 * Global minimum has functional value of -1.
 * Transition of domain with three extrema to one domain with one extrema
 * is at angle <code>phi_b = pi / 4 = 45 deg</code>.
 *
 * @author Alexander Hentschel, Michel Tokic
 */
public class NLGP {

	private static final double U0 = Math.cbrt(1 + Math.sqrt(2)) / Math.sqrt(3);
	private static final double R0 = U0 + 1. / (3.*U0);
	private static final double LMBD = 2. * R0 * R0 - Math.pow(R0, 4) + 8. * Math.sqrt(2. / 27.) * R0;

	private static final double NORM_ALPHA = 2. / LMBD;
	private static final double NORM_BETA = 1. / LMBD;
	private static final double NORM_KAPPA = -8. * Math.sqrt(2./27.) / LMBD;
	private static final double PHI_B = Math.PI/4.;
	private static final double QH_B = -Math.sqrt(1./27.);

	/**
	 * Returns angle where transition occurs from domain with three extrema
	 * to one domain with one extrema.
	 * @return transition angle in radians
	 */
	public double getDomainBorderAngle() {
		return PHI_B;
	}

	/**
	 * Function value of normalized, linearly biased Goldstone Potential
	 * in euclidian coordinates.
	 * @param x in R
	 * @param y in R
	 * @return resulting euclidian-NLGP function value
	 */
	public double euclideanNlgp(final double x, final double y) {
		// 	rsq = np.square(x) + np.square(y)
		//	return -self.__norm_alpha * rsq + self.__norm_beta * np.square(rsq) + self.__norm_kappa * y
		final double rsq = x*x + y*y;
		return -NORM_ALPHA * rsq + NORM_BETA * rsq*rsq + NORM_KAPPA * y;
	}

	/**
	 * Function value of normalized, linearly biased Goldstone Potential
	 * in polar coordinates.
	 * @param r in R
	 * @param phi angle in Radians
	 * @return resulting polar-NLGP function value
	 */
	public double polarNlgp(final double r, final double phi) {
		//rsq = np.square(r)
		//return -self.__norm_alpha * rsq + self.__norm_beta * np.square(rsq) + self.__norm_kappa * sin(phi) * r
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
		// use 2-pi-symmetry to move phi in domain [0,360°]
		double modPhi = phi % (2.*Math.PI);

		//
		// if phi >= 180°, use symmetry of LGP:
		// * compute r_min in domain: phi - 180° in [0,180°]
		// * multiply resulting radius with -1
		final double scalar;
		if (modPhi >= Math.PI) {
			modPhi -= Math.PI;
			scalar = -1;
		} else {
			scalar = 1;
		}

		final double qh = NORM_KAPPA * Math.sin(modPhi) / (8. * NORM_BETA);

		/*
		 * For numerical stability, we distinguish the domain with 3 extrema from the
		 * domain with one extremum based on qh. Specifically, the domain-limit
		 * value qh_b is
		 * qh_b = norm_kappa * sin(phi_b) / (8*norm_beta) = - sqrt(1/27)
		 * In comparison, distinguishing domains based on phi_b leads to numerical
		 * instabilities when phi -> phi_b. For example in Case A, i.e. phi > phi_b,
		 * tiny numerical errors could result in
		 * qh^2 < sqrt(1/27) when phi -> phi_b
		 * even though this would be analytically never possible. Nevertheless,
		 * with limited precision this instability occurs resulting in
		 * sqrt(qh*qh - 1 / 27) = NaN
		 *
		 * determine, if qh is in domain with one extreme or three
		 * * if qh <= qh_b = - sqrt(1/27)
		 * => domain with only one global extremum
		 * else => domain with three global extrema
		 */

		final double r0;
		if (qh <= QH_B) {
			final double u = Math.cbrt(-qh + Math.sqrt(qh * qh - 1. / 27.));
			r0 = u + 1. / (3.*u);
		} else {
			r0 = Math.sqrt(4. / 3.) * Math.cos(1. / 3. * Math.acos(-qh * Math.sqrt(27.)));
		}
		return scalar*r0;
	}

	/**
	 * Vectorized version of {@link #globalMinimumRadius(double)}.
	 * @param phi a set of angles in radians
	 * @return resulting r0s with minimal NLGs
	 */
	public double[] globalMinimumRadius(final double[] phi) {
		final double[] ret = new double[phi.length];
		for (int i = 0; i < phi.length; i++) {
			ret[i] = NLGP.this.globalMinimumRadius(phi[i]);
		}
		return ret;
	}

	/**
	 * Returns the minimal functional value along phi-axis.
	 * i.e. <code>min_{r} polarNlgp(r, phi)</code>.
	 * @param phi angle in Radians
	 * @return the minimal functional value along phi-axis
	 */
	public double globalMinimum(final double phi) {
		final double r0 = NLGP.this.globalMinimumRadius(phi);
		return polarNlgp(r0, phi);
	}
}
