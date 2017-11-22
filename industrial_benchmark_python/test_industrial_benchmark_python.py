# coding: utf8

import unittest
from IDS import IDS
import numpy as np
from numpy import genfromtxt


class TestIB(unittest.TestCase):

    def test_example(self):


        for i in range(5):
            print("Test %i is running", i)

            # fixer seed für at
            np.random.seed(500 + 1)
            # Umwelt erzeugen mit fixem seed, falls kein seed angegeben, wird Umwelt mit random Generator wieder erzeugt
            env = IDS(p=100, inital_seed=1005 + i)
            at = 2 * np.random.rand(3) - 1  # [-0.72329821, -0.65278997,  0.90032803]


            # eine Aktion ausführen
            markovStates = env.step(at)
            # Get results: all States and all o-States
            all_States = env.allStates()
            operational_States = env.operational_cost_Buffer()

            print("all States")
            print(env.allStates())

            #np.savetxt('all_States'+str(i)+'.csv', all_States)
            #np.savetxt('operational_States'+str(i)+'.csv', operational_States)

            muster_all_States = genfromtxt('all_States'+str(i)+'.csv', delimiter=',')
            muster_operational_States = genfromtxt('operational_States'+str(i)+'.csv', delimiter=',')

            # Test if result and muster are equal
            np.testing.assert_array_almost_equal(muster_all_States, all_States)
            np.testing.assert_array_almost_equal(muster_operational_States, operational_States)


            # Test if cost = -reward
            #print("Cost")
            #cost = env.state['cost']
            #reward = env.state['reward']
            #np.savetxt('cost' + str(i) + '.csv', cost)
            #muster_cost = genfromtxt('cost'+str(i)+'.csv', delimiter=',')
            #print(cost)
            #print(reward)
            #self.assertEqual(cost, muster_cost)