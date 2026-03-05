import unittest
from industrial_benchmark_python.IDS import IDS
import numpy as np

class TestIB(unittest.TestCase):

    def all_states(self, env):
        s = env.state
        array = [
            s.operational_cost_buffer[0], s.operational_cost_buffer[1],
            s.operational_cost_buffer[2], s.operational_cost_buffer[3],
            s.operational_cost_buffer[4], s.operational_cost_buffer[5],
            s.operational_cost_buffer[6], s.operational_cost_buffer[7],
            s.operational_cost_buffer[8], s.operational_cost_buffer[9],
            s.current_operational_cost, s.hidden_gain, s.hidden_velocity,
            s.hidden_shift, s.gs_domain, s.gs_sys_response, s.gs_phi_idx,
            s.effective_gain, s.effective_velocity, s.miscalibration,
            s.consumption, s.setpoint, s.velocity, s.gain, s.shift,
            s.fatigue, s.fatigue_base, s.operational_cost_conv, s.reward,
        ]
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
            env = IDS(p[i], initial_seed=1005 + i)

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
