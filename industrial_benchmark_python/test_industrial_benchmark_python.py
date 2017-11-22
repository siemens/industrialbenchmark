# coding: utf8
import unittest
from IDS import IDS
import numpy as np
from numpy import genfromtxt


class TestIB(unittest.TestCase):

    def test_example(self):
        # fixed seed for setpoint
        np.random.seed(501)
        p = []

        # generate different values of setpoint
        for value in range(5):
            p.append(np.random.randint(1, 1000))

        for i in range(5):
            # generate IB with fixed seed. If no seed is given, IB is generated with random values
            env = IDS(p[i], inital_seed=1005 + i)
            at = 2 * np.random.rand(3) - 1

            # perform action
            markovStates = env.step(at)

            # get results: all States and all operational costs
            all_States = env.allStates()
            operational_States = env.operational_cost_Buffer()

            # before the actual test: generate files with which all test files can be compared
            #np.savetxt('all_States'+str(i)+'.csv', all_States)
            #np.savetxt('operational_States'+str(i)+'.csv', operational_States)

            # test files
            test_all_States = genfromtxt('all_States'+str(i)+'.csv', delimiter=',')
            test_operational_States = genfromtxt('operational_States'+str(i)+'.csv', delimiter=',')

            # test if test files and original files are equal
            np.testing.assert_array_almost_equal(test_all_States, all_States)
            np.testing.assert_array_almost_equal(test_operational_States, operational_States)

