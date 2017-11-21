from industrial_benchmark_python.IDS import IDS
from collections import OrderedDict

class interpreter():

    def __init__(self, setpoint, reward_type, action_type):

        self.setpoint = setpoint
        self.reward_type = reward_type
        self.action_type = action_type

        # create environment
        self.env = IDS(self.setpoint)

        self.observation = self.IB.visibleState()
        self.reward = self.IB.state['reward']
        self.done = False
        self.markovianState = self.markovianState()


    def step(self, action):

        # Agent performs one step and gets new state
        # save state in State-Buffer (30 states)
        self.env.step(action)
        self.observation = self.IB.visibleState()
        self.markovianState = self.markovianState()



    def markovianState(self):
        markovian_states_variables = ['setpoint', 'velocity', 'gain', 'shift', 'fatigue', 'consumption',
                                      'op_cost_t0', 'op_cost_t1', 'op_cost_t2', 'op_cost_t3', 'op_cost_t4',
                                      'op_cost_t5', 'op_cost_t6', 'op_cost_t7', 'op_cost_t8', 'op_cost_t9',
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

        hidden_state = OrderedDict(zip(markovian_states_variables, markovian_states_values))

        # Prints the markovian states at every time step
        # for i in info.items():
        #         print i

        return hidden_state