# coding: utf8

import numpy as np
from numpy import pi, sign
import reward_function
from enum import Enum


# DONE diese Klasse in eine extra
# DONE neutral und initial raus
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

        # internal state
        self.reset()

    def _check_safe_zone(self, safe_zone):
        if (safe_zone < 0):
            raise ValueError('safe_zone must be non-negative')
        return safe_zone

    # DONE wird nicht aufgerufen
    '''
    def _check_number_steps(self, number_steps):
        if (number_steps < 1):
            raise ValueError('number_steps must be positive integer')
        if (number_steps // 4):
            raise ValueError('number_steps must be integer multiple of 4')
        return number_steps
    '''

    # DONE Funktionsnamen unbenannt --> alle Funktionen nun mit penalty
    def reset(self):
        self._domain = self.Domain.positive # DONE initial raus, dafür positive
        self._Phi_idx = 0
        self._system_response = self.System_Response.advantageous
        self._current_penalty_function = self.get_penalty_function()

    def reward(self, pos):
        return self._current_penalty_function.reward(pos)

    def optimal_position(self):
        return self._current_penalty_function.optimum_radius

    def optimal_reward(self):
        return self._current_penalty_function.optimum_value


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

        # DONE Reihenfolge ändern
        # (4) apply symmetry
        self._Phi_idx = self._apply_symmetry(self._Phi_idx)

        # (5) if self._Phi_idx == 0: reset internal state
        if (self._Phi_idx == 0) and (abs(new_control_value) <= self._safe_zone):
            self.reset()

        # print("  self._Phi_idx = " + str(self._Phi_idx))
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
        """
        By employing reflection symmetry with respect to x-axis:
         o Phi -> pi -Phi
         o turn direction -> turn direction
         o x -> x
       """
        if abs(Phi_idx) < self._strongest_penality_abs_idx:
            return Phi_idx

        # TODO im anderen Code: Phi_idx + (4*self._strongest_penality_abs_idx) % (4*self._strongest_penality_abs_idx)
        Phi_idx = Phi_idx % (4 * self._strongest_penality_abs_idx)
        Phi_idx = 2 * self._strongest_penality_abs_idx - Phi_idx
        return Phi_idx

    def get_penalty_function(self):
        #DONE: if Abfrage raus (_apply_symmetry schon vorher angewendet)
        """
        if Phi_idx is None:
            p = self._Phi_idx
        else:
            p = self._apply_symmetry(Phi_idx)
        """
        # DONE: neue Methode, if raus
        # if (abs(p) > self._strongest_penality_abs_idx):
        #                 raise IndexError("Turn index (" + str(p) + ") exceeds bounds (" + str(self._strongest_penality_abs_idx) + ")")
        # DONE idx außerhalb von ArrayIndex
        idx = int(self._strongest_penality_abs_idx + self._Phi_idx)
        # DONE: es fehlte if Abfrage:
        if idx < 0:
            idx = idx + len(self._penalty_functions_array)
        return self._penalty_functions_array[idx]

    def _define_reward_functions(self, number_steps, max_required_step):
        """
        By employing reflection symmetry with respect to x-axis:
         o Phi -> pi -Phi
         o turn direction -> turn direction
         o x -> x
        """
        # DONE extra Methode (compute_strongest_penalty_absIdx) für k --> im Konstruktor gleich aufgerufen
        k = self._strongest_penality_abs_idx
        angle_gid = np.arange(-k, k + 1) * 2 * pi / number_steps
        reward_functions = [reward_function.reward_function(Phi, max_required_step) for Phi in angle_gid]
        #
        # store in member variables
        # self._strongest_penality_abs_idx = k

        self._penalty_functions_array = np.array(reward_functions)
        return self._penalty_functions_array

    def compute_strongest_penalty_absIdx(self, number_steps):
        if (number_steps < 1) or (number_steps % 4 != 0):
            raise ValueError('number_steps must be positive and integer multiple of 4')

        _strongest_penality_abs_idx = number_steps // 4  # integer devision
        return _strongest_penality_abs_idx

