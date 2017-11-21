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
import numpy as np
from numpy import pi, sign
import reward_function
from enum import Enum
from dynamics import dynamics

# TODO diese Klasse in eine extra
'''
class dynamics:


    class Domain(Enum):
        negative = -1
        initial = 0
        positive = +1

    class System_Response(Enum):
        advantageous = +1
        neutral = 0
        disadvantageous = -1

    
    def __init__(self, number_steps, max_required_step, safe_zone):
        self._safe_zone = self._check_safe_zone(safe_zone)
        self._strongest_penality_abs_idx = self.compute_strongest_penalty_absIdx(number_steps)
        self._penalty_functions_array = self._define_reward_functions(number_steps, max_required_step)

        # internal state
        self.reset()

    def _check_safe_zone(self, safe_zone):
        if (safe_zone < 0):
            raise ValueError('safe_zone must be non-negative')
        return safe_zone

    # TODO wird nicht aufgerufen
    '''
'''
    def _check_number_steps(self, number_steps):
        if (number_steps < 1):
            raise ValueError('number_steps must be positive integer')
        if (number_steps // 4):
            raise ValueError('number_steps must be integer multiple of 4')
        return number_steps
    '''
'''
    # TODO evtl. am Anfang der Klasse ??
    def reset(self):
        self._domain = self.Domain.initial
        self._Phi_idx = 0
        self._system_response = self.System_Response.advantageous
        self._current_penalty_function = self.get_penalty_function()
              
    def reward(self, pos):
        return self._current_penalty_function.reward(pos)


    def optimal_position(self):
        return self._current_penalty_function.optimum_radius

    def optimal_reward(self):
        return self._current_penalty_function.optimum_value


    # TODO new_control_value unbennen?
    def state_transition(self, new_control_value):

        old_domain = self._domain

        # (0) compute new domain
        self._domain = self._compute_domain(new_control_value)

        # (1) if domain change: system_response <- advantageous
        if self._domain != old_domain:
            self._system_response = self.System_Response.advantageous

        # (2) compute & apply turn direction
        self._Phi_idx += self._compute_angular_step(new_control_value)

        # (3) Update system response if necessary
        self._system_response = self._updated_system_response(self._Phi_idx, new_control_value)

        # TODO Reihenfolge ändern
        # (4) apply symmetry
        self._Phi_idx = self._apply_symmetry(self._Phi_idx)
        #TODO old_domain = self._domain ? war im anderen Code

        # (5) if self._Phi_idx == 0: reset internal state
        if (self._Phi_idx == 0) and (abs(new_control_value) <= self._safe_zone):
            self.reset()

        #print("  self._Phi_idx = " + str(self._Phi_idx))
        self._current_penalty_function = self.get_penalty_function()
        
    def _compute_domain(self, new_position):
        """
        compute the new domain of control action
        Note: 
         * if control action is in safe zone, domain remains unchanged
         * as 'penalty landscape' turn direction is independent of exact position
           in safe zone, reset to Domain.initial can be applied later 
        """
        if abs(new_position) <= self._safe_zone: 
            return self._domain
        else:
            return self.Domain(sign(new_position))
    
    def _compute_angular_step(self, new_position):
        # cool down: when position close to zero
        if abs(new_position) <= self._safe_zone: # cool down
            return -sign(self._Phi_idx)

        if self._Phi_idx == -self._domain.value * self._strongest_penality_abs_idx:
            return 0
        return self._system_response.value * sign(new_position)
    

    def _updated_system_response(self, new_Phi_idx, new_position):
        """
        only changes system response to if turn angle hits 90deg 
        in domain of current position, i.e.:
        * new_position > self._safe_zone and new_Phi_idx = 90deg
        * new_position < -self._safe_zone and new_Phi_idx = -90deg
        """
        if abs(new_Phi_idx) >= self._strongest_penality_abs_idx:
            return self.System_Response.disadvantageous
        else:
            return self._system_response
        
    def _apply_symmetry(self, Phi_idx):
        """
        By employing reflection symmetry with respect to x-axis:
         o Phi -> pi -Phi
         o turn direction -> turn direction
         o x -> x
        moves 'penalty landscape' rotation angle in domain [-90deg ... +90deg]
        corresponding to Phi_index in 
             [ -strongest_penality_abs_idx*angular_speed, ..., -angular_speed, 
              0, 
              angular_speed, ..., strongest_penality_abs_idx*angular_speed ]         
        """
        #
        # Do nothing if 'penalty landscape' rotation angle is in 
        #    [-90deg ... +90deg] 
        # corresponding to angle indices
        #    [-self._strongest_penality_abs_idx, ...-1,0,1, ..., self._strongest_penality_abs_idx-]
        if abs(Phi_idx) < self._strongest_penality_abs_idx:
            return Phi_idx
        # 
        # Otherwise:
        # Use 2pi symmetry to move angle index p in domain 
        #   [0 ... 360deg)
        # corresponding to angle indices
        #   [0, ..., 4*self._strongest_penality_abs_idx-1]
        # But we are only executing the following code, if the angle is in
        #   (90deg, ..., 270deg)
        # corresponding to angle indices
        #   [self._strongest_penality_abs_idx+1, ..., 3*self._strongest_penality_abs_idx-1]
        # Therefore, the reflection-symmetry operation 
        #    p <- 2*self._strongest_penality_abs_idx - p
        # will transform p back into the desired angle indices domain
        #    [-self._strongest_penality_abs_idx, ...-1,0,1, ..., self._strongest_penality_abs_idx-]
        # TODO im anderen Code: Phi_idx + (4*self._strongest_penality_abs_idx) % (4*self._strongest_penality_abs_idx)
        Phi_idx = Phi_idx % (4*self._strongest_penality_abs_idx)
        Phi_idx = 2*self._strongest_penality_abs_idx - Phi_idx
        return Phi_idx

    def get_penalty_function(self, Phi_idx=None):
        if Phi_idx is None:
            p = self._Phi_idx
        else:
            p = self._apply_symmetry(Phi_idx)
#             if (abs(p) > self._strongest_penality_abs_idx):
#                 raise IndexError("Turn index (" + str(p) + ") exceeds bounds (" + str(self._strongest_penality_abs_idx) + ")")
        # TODO idx außerhalb con ArrayIndex
        idx = int(self._strongest_penality_abs_idx + p)
        # TODO: es fehlt:
        if idx < 0 :
            idx = idx + len(self._penalty_functions_array)
        return self._penalty_functions_array[idx]
 
    def _define_reward_functions(self, number_steps, max_required_step):
        """
        Parameters
        ----------------        
        * Number_steps:
        the number of steps required for one full cycle of the optimal policy.
        For easy numerics, it is required that number_steps is positive and an integer multiple of 4.

        By employing reflection symmetry with respect to x-axis:
         o Phi -> pi -Phi
         o turn direction -> turn direction
         o x -> x 
        The required rewards functions can be restricted to turn angles Phi in [-90deg ... +90deg] 
        of the 'penalty landscape'. One quarter-segment (e.g [0 ... 90deg]) of the entire 
        'penalty landscape' is divided into Number_steps / 4 steps. Note that Number_steps / 4
        is an integer per requirement from above. 
        
        
        Implementation:
        ----------------        
        According to the above explanation, the 'penalty landscape' turns in each state transition 
            angular_speed = 360deg / number_steps
        or does not turn at all. Per convention, the 'penalty landscape' positions are confined to a
        homogeneously spaced grid of turn angles 
            [ 0, angular_speed, 2*angular_speed, ... , (number_steps -1)*angular_speed ]
        Lets define 
            strongest_penality_abs_idx = number_steps / 4
        Hence, 
            strongest_penality_abs_idx*angular_speed = (number_steps / 4) * (360deg / number_steps)= 90deg
        It follows that 
           o Phi = - strongest_penality_abs_idx*angular_speed  maximized the control penalties 
             for the positive domain (i.e. where x > 0)
           o Phi = + strongest_penality_abs_idx*angular_speed  maximized the control penalties 
             for the negative domain (i.e. where x < 0)
        Exploiting reflection symmetry the required grid of reward functions can be reduced to
            [ -strongest_penality_abs_idx*angular_speed, ..., -angular_speed, 
              0, 
              angular_speed, ..., strongest_penality_abs_idx*angular_speed ]
        This has the advantage, that either end of the grid represents the worst case points of the 
        """
        '''
'''
        if (number_steps < 1) or (number_steps % 4 != 0):
            raise ValueError('number_steps must be positive and integer multiple of 4')
        #
        k = number_steps // 4 # integer devision
        '''
'''
        # TODO extra Funktion für k --> im Konstruktor gleich aufgerufen
        k = self._strongest_penality_abs_idx
        angle_gid = np.arange(-k,k+1) * 2*pi / number_steps 
        reward_functions = [reward_function.reward_function(Phi, max_required_step) for Phi in angle_gid]
        #
        # store in member variables
        #self._strongest_penality_abs_idx = k

        self._penalty_functions_array = np.array(reward_functions)
        return self._penalty_functions_array

    def compute_strongest_penalty_absIdx(self, number_steps):
        if (number_steps < 1) or (number_steps % 4 != 0):
            raise ValueError('number_steps must be positive and integer multiple of 4')

        _strongest_penality_abs_idx = number_steps // 4  # integer devision
        return _strongest_penality_abs_idx


'''


class environment:
    # Done reset Methoden unbenannt, da zweimal vorhanden --> Verwirrung
    # Done Methoden umgeschrieben
    def __init__(self, number_steps, max_required_step, safe_zone):
        self._dynamics = dynamics(number_steps, max_required_step, safe_zone)
        self.reset_position_zero()

    def reset_position_zero(self):
        self._control_position = self.reset_position(0)

    def reset_position(self, control_start_value):
        self._control_position = control_start_value

    def reward(self):
        return self._dynamics.reward(self._control_position)




    # Done wird nicht aufgrufen
    '''
    def state_transition(self, control_value_change):
        """
        applies action and returns reward
        """
        # not yet implemented: test if action is allowed
        self._control_position += control_value_change
        self._dynamics.state_transition(self._control_position)
        return self.reward()
    '''
    def state_transition(self, control_value):
        """
        applies action and returns reward
        """
        # not yet implemented: test if action is allowed
        self._control_position = control_value
        self._dynamics.state_transition(self._control_position)
        return self.reward()

    def get_penalty_function(self):
        return self._dynamics.get_penalty_function()


    def optimal_position(self):
        return self._dynamics.optimal_position()

    def optimal_reward(self):
        return self._dynamics.optimal_reward()









      
# EOF
