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
 * Normalized, linearly biased Goldstone Potential

 * Normalization:
 * global minimum has functional value of -1
 * transition of domain with three extrema to one domain with one extrema
 * is at angle phi_b = pi / 4 [45 deg]
 *
 * @author Alexander Hentschel, Michel Tokic
 *
 */
public class NLGP {

	private static final double u0 = Math.cbrt(1 + Math.sqrt(2)) / Math.sqrt(3);
	private static final double r0 = u0 + 1. / (3.*u0);
	private static final double lmbd = 2. * r0*r0 - Math.pow(r0,4) + 8. * Math.sqrt(2./27.) * r0;

	private static final double norm_alpha = 2. / lmbd;
	private static final double norm_beta = 1. / lmbd;
	private static final double norm_kappa = -8. * Math.sqrt(2./27.) / lmbd;
	private static final double phi_b = Math.PI/4.;
	private static final double qh_b = -Math.sqrt(1./27.);

	/**
	 * Returns angle where transition occurs from domain with three extrema to one domain with one extrema
	 * @return angle where transition occurs from domain with three extrema to one domain with one extrema
	 */
	public double domain_border_angle() {
		return phi_b;
	}

	/**
	 * Function value of normalized, linearly biased Goldstone Potential
	 * in euclidean coordinates.
	 * @param x in R
	 * @param y in R
	 * @return
	 */
	public double euclidean_nlgp(final double x, final double y) {
		// 	rsq = np.square(x) + np.square(y)
		//	return -self.__norm_alpha * rsq + self.__norm_beta * np.square(rsq) + self.__norm_kappa * y
		final double rsq = x*x + y*y;
		return -norm_alpha * rsq + norm_beta * rsq*rsq + norm_kappa * y;
	}

	/**
	 * Function value of normalized, linearly biased Goldstone Potential
	 * in polar coordinates.
	 * @param r in R
	 * @param phi angle in Radians
	 * @return
	 */
	public double polar_nlgp(final double r, final double phi) {
		//rsq = np.square(r)
		//return -self.__norm_alpha * rsq + self.__norm_beta * np.square(rsq) + self.__norm_kappa * sin(phi) * r
		final double rsq = r*r;
		return -norm_alpha * rsq + norm_beta * rsq*rsq + norm_kappa * Math.sin(phi) * r;
	}

	/**
	 * returns the radius r0 along phi-axis where NLG has minimal function value, i.e.
	 * r0 = argmin_{r} polar_nlgp(r,phi)
	 * @param phi angle in Radians
	 * @return returns the radius r0 along phi-axis where NLG has minimal function value
	 */
	public double global_minimum_radius(double phi) {
		// use 2-pi-symmetry to move phi in domain [0,360°]
		phi = phi % (2.*Math.PI);

		//
		// if phi >= 180°, use symmetry of LGP:
		// * compute r_min in domain: phi - 180° in [0,180°]
		// * multiply resulting radius with -1
		double scalar = 1;
		if (phi >= Math.PI) {
			phi -= Math.PI;
			scalar = -1;
		}

		final double qh = norm_kappa * Math.sin(phi) / (8. * norm_beta);

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
		if (qh <= qh_b) {
			final double u = Math.cbrt(-qh + Math.sqrt(qh*qh - 1. / 27.));
			r0 = u + 1. / (3.*u);
		} else {
			r0 = Math.sqrt(4./3.) * Math.cos(1./3. * Math.acos(-qh*Math.sqrt(27.)) );
		}
		return scalar*r0;
	}

	/**
	 * vectorized version of global_minimum_radius(double)
	 * @param phi
	 * @return
	 */
	public double[] global_minimum_radius(double[] phi) {
		double ret[] = new double [phi.length];
		for (int i=0; i<phi.length; i++) {
			ret[i] = global_minimum_radius(phi[i]);
		}
		return ret;
	}

	/**
	 * returns the minimal functional value along phi-axis, i.e.
	 * min_{r} polar_nlgp(r,phi)
	 * @param phi angle in Radians
	 * @return the minimal functional value along phi-axis
	 */
	public double global_minimum(double phi) {
		double r0 = global_minimum_radius(phi);
		return polar_nlgp(r0, phi);
	}

}

