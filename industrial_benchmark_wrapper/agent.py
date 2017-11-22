from interpreter import  interpreter
import numpy as np
from model import model

class agent():

    def __init__(self, size):
        print('Create Agent')
        self.size = size
        setpoint = 50
        reward_type = 'classic'
        action_type = "discrete"
        self.agent = interpreter(setpoint, reward_type, action_type)
        self.model = model()
        self.observation = self.agent.step(2 * np.random.rand(3) - 1)
        self.buffer_states = (self.observation)

        # Buffer inizialisieren mit 30 states und random action
        for buffer in range(3):
            action = 2 * np.random.rand(3) - 1
            self.observation = self.agent.step(action)
            self.buffer_states = np.expand_dims(self.buffer_states, self.observation, axis = 3)
            #self.buffer_states = np.vstack([self.buffer_states, self.observation])


        print(self.buffer_states)
        #print(self.buffer_states[1,:])
        self.state = np.array(self.buffer_states)

        self.step()

    def step(self):
        # get action
        action = self.model.get_action()

        # perform action and observe state
        self.observation = self.agent.step(action)
        #print (self.observation)
        # save new state








