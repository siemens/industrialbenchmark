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
normalized, linearly biased Goldstone Potential

Normalization:
* global minimum has functional value of -1
* transition of domain with three extrema to one domain with one extrema
  is at angle phi_b = pi / 4 [45 deg]

Created on 04.02.2016

@author: Alexander Hentschel
'''

import numpy as np
from numpy import sqrt, sin, cos, arcsin, arccos, pi
from scipy.special import cbrt

class nlgp:

    def __init__(self):
        u0 = cbrt(1 + sqrt(2)) / sqrt(3)
        r0 = u0 + 1 / (3*u0)
        
        lmbd = 2 * r0**2 - r0**4 + 8 * sqrt(2/27.) * r0
        
        self.__norm_alpha = 2 / lmbd
        self.__norm_beta = 1 / lmbd
        self.__norm_kappa = -8 * sqrt(2/27.) / lmbd
        self.__phi_b = pi / 4.
        self.__qh_b = - sqrt(1/27.)

    def domain_border_angle(self):
        '''
        Returns angle where transition occurs from domain with three extrema to one domain with one extrema
        '''
        return self.__phi_b

    def euclidean_nlgp(self, x, y):
        '''
        Function value of normalized, linearly biased Goldstone Potential
        in euclidean coordinates: 
          * x,y in R
        '''   
        rsq = np.square(x) + np.square(y)
        return -self.__norm_alpha * rsq   + self.__norm_beta * np.square(rsq) + self.__norm_kappa * y
    
    def polar_nlgp(self, r, phi):
        '''
        Function value of normalized, linearly biased Goldstone Potential
        in polar coordinates: 
          * r in R
          * angle in Radians
        '''   
        rsq = np.square(r) 
        return -self.__norm_alpha * rsq   + self.__norm_beta * np.square(rsq) + self.__norm_kappa * sin(phi) * r
    
    def global_minimum_radius(self,phi):
        '''
        returns the radius r0 along phi-axis where NLG has minimal function value, i.e. 
            r0 = argmin_{r} polar_nlgp(r,phi)
        angle phi in Radians
        '''
        f = np.vectorize(self.__global_minimum_radius)
        return f(phi)

    def __global_minimum_radius(self,phi):
        '''
        only applicable for scalar phi
        angle in Radians
        '''
        # use 2-pi-symmetry to move phi in domain [0,360°]
        phi = phi % (2*pi)
        #
        # if phi >= 180°, use symmetry of LGP:
        #  * compute r_min in domain: phi - 180° in [0,180°]
        #  * multiply resulting radius with -1
        if phi >= pi:
            phi -= pi
            scalar = -1
        else:
            scalar = 1

        #    
        qh = self.__norm_kappa * sin(phi) / (8*self.__norm_beta)
        #
        # For numerical stability, we distinguish the domain with 3 extrema from the 
        # domain with one extremum based on qh. Specifically, the domain-limit 
        # value qh_b is
        #   qh_b = norm_kappa * sin(phi_b) / (8*norm_beta) = - sqrt(1/27)
        # In comparison, distinguishing domains based on phi_b leads to numerical 
        # instabilities when phi -> phi_b. For example in Case A, i.e. phi > phi_b, 
        # tiny numerical errors could result in 
        #    qh^2 < sqrt(1/27) when phi -> phi_b
        # even though this would be analytically never possible. Nevertheless, 
        # with limited precision this instability occurs resulting in
        #    sqrt(qh*qh - 1 / 27) = NaN
        #
        # determine, if qh is in domain with one extreme or three
        #  * if qh <= qh_b = - sqrt(1/27) 
        #          => domain with only one global extremum
        #    else  => domain with three global extrema

        if (qh <= self.__qh_b):
            u =  cbrt(-qh + sqrt(qh*qh - 1 / 27))
            r0 = u + 1 / (3*u)
        else:
            r0 = sqrt(4/3) * cos(1/3 * arccos(-qh*sqrt(27)) )
        return scalar*r0
    
    
    def global_minimum(self,phi):
        '''
        returns the minimal functional value along phi-axis, i.e. 
            min_{r} polar_nlgp(r,phi)
        angle phi in Radians
        '''        
        r0 = self.global_minimum_radius(phi)
        return self.polar_nlgp(r0, phi)

































# EOF
