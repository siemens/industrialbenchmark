# coding=utf-8
from __future__ import division
'''
The MIT License (MIT)

Copyright 2017 Siemens AG

Author: Alexander Hentschel

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
'''
'''
Created on 06.08.2014

* Requires Pandas 0.16
* For Execution on Command line

@author: Alexander Hentschel
'''

import numpy as np
from numpy import pi, sin, sign
from nlgp import nlgp


class reward_function:
    
    def __init__(self, phi, max_required_step):
        """
        generates the reward function for fixed phi
        works ONLY WITH SCALAR inputs
        Input:
         * phi: angle in Radians
         * max_required_step: the max. required step by the optimal policy; must be positive
        """
        self.phi = phi
        self.max_required_step = max_required_step
        if max_required_step <= 0:
            raise ValueError("Value for argument max_required_step must be positive")
        #
        
        self.__reward_function = self.__reward_function_factory(phi, max_required_step)
        self.__vectorized_reward_function = np.vectorize(self.__reward_function)
        self.optimum_radius = self.__compute_optimal_radius(phi, max_required_step)
        self.optimum_value = self.__reward_function(self.optimum_radius)
        
    def reward(self, r):
        return self.__reward_function(r)

# ################################################################################# #
#                           Radius Transformation                                   #
# --------------------------------------------------------------------------------- #
# ################################################################################# #


    def __transfer_function_factory(self, r0, chi, xi):
        exponent =  chi / xi
        scaling = xi / chi**exponent
        return lambda x: r0 + scaling * x**exponent
    
    def __rad_transformation_factory(self, x0, r0, x1, r1):

        if not ((x0<=0 and r0<=0 and x1<=0 and r1<=0) or (x0>=0 and r0>=0 and x1>=0 and r1>=0)):
            raise ValueError("x0, r0, x1, r1 must bei either all positive or all negative")
        x0 = abs(x0)
        r0 = abs(r0)
        x1 = abs(x1)
        r1 = abs(r1)
        if x0<=0 or r0<=0:
            raise ValueError("x0, r0 must be positive")
        if x1<x0 or r1<r0 :
            raise ValueError("required: (x0, r0) < (x1, r1)")
        rscaler = r0 / x0
        def tsf(x):
            s = sign(x)
            x = abs(x)
            seg2_tsf = self.__transfer_function_factory(r0, x1 - x0, r1 - r0)
            if x<=x0: 
                return s*rscaler*x
            if x<x1:
                return s*seg2_tsf(x - x0)
            return s*(x - x1 + r1)
        return tsf
    
# ################################################################################# #
#                         Radius transforming NLGP                                  #
# --------------------------------------------------------------------------------- #
# ################################################################################# #
    
    def __compute_optimal_radius(self, phi, max_required_step):
        """
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
        phi = np.mod(phi, 2*pi)
        #
        opt = max(abs(sin(phi)), max_required_step)
        if phi>=pi:
            opt *= -1
        return opt
    
    def __reward_function_factory(self, phi, max_required_step):
        """
        generates the reward function for fixed phi
        works ONLY WITH SCALAR inputs
        Input:
         * phi: angle in Radians
         * max_required_step: the max. required step by the optimal policy; must be positive
        """
        L = nlgp()
        # use 2-pi-symmetry to move phi in domain [0,2pi]
        phi = np.mod(phi, 2*pi)
        # the desired radius at which we want the global optimim to be: 
        opt_rad = self.__compute_optimal_radius(phi, max_required_step)
        #
        # the radius of minimum in NLGP:
        r0 = L.global_minimum_radius(phi)
        #
        # radius transformation such that minimum is moved to desired value
        tr = self.__rad_transformation_factory(opt_rad, r0, sign(opt_rad)*2, sign(r0)*2)
        tr = np.vectorize(tr)
        return lambda r: L.polar_nlgp(tr(r), phi)































# EOF
