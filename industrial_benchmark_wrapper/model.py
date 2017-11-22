import numpy as np


class model():

    def __init__(self):
        # TODO
        model = "TODO"
        #model.add(Flatten(input_shape=(1,) + env.observation_space.shape))
        #model.add(Dense(124))
        #model.add(Activation('relu'))
        #model.add(Dense(56))
        #model.add(Activation('relu'))
        #model.add(Dense(nb_actions))
        #model.add(Activation('linear'))




    def get_action(self):
        # TODO
        action = 2 * np.random.rand(3) - 1
        return action