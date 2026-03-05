"""
The MIT License (MIT)

Copyright 2017 Siemens AG

Author: Stefan Depeweg

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
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

import numpy as np
from numpy.typing import NDArray

from industrial_benchmark_python.goldstone.environment import environment as GoldstoneEnvironment
from industrial_benchmark_python.EffectiveAction import EffectiveAction


@dataclass
class BenchmarkState:
    """Typed state representation for the industrial benchmark.

    Replaces the former ``OrderedDict`` with cryptic single-letter keys
    (e.g. ``'o'``, ``'coc'``, ``'fb'``) with clearly named, typed fields.
    """

    # Internal buffers & costs
    operational_cost_buffer: NDArray[np.float64] = field(default_factory=lambda: np.zeros(10))
    current_operational_cost: float = 0.0
    fatigue_base: float = 0.0
    operational_cost_conv: float = 0.0
    hidden_gain: float = 0.0
    hidden_velocity: float = 0.0
    hidden_shift: float = 0.0

    # Goldstone variables
    gs_domain: int = 0
    gs_sys_response: int = 0
    gs_phi_idx: int = 0
    effective_gain: float = 0.0
    effective_velocity: float = 0.0
    miscalibration: float = 0.0

    # Observables
    setpoint: float = 50.0
    velocity: float = 50.0
    gain: float = 50.0
    shift: float = 50.0
    fatigue: float = 0.0
    consumption: float = 0.0
    cost: float = 0.0
    reward: float = 0.0

    # Ordered list of observable keys for external access
    OBSERVABLE_KEYS: list[str] = field(
        default_factory=lambda: [
            "setpoint", "velocity", "gain", "shift",
            "fatigue", "consumption", "cost", "reward",
        ],
        repr=False,
    )

    def __getitem__(self, key: str) -> Any:
        """Allow dict-style access for backward compatibility."""
        return getattr(self, key)

    def __setitem__(self, key: str, value: Any) -> None:
        """Allow dict-style assignment for backward compatibility."""
        setattr(self, key, value)

    def observable_array(self) -> NDArray[np.float64]:
        """Return observable state as a flat numpy array."""
        return np.concatenate([
            np.array(getattr(self, k)).ravel() for k in self.OBSERVABLE_KEYS
        ])

    def full_array(self) -> NDArray[np.float64]:
        """Return the full Markov state as a flat numpy array."""
        all_keys = [
            "operational_cost_buffer", "current_operational_cost",
            "fatigue_base", "operational_cost_conv",
            "hidden_gain", "hidden_velocity", "hidden_shift",
            "gs_domain", "gs_sys_response", "gs_phi_idx",
            "effective_gain", "effective_velocity", "miscalibration",
            "setpoint", "velocity", "gain", "shift",
            "fatigue", "consumption", "cost", "reward",
        ]
        return np.concatenate([
            np.array(getattr(self, k)).ravel() for k in all_keys
        ])


class IDS:
    """Lightweight Python implementation of the industrial benchmark.

    Uses the same standard settings as in
    ``src/main/resources/simTest.properties`` of the Java implementation.
    """

    def __init__(
        self,
        p: float = 50,
        stationary_p: bool = True,
        initial_seed: int | None = None,
    ) -> None:
        """Initialize the industrial benchmark.

        Args:
            p: Setpoint hyperparameter (1-100) affecting dynamics and
                stochasticity.
            stationary_p: If False the setpoint varies over time, making
                the system more non-stationary.
            initial_seed: Optional RNG seed for reproducibility.
        """
        np.random.seed(initial_seed)

        # Constants
        self.max_required_step: float = np.sin(15.0 / 180.0 * np.pi)
        self.gs_bound: float = 1.5
        self.gs_setpoint_dependency: float = 0.02
        self.gs_scale: float = 2.0 * self.gs_bound + 100.0 * self.gs_setpoint_dependency

        # Cost / reward weighting constants
        self.CRF: float = 3.0
        self.CRC: float = 1.0
        self.CRGS: float = 25.0

        self.stationary_p = stationary_p
        self.gs_environment = GoldstoneEnvironment(
            24, self.max_required_step, self.max_required_step / 2.0,
        )

        self.state = BenchmarkState()
        self.state.setpoint = p
        self.state.gs_domain = self.gs_environment._dynamics.Domain.positive.value
        self.state.gs_sys_response = self.gs_environment._dynamics.System_Response.advantageous.value

        self._init = True
        self._define_new_sequence()
        self.step(np.zeros(3))

    def visibleState(self) -> NDArray[np.float64]:
        """Return the observable portion of the state."""
        return self.state.observable_array()

    def markovState(self) -> NDArray[np.float64]:
        """Return the full Markov state."""
        return self.state.full_array()

    def step(self, delta: NDArray[np.float64]) -> None:
        """Advance the benchmark by one time step."""
        self._update_setpoint()
        self._add_action(delta)
        self._update_fatigue()
        self._update_current_operational_cost()
        self._update_operational_cost_convolution()
        self._update_gs()
        self._update_operational_costs()
        self._update_cost()

    # ------------------------------------------------------------------
    # Internal dynamics
    # ------------------------------------------------------------------

    def _update_setpoint(self) -> None:
        if self.stationary_p:
            return

        if self._p_step == self._p_steps:
            self._define_new_sequence()

        new_p = self.state.setpoint + self._p_ch
        if new_p > 100 or new_p < 0:
            if np.random.rand() > 0.5:
                self._p_ch *= -1

        self.state.setpoint = np.clip(new_p, 0, 100)
        self._p_step += 1

    def _add_action(self, delta: NDArray[np.float64]) -> None:
        delta = np.clip(delta, -1, 1)
        self.state.velocity = np.clip(self.state.velocity + delta[0], 0.0, 100.0)
        self.state.gain = np.clip(self.state.gain + 10 * delta[1], 0.0, 100.0)
        self.state.shift = np.clip(
            self.state.shift
            + ((self.max_required_step / 0.9) * 100.0 / self.gs_scale) * delta[2],
            0.0, 100.0,
        )
        self.state.hidden_shift = np.clip(
            self.gs_scale * self.state.shift / 100.0
            - self.gs_setpoint_dependency * self.state.setpoint
            - self.gs_bound,
            -self.gs_bound, self.gs_bound,
        )

    def _update_fatigue(self) -> None:
        exp_lambda = 0.1
        action_tolerance = 0.05
        fatigue_amplification = 1.1
        fatigue_amplification_max = 5.0
        fatigue_amplification_start = 1.2

        velocity = self.state.velocity
        gain = self.state.gain
        setpoint = self.state.setpoint

        hidden_gain = self.state.hidden_gain
        hidden_velocity = self.state.hidden_velocity

        eff_act = EffectiveAction(velocity, gain, setpoint)
        eff_act_velocity = eff_act.getEffectiveVelocity()
        eff_act_gain = eff_act.getEffectiveGain()

        self.state.effective_gain = eff_act_gain
        self.state.effective_velocity = eff_act_velocity

        noise_e_g = np.random.exponential(exp_lambda)
        noise_e_v = np.random.exponential(exp_lambda)
        noise_u_g = np.random.rand()
        noise_u_v = np.random.rand()

        # BUG FIX: np.float() was removed in NumPy 1.24 (issue #17)
        noise_b_g = float(np.random.binomial(1, np.clip(eff_act_gain, 0.001, 0.999)))
        noise_b_v = float(np.random.binomial(1, np.clip(eff_act_velocity, 0.001, 0.999)))

        noise_gain = 2.0 * (1.0 / (1.0 + np.exp(-noise_e_g)) - 0.5)
        noise_velocity = 2.0 * (1.0 / (1.0 + np.exp(-noise_e_v)) - 0.5)

        noise_gain += (1 - noise_gain) * noise_u_g * noise_b_g * eff_act_gain
        noise_velocity += (1 - noise_velocity) * noise_u_v * noise_b_v * eff_act_velocity

        if eff_act_gain <= action_tolerance:
            hidden_gain = eff_act_gain
        elif hidden_gain >= fatigue_amplification_start:
            hidden_gain = np.minimum(fatigue_amplification_max, fatigue_amplification * hidden_gain)
        else:
            hidden_gain = 0.9 * hidden_gain + noise_gain / 3.0

        if eff_act_velocity <= action_tolerance:
            hidden_velocity = eff_act_velocity
        elif hidden_velocity >= fatigue_amplification_start:
            hidden_velocity = np.minimum(fatigue_amplification_max, fatigue_amplification * hidden_velocity)
        else:
            hidden_velocity = 0.9 * hidden_velocity + noise_velocity / 3.0

        if np.maximum(hidden_velocity, hidden_gain) == fatigue_amplification_max:
            alpha = 1.0 / (1.0 + np.exp(-np.random.normal(2.4, 0.4)))
        else:
            alpha = np.maximum(noise_velocity, noise_gain)

        fb = np.maximum(0, ((30000.0 / ((5 * velocity) + 100)) - 0.01 * (gain**2)))
        self.state.hidden_velocity = hidden_velocity
        self.state.hidden_gain = hidden_gain
        self.state.fatigue = (fb * (1 + 2 * alpha)) / 3.0
        self.state.fatigue_base = fb

    def _update_current_operational_cost(self) -> None:
        cost_setpoint = 2.0
        cost_velocity = 4.0
        cost_gain = 2.5

        gain = self.state.gain
        velocity = self.state.velocity
        setpoint = self.state.setpoint

        costs = cost_setpoint * setpoint + cost_gain * gain + cost_velocity * velocity
        o = np.exp(costs / 100.0)
        self.state.current_operational_cost = o

        if self._init:
            self.state.operational_cost_buffer += o
            self._init = False
        else:
            self.state.operational_cost_buffer[:-1] = self.state.operational_cost_buffer[1:]
            self.state.operational_cost_buffer[-1] = o

    def _update_operational_cost_convolution(self) -> None:
        conv_array = np.array([0.11111, 0.22222, 0.33333, 0.22222, 0.11111, 0.0, 0.0, 0.0, 0.0, 0.0])
        self.state.operational_cost_conv = np.dot(self.state.operational_cost_buffer, conv_array)

    def _update_gs(self) -> None:
        effective_shift = self.state.hidden_shift

        domain = self.state.gs_domain
        phi_idx = self.state.gs_phi_idx
        system_response = self.state.gs_sys_response

        reward, domain, phi_idx, system_response = self.gs_environment.state_transition(
            self.gs_environment._dynamics.Domain(domain),
            phi_idx,
            self.gs_environment._dynamics.System_Response(system_response),
            effective_shift,
        )
        self.state.miscalibration = -reward
        self.state.gs_domain = domain.value
        self.state.gs_sys_response = system_response.value
        self.state.gs_phi_idx = phi_idx

    def _update_operational_costs(self) -> None:
        rGS = self.state.miscalibration
        e_new_hidden = self.state.operational_cost_conv - (self.CRGS * (rGS - 1.0))
        operational_costs = e_new_hidden - np.random.randn() * (1 + 0.005 * e_new_hidden)
        self.state.consumption = operational_costs

    def _update_cost(self) -> None:
        fatigue = self.state.fatigue
        consumption = self.state.consumption
        cost = self.CRF * fatigue + self.CRC * consumption

        self.state.cost = cost
        self.state.reward = -cost

    def _define_new_sequence(self) -> None:
        length = np.random.randint(1, 100)
        self._p_steps = length
        self._p_step = 0
        p_ch = 2 * np.random.rand() - 1
        if np.random.rand() < 0.1:
            p_ch *= 0.0
        self._p_ch = p_ch
