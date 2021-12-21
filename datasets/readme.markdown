# The Industrial Benchmark Offline RL Datasets

This folder contains the datasets generated with the industrial benchmark that are in multiple publications used for offline Reinforcement Learning. The datasets are collected under three different baseline policys - bad, mediocre, and optimized. See the referenced papers below for details about the baselines. The baselines are then mixed with varying levels of uniform random exploration - variants with 0%, 20%, 40%, 60%, and 80% exist for each baseline. Additionally, a dataset with 100% random actions is provided.
The datasets are pickled python lists, containing 100k tuples of length 5, where each tuple consists of (state, action, reward, done, next_state). Note that the rewards have been scaled (divided by 100) to keep them in a nicer range.

Example to open a dataset:

    >> import pickle
    >> datafile = open("bad/bad_0.2_100x1000.pickle", "rb")
    >> tuples = pickle.load(datafile)
    >> print(len(tuples), len(tuples[0]))
    100000 5

See the papers in which the datasets were introduced for additional details on the datasets, their baselines and experimental results of various algorithms on them:

* [<a href="https://arxiv.org/abs/2008.05533">MOOSE</a>] Swazinna, P., Udluft, S., and Runkler, T. (2021b). Overcoming model bias for robust offline deep reinforcement learning. Engineering Applications of Artificial Intelligence, 104,104366.
