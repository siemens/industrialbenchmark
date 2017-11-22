from industrial_benchmark_python.IDS import IDS
from collections import OrderedDict
import numpy as np

class interpreter():

    def __init__(self, setpoint, reward_type, action_type):

        self.setpoint = setpoint
        self.reward_type = reward_type
        self.action_type = action_type

        # create environment
        self.env = IDS(self.setpoint)

        #self.observation = self.env.visibleState()
        #self.markov_state = self.env.markovState()
        #self.states = self.env.visibleState()
        self.reward = self.env.state['reward']
        self.done = False
        self.hidden = self.markovianState()


    def step(self, action):

        # Agent performs one step and gets new state
        # save state in State-Buffer (30 states)
        self.env.step(action)
        self.states = self.env.visibleState()
        #markov_state = self.list_to_numpy()
        #self.hidden = self.markovianState()
        self.state = self.create_state_array()
        #print(self.state)
        #print(self.env.visibleState())
        return self.state


    def create_state_array(self):
        return np.array([ ('p',self.env.state['p']), ('v',self.env.state['v']), ('g',self.env.state['g']), ('h',self.env.state['h']), ('f',self.env.state['f']), ('c',self.env.state['c']),('cost',self.env.state['cost']), ('reward',self.env.state['reward']) ])



    def markovianState(self):
        markovian_states_variables = ['setpoint', 'velocity', 'gain', 'shift', 'fatigue', 'consumption',
                                      'op_cost_t0', 'op_cost_t1', 'op_cost_t2', 'op_cost_t3', 'op_cost_t4',
                                      'op_cost_t5', 'op_cost_t6', 'op_cost_t7', 'op_cost_t8', 'op_cost_t9',
                                      'ml1', 'ml2', 'ml3', 'hv', 'hg']

        markovian_states_values = [self.env.state['p'],
                                   self.env.state['v'],
                                   self.env.state['g'],
                                   self.env.state['h'],
                                   self.env.state['f'],
                                   self.env.state['c'],
                                   self.env.state['o'][0],
                                   self.env.state['o'][1],
                                   self.env.state['o'][2],
                                   self.env.state['o'][3],
                                   self.env.state['o'][4],
                                   self.env.state['o'][5],
                                   self.env.state['o'][6],
                                   self.env.state['o'][7],
                                   self.env.state['o'][8],
                                   self.env.state['o'][9],
                                   self.env.state['gs_domain'],
                                   self.env.state['gs_sys_response'],
                                   self.env.state['gs_phi_idx'],
                                   self.env.state['hv'],
                                   self.env.state['hg']]

        hidden_state = OrderedDict(zip(markovian_states_variables, markovian_states_values))

        # Prints the markovian states at every time step
        # for i in info.items():
        #         print i

        return hidden_state