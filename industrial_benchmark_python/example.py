# coding=utf-8
from IDS import IDS
import numpy as np
import pylab as plt
'''
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
'''

n_trajectories = 10
T = 1000

data = np.zeros((n_trajectories,T))
data_cost = np.zeros((n_trajectories,T))

for k in range(n_trajectories):
    env = IDS(p=100)
    for t in range(T):
        at = 2 * np.random.rand(3) -1
        markovStates = env.step(at)
        data[k,t] = env.visibleState()[-1]

        all_States = env.allStates()


plt.plot(data.T)
plt.xlabel('T')
plt.ylabel('Reward')
plt.show()