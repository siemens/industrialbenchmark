# coding=utf-8
from __future__ import division
import numpy as np
from numpy import pi, sin, sign
from .nlgp import nlgp
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
        
        self.__reward_function = self.__reward_function_factory(phi, max_required_step)
        self.__vectorized_reward_function = np.vectorize(self.__reward_function)
        self.optimum_radius = self.__compute_optimal_radius(phi, max_required_step)
        self.optimum_value = self.__reward_function(self.optimum_radius)
        
    def reward(self, r):
        return self.__reward_function(r)

    def __rad_transformation_factory(self, opt_rad, min_rad):
        def tsf(x):
            if abs(x) <= abs(opt_rad):
                return x * abs(min_rad) / abs(opt_rad)
            else:
                exponent = (2-abs(opt_rad)) / (2-abs(min_rad))
                scaling = (2-abs(min_rad)) / (2-abs(opt_rad))**exponent
                return np.sign(x) * (abs(min_rad) + scaling * (abs(x) - abs(opt_rad))**exponent)
        return tsf
    
    def __compute_optimal_radius(self, phi, max_required_step):
        phi = np.mod(phi, 2*pi)

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
        min_rad = L.global_minimum_radius(phi)
        #
        # radius transformation such that minimum is moved to desired value
        tr = self.__rad_transformation_factory(opt_rad, min_rad)
        tr = np.vectorize(tr)
        return lambda r: L.polar_nlgp(tr(r), phi)