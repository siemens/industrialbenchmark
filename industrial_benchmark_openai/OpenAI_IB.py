'''
The MIT License (MIT)

Copyright 2017 Technical University of Berlin

Authors: Ludwig Winkler

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
import gym
from gym import error, spaces, utils
from gym.utils import seeding
import sys
sys.path.append('../')
from industrial_benchmark_python.IDS import IDS

from collections import OrderedDict

class OpenAI_IB(gym.Env):

        def __init__(self, setpoint, reward_type, action_type):

                # Setting up the IB environment
                self.setpoint = setpoint
                self.IB = IDS(setpoint)
                # Used to determine whether to return the absolute value or the relative change in the cost function
                self.reward_function = reward_type
                # Used to set an arbitrary limit of how many time steps the environment can take before resetting
                # Could potentially be unlimited
                self.reset_after_timesteps = 1000
                self.action_type = action_type

                # Action space and the observation space
                if self.action_type == 'discrete':

                        # Discrete action space with three different values per steerings for the three steerings ( 3^3 = 27)
                        self.action_space = spaces.Discrete(27)

                        # Observation space for [setpoint, velocity, gain, shift, fatigue, consumption, cost]
                        self.observation_space = spaces.Box(low=np.array([0, 0, 0, 0, 0, 0]), high=np.array([100, 100, 100, 1000, 1000, 1000]))

                        # A list of all possible actions discretized into [-1,0,1] e.g. [[-1,-1,-1],[-1,-1,0],[-1,-1,1],[-1,0,-1],[-1,0,0], ... ]
                        # Network has 27 outputs and chooses one environmental action out of the discretized 27 possible actions
                        self.env_action = []
                        for v in [-1, 0, 1]:
                                for g in [-1, 0, 1]:
                                        for s in [-1, 0, 1]:
                                                self.env_action.append([v, g, s])
                        self.observation_space = spaces.Box(low=np.array([0, 0, 0, 0, 0, 0]), high=np.array([100, 100, 100, 1000, 1000, 1000]))

                elif self.action_type == 'continuous':

                        # Continuous action space for each steering [-1,1]
                        self.action_space = spaces.Box(np.array([-1,-1,-1]), np.array([+1,+1,+1]))

                        # Observation space for [setpoint, velocity, gain, shift, fatigue, consumption, cost]
                        self.observation_space = spaces.Box(low=np.array([0, 0, 0, 0, 0, 0]), high=np.array([100, 100, 100, 1000, 1000, 1000]))

                else:
                        raise ValueError('Invalid action_type. action_space can either be "discrete" or "continuous"')

                # Values returned by the OpenAI environment placeholder
                # IB.visibleState() returns [setpoint, velocity, gain, shift, fatigue, consumption, cost]
                # Only [velocity, gain, shift, fatigue, consumption] are used as observation
                self.observation = self.IB.visibleState()[:-1]
                self.reward = -self.IB.state['cost']
                self.done = False
                self.info = self.markovianState()

                # Alternative reward that returns the improvement or decrease in the cost function
                # If the cost function improves/decreases, the reward is positiv
                # If the cost function deteriorates/increases, the reward is negative
                # e.g.: -400 -> -450 = delta_reward of -50
                self.delta_reward = 0

                # env_steps is used for the self.done variable. If it's larger than e.g. 1000, the environment resets
                # Can be an arbitrary high number
                self.env_steps = 0

                # smoothed_cost is used as a smoother cost function for monitoring the agent & environment with lower variance
                # Updates itself with 0.95*old_cost + 0.05*new_cost or any other linear combination
                self.smoothed_cost = self.IB.state['cost']

                self._seed()
                self._reset()

        def _step(self, _action):

                # Executing the action and saving the observation
                if self.action_type == 'discrete':
                        self.IB.step(self.env_action[_action])
                elif self.action_type == 'continuous':
                        self.IB.step(_action)

                self.observation = self.IB.visibleState()[:-1]

                # Calculating both the relative reward (improvement or decrease) and updating the reward
                self.delta_reward = self.reward - self.IB.state['cost']
                self.reward = self.IB.state['cost']

                # Due to the very high stochasticity a smoothed cost function is easier to follow visually
                self.smoothed_cost = int(0.9 * self.smoothed_cost + 0.1 * self.IB.state['cost'])

                # Stopping condition
                if self.env_steps >= self.reset_after_timesteps:
                        self.done = True

                self.env_steps += 1

                # Two reward functions are available:
                # 'classic' which returns the original cost and
                # 'delta' which returns the change in the cost function w.r.t. the previous cost
                if self.reward_function == 'classic':
                        return_reward = -self.IB.state['cost']
                elif self.reward_function == 'delta':
                        return_reward = self.delta_reward
                else:
                        raise ValueError('Invalid reward function specification. "classic" for the original cost function or "delta" for the change in the cost fucntion between steps.')

                # Print to track agent & environment during training
                print (' Cost smoothed:', -self.smoothed_cost, ' State (v,g,s):', np.around(self.IB.visibleState()[1:4], 0), '\t Action: ', _action,)

                self.info = self.markovianState()
                # reward is divided by 100 to improve learning
                return self.observation, return_reward/100, self.done, self.info

        def _reset(self):

                # Resetting the entire environment
                self.IB = IDS(self.setpoint)
                self.observation = self.IB.visibleState()[:-1]
                self.info = self.markovianState()
                self.reward = -self.IB.state['cost']
                self.env_steps = 0
                self.done = False

                print ('\n Reset')
                print

                return self.observation

        def _seed(self, seed=None):
                self.np_random, seed = seeding.np_random(seed)
                return [seed]

        #
        def _render(self, mode='human', close=False):
                print ('Rendering of the environment is not supported')
                pass

        def markovianState(self):
                markovian_states_variables = ['setpoint', 'velocity', 'gain', 'shift', 'fatigue', 'consumption',
                                              'op_cost_t0','op_cost_t1', 'op_cost_t2', 'op_cost_t3', 'op_cost_t4',
                                              'op_cost_t5','op_cost_t6', 'op_cost_t7', 'op_cost_t8', 'op_cost_t9',
                                              'ml1', 'ml2', 'ml3', 'hv', 'hg']

                markovian_states_values = [self.IB.state['p'],
                                           self.IB.state['v'],
                                           self.IB.state['g'],
                                           self.IB.state['s'],
                                           self.IB.state['f'],
                                           self.IB.state['c'],
                                           self.IB.state['o'][0],
                                           self.IB.state['o'][1],
                                           self.IB.state['o'][2],
                                           self.IB.state['o'][3],
                                           self.IB.state['o'][4],
                                           self.IB.state['o'][5],
                                           self.IB.state['o'][6],
                                           self.IB.state['o'][7],
                                           self.IB.state['o'][8],
                                           self.IB.state['o'][9],
                                           self.IB.state['gs_domain'],
                                           self.IB.state['gs_sys_response'],
                                           self.IB.state['gs_phi_idx'],
                                           self.IB.state['hv'],
                                           self.IB.state['hg']]

                info = OrderedDict(zip(markovian_states_variables,markovian_states_values))

                # Prints the markovian states at every time step
                # for i in info.items():
                #         print i

                return info