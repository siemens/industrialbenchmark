import unittest
from industrial_benchmark_python.IDS import IDS
import numpy as np

class TestIB(unittest.TestCase):

    def all_states(self,env):
        array = [env.state['o'][0],env.state['o'][1],env.state['o'][2],env.state['o'][3],env.state['o'][4],env.state['o'][5],env.state['o'][6],env.state['o'][7],env.state['o'][8],env.state['o'][9],env.state['coc'],env.state['hg'], env.state['hv'], env.state['he'], env.state['gs_domain'], env.state['gs_sys_response'], env.state['gs_phi_idx'], env.state['ge'], env.state['ve'],env.state['MC'],env.state['c'],env.state['p'],env.state['v'],env.state['g'],env.state['h'], env.state['f'], env.state['fb'], env.state['oc'], env.state['reward'] ]
        return array
    def test_example(self):
        # fixed seed for setpoint
        np.random.seed(501)
        p = []
        trajectories = 10
        T = 1000 # perform 1000 actions/ steps

        # generate different values of setpoint
        for value in range(10):
            p.append(np.random.randint(1, 100))

        # perform 1000 actions per trajectory
        for i in range(trajectories):
            # generate IB with fixed seed. If no seed is given, IB is generated with random values
            env = IDS(p[i], inital_seed=1005 + i)

            for j in range(T):
                at = 2 * np.random.rand(3) - 1
                # perform action
                env.step(at)
                markovStates = self.all_states(env)
                if (j==0):
                    markovStates_all = np.array(markovStates)
                else:
                    markovStates_all = np.vstack([markovStates_all, markovStates])

            #np.savetxt('markovStates_test' + str(i) + '.csv',  markovStates_all, delimiter=',')

            compare_file_markovStates = np.genfromtxt('test_data/markovStates'+str(i)+'.csv', delimiter=',')

            # test if test files and original files are equal
            np.testing.assert_array_almost_equal(compare_file_markovStates, markovStates_all)