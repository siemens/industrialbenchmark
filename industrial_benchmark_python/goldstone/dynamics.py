# coding: utf8
import numpy as np
from numpy import pi, sign
import reward_function
from enum import Enum
'''
The MIT License (MIT)

Copyright 2017 Siemens AG

Author: Judith Mosandl

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

class dynamics:
    class Domain(Enum):
        negative = -1
        positive = +1

    class System_Response(Enum):
        advantageous = +1
        disadvantageous = -1

    def __init__(self, number_steps, max_required_step, safe_zone):
        self._safe_zone = self._check_safe_zone(safe_zone)
        self._strongest_penality_abs_idx = self.compute_strongest_penalty_absIdx(number_steps)
        self._penalty_functions_array = self._define_reward_functions(number_steps, max_required_step)

        self.reset()

    def _check_safe_zone(self, safe_zone):
        if (safe_zone < 0):
            raise ValueError('safe_zone must be non-negative')
        return safe_zone

    def reset(self):
        self._domain = self.Domain.positive
        self._Phi_idx = 0
        self._system_response = self.System_Response.advantageous
        self._current_penalty_function = self.get_penalty_function()

    def reward(self, pos):
        return self._current_penalty_function.reward(pos)

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

        # (4) apply symmetry
        self._Phi_idx = self._apply_symmetry(self._Phi_idx)

        # (5) if self._Phi_idx == 0: reset internal state
        if (self._Phi_idx == 0) and (abs(new_control_value) <= self._safe_zone):
            self.reset()

        self._current_penalty_function = self.get_penalty_function()

    def _compute_domain(self, new_position):
        #compute the new domain of control action
        if abs(new_position) <= self._safe_zone:
            return self._domain
        else:
            return self.Domain(sign(new_position))

    def _compute_angular_step(self, new_position):
        # cool down: when position close to zero
        if abs(new_position) <= self._safe_zone:  # cool down
            return -sign(self._Phi_idx)

        if self._Phi_idx == -self._domain.value * self._strongest_penality_abs_idx:
            return 0
        return self._system_response.value * sign(new_position)

    def _updated_system_response(self, new_Phi_idx, new_position):
        if abs(new_Phi_idx) >= self._strongest_penality_abs_idx:
            return self.System_Response.disadvantageous
        else:
            return self._system_response

    def _apply_symmetry(self, Phi_idx):
        if abs(Phi_idx) < self._strongest_penality_abs_idx:
            return Phi_idx

        Phi_idx = (Phi_idx + (4 * self._strongest_penality_abs_idx)) % (4 * self._strongest_penality_abs_idx)
        Phi_idx = 2 * self._strongest_penality_abs_idx - Phi_idx
        return Phi_idx

    def get_penalty_function(self):
        idx = int(self._strongest_penality_abs_idx + self._Phi_idx)
        if idx < 0:
            idx = idx + len(self._penalty_functions_array)
        return self._penalty_functions_array[idx]

    def _define_reward_functions(self, number_steps, max_required_step):
        k = self._strongest_penality_abs_idx
        angle_gid = np.arange(-k, k + 1) * 2 * pi / number_steps
        reward_functions = [reward_function.reward_function(Phi, max_required_step) for Phi in angle_gid]

        self._penalty_functions_array = np.array(reward_functions)
        return self._penalty_functions_array

    def compute_strongest_penalty_absIdx(self, number_steps):
        if (number_steps < 1) or (number_steps % 4 != 0):
            raise ValueError('number_steps must be positive and integer multiple of 4')

        _strongest_penality_abs_idx = number_steps // 4
        return _strongest_penality_abs_idx

