"""
The MIT License (MIT)

Copyright 2020 Siemens AG, Technical University of Berlin

Authors: Phillip Swazinna (Earlier Version: Ludwig Winkler)

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

from typing import Any

import gymnasium as gym
import numpy as np
from numpy.typing import NDArray

from industrial_benchmark_python.IDS import IDS


class IBGym(gym.Env):
    """Gymnasium wrapper for the industrial benchmark."""

    metadata: dict[str, Any] = {"render_modes": ["human"]}

    def __init__(
        self,
        setpoint: float,
        reward_type: str = "classic",
        action_type: str = "continuous",
        observation_type: str = "classic",
        reset_after_timesteps: int = 1000,
        init_seed: int | None = None,
        n_past_timesteps: int = 30,
        render_mode: str | None = None,
    ) -> None:
        """Initialize the Gymnasium-wrapped industrial benchmark.

        Args:
            setpoint: Determines behavior of the industrial benchmark.
            reward_type: ``"classic"`` for absolute reward or ``"delta"``
                for change in reward.
            action_type: ``"discrete"`` or ``"continuous"``.
            observation_type: ``"classic"`` for single state frame or
                ``"include_past"`` for N stacked frames.
            reset_after_timesteps: Episode length before auto-reset.
            init_seed: Seed for reproducibility.
            n_past_timesteps: Number of past frames when using
                ``observation_type="include_past"``.
            render_mode: Gymnasium render mode.
        """
        super().__init__()
        self.render_mode = render_mode

        # IB environment parameter
        self.setpoint = setpoint

        # initial seeding
        self.init_seed = init_seed
        np.random.seed(self.init_seed)

        # Reward and observation config
        self.reward_function = reward_type
        self.reset_after_timesteps = reset_after_timesteps
        self.action_type = action_type
        self.observation_type = observation_type
        self.n_past_timesteps = n_past_timesteps

        # Mutable episode state — initialized in reset()
        self.IB: IDS | None = None
        self.info: dict[str, Any] = {}
        self.reward: float = 0.0
        self.delta_reward: float = 0.0
        self.smoothed_reward: float = 0.0
        self.env_steps: int = 0
        self.terminated: bool = False
        self.last_action: Any = None
        self.observation: Any = None

        # Action space
        if self.action_type == "discrete":
            self.action_space = gym.spaces.Discrete(27)
            self.env_action: list[list[int]] = [
                [v, g, s]
                for v in [-1, 0, 1]
                for g in [-1, 0, 1]
                for s in [-1, 0, 1]
            ]
        elif self.action_type == "continuous":
            self.action_space = gym.spaces.Box(
                np.array([-1, -1, -1], dtype=np.float32),
                np.array([+1, +1, +1], dtype=np.float32),
            )
        else:
            raise ValueError('action_type must be "discrete" or "continuous"')

        # Observation space
        single_low = np.array([0, 0, 0, 0, 0, 0], dtype=np.float32)
        single_high = np.array([100, 100, 100, 100, 1000, 1000], dtype=np.float32)

        if self.observation_type == "classic":
            self.observation_space = gym.spaces.Box(low=single_low, high=single_high)
        elif self.observation_type == "include_past":
            low = np.tile(single_low, self.n_past_timesteps)
            high = np.tile(single_high, self.n_past_timesteps)
            self.observation_space = gym.spaces.Box(low=low, high=high)
        else:
            raise ValueError('observation_type must be "classic" or "include_past"')

        self.reset()

    def step(
        self, action: Any,
    ) -> tuple[NDArray[np.float64], float, bool, bool, dict[str, Any]]:
        """Take one step in the environment.

        Returns:
            A 5-tuple of (observation, reward, terminated, truncated, info)
            following the Gymnasium API.
        """
        if self.terminated:
            self.reset()

        self.last_action = action

        if self.action_type == "discrete":
            self.IB.step(self.env_action[action])
        elif self.action_type == "continuous":
            self.IB.step(action)

        return_observation = self._update_observation()

        new_reward = -self.IB.state.cost
        self.delta_reward = new_reward - self.reward
        self.reward = new_reward
        self.smoothed_reward = 0.9 * self.smoothed_reward + 0.1 * self.reward

        self.env_steps += 1
        truncated = False
        if self.env_steps >= self.reset_after_timesteps:
            self.terminated = True

        if self.reward_function == "classic":
            return_reward = self.reward
        elif self.reward_function == "delta":
            return_reward = self.delta_reward
        else:
            raise ValueError('reward_function must be "classic" or "delta"')

        self.info = self._markovian_state()
        return return_observation, return_reward, self.terminated, truncated, self.info

    def reset(
        self,
        *,
        seed: int | None = None,
        options: dict[str, Any] | None = None,
    ) -> tuple[NDArray[np.float64], dict[str, Any]]:
        """Reset the environment.

        Returns:
            A 2-tuple of (observation, info) following the Gymnasium API.
        """
        super().reset(seed=seed, options=options)

        self.IB = IDS(self.setpoint, initial_seed=self.init_seed)
        self.init_seed = np.random.randint(0, 100000)

        if self.observation_type == "include_past":
            self.observation = []

        return_observation = self._update_observation()

        self.info = self._markovian_state()
        self.reward = -self.IB.state.cost
        self.delta_reward = 0.0
        self.smoothed_reward = self.reward
        self.env_steps = 0
        self.terminated = False

        return return_observation, self.info

    def render(self) -> None:
        """Print the current reward, state, and last action."""
        if self.render_mode == "human":
            print(
                "Reward:", self.reward,
                "State (v,g,s):", self.IB.visibleState()[1:4],
                "Action:", self.last_action,
            )

    def _update_observation(self) -> NDArray[np.float64]:
        if self.observation_type == "classic":
            self.observation = self.IB.visibleState()[:-2]
            return self.observation

        if self.observation_type == "include_past":
            single_state = self.IB.visibleState()[:-2]

            if len(self.observation) > 0:
                self.observation.pop()

            self.observation.insert(0, single_state)

            if len(self.observation) < self.n_past_timesteps:
                oldest_obs = self.observation[-1]
                oldest_repeated = [oldest_obs] * (self.n_past_timesteps - len(self.observation))
                self.observation = self.observation + oldest_repeated

            return np.hstack(self.observation)

        raise ValueError('observation_type must be "classic" or "include_past"')

    def _markovian_state(self) -> dict[str, float]:
        """Return the full Markov state as a dictionary."""
        s = self.IB.state
        keys = [
            "setpoint", "velocity", "gain", "shift", "fatigue", "consumption",
            "op_cost_t0", "op_cost_t1", "op_cost_t2", "op_cost_t3", "op_cost_t4",
            "op_cost_t5", "op_cost_t6", "op_cost_t7", "op_cost_t8", "op_cost_t9",
            "ml1", "ml2", "ml3", "hv", "hg",
        ]
        values = [
            s.setpoint, s.velocity, s.gain, s.shift, s.fatigue, s.consumption,
            s.operational_cost_buffer[0], s.operational_cost_buffer[1],
            s.operational_cost_buffer[2], s.operational_cost_buffer[3],
            s.operational_cost_buffer[4], s.operational_cost_buffer[5],
            s.operational_cost_buffer[6], s.operational_cost_buffer[7],
            s.operational_cost_buffer[8], s.operational_cost_buffer[9],
            s.gs_domain, s.gs_sys_response, s.gs_phi_idx,
            s.hidden_velocity, s.hidden_gain,
        ]
        return dict(zip(keys, values))
