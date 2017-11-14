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
	private static final double qh_b = -Math.sqrt(1./27.);
	
	/**
	 * Function value of normalized, linearly biased Goldstone Potential
	 * in polar coordinates.
	 * @param r in R
	 * @param phi angle in Radians
	 * @return
	 */
	public double polar_nlgp (final double r, final double phi) {
        final double rsq = r*r; 
		return -norm_alpha * rsq + norm_beta * rsq*rsq + norm_kappa * Math.sin(phi) * r;
	}
	
	/**
	 * returns the radius r0 along phi-axis where NLG has minimal function value, i.e. 
       r0 = argmin_{r} polar_nlgp(r,phi)        
	 * @param phi angle in Radians
	 * @return returns the radius r0 along phi-axis where NLG has minimal function value
	 */
	public double global_minimum_radius(double phi) {
	   final double qh = norm_kappa * Math.abs(Math.sin(phi)) / (8. * norm_beta);
        
       final double r0;

        double signum_phi = Math.signum(Math.sin(phi));
        if(signum_phi==0.0){
        	signum_phi = 1.0;
        }
        
        if (qh < qh_b) {
            final double u2 =  Math.cbrt(-signum_phi*qh + Math.sqrt(qh*qh - 1. / 27.));
            r0 = u2 + 1. / (3.*u2);
        } else {
        	r0 = signum_phi * Math.sqrt(4./3.) * Math.cos(1./3. * Math.acos(-qh*Math.sqrt(27.)) );
        }   
        return r0;      
	}	
}
