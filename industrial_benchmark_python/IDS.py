# coding=utf-8
from __future__ import division
'''
The MIT License (MIT)

Copyright 2017 Siemens AG

Authors: Stefan Depeweg

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
import numpy as np
from goldstone.environment import environment as GoldstoneEnvironment
from EffectiveAction import EffectiveAction
from collections import OrderedDict

class IDS(object):
    '''
    Lightweight python implementation of the industrial benchmark
    Uses the same standard settings as in src/main/ressources/simTest.properties 
    of java implementation
    '''

    def __init__(self,p=50,stationary_p=True, inital_seed=None):

        '''
        p sets the setpoint hyperparameter (between 1-100) which will
        affect the dynamics and stochasticity.

        stationary_p = False will make the setpoint vary over time. This
        will make the system more non-stationary.
        '''

        # fix seed only for testing
        #if inital_seed != None:
        np.random.seed(inital_seed)


        self.maxRequiredStep = np.sin(15./180.*np.pi);
        self.gsBound = 1.5
        self.gsSetPointDependency = 0.02
        self.gsScale = 2.*self.gsBound + 100.*self.gsSetPointDependency # scaling factor for shift

        self.CRD = 3. # Reward Fatigue weighted
        self.CRE = 1 # DONE im Paper -1 # Reward Consumtion weighted
        self.CRGS =  25.

        self.dv = 1 # scaling factor for velocity
        self.dg = 10 # scaling factor for gain
        self.dh = (self.maxRequiredStep/0.9)*100./self.gsScale # scaling factor for shift

        self.stationary_p = stationary_p

        self.gsEnvironment = GoldstoneEnvironment(24, self.maxRequiredStep, self.maxRequiredStep/2.0);

        self.state = OrderedDict()

        # gold stone
        self.state['o'] = np.zeros(10) #  operational cost buffer
        self.state['coc'] = 0 # current operational cost
        self.state['hg'] = 0. # hidden gain
        self.state['hv'] = 0. # hidden velocity
        self.state['he'] = 0. # hidden/ effective shift
        self.state['gs_domain'] = self.gsEnvironment._dynamics._domain.value # miscalibration domain
        self.state['gs_sys_response'] = self.gsEnvironment._dynamics._system_response.value # miscalibration System Response
        self.state['gs_phi_idx'] = self.gsEnvironment._dynamics._Phi_idx # miscalibration Phi_idx/ direction
        self.state['ge'] = 0. # effective action gain beta
        self.state['ve'] = 0. # effective action velocity alpha
        self.state['MC'] = 0. # misCalibration

        # observables
        self.state['c'] = 0. # consumption
        self.state['p'] = p  # SetPoint
        self.state['v'] = 50. # Velocity
        self.state['g'] = 50.  # Gain
        self.state['h'] = 50.  # Shift
        self.state['f'] = 0.  # fatigue
        self.state['fb'] = 0.  # basic fatigue: without bifurcation aspects
        self.state['oc'] = 0 # current operational cost conv
        self.state['cost'] = 0. #  signal/ total
        self.state['reward'] = 0. # reward

        self.init = True
        self.defineNewSequence()
        self.step(np.zeros(3))

    def visibleState(self):
        return np.array([self.state['p'],self.state['v'],self.state['g'],self.state['h'],self.state['f'],self.state['c'], self.state['cost'], self.state['reward']])

    def markovState(self):
        return np.hstack((self.state['o'], np.array([self.state[k] for k in self.state.keys()])))
        #return np.hstack((self.state['o'],np.array([self.state[k] for k in self.state.keys()[1:]])))

#---------------FOR TESTING---------------------------------------------------
    #this 2 methods are only here for testing
    def operational_cost_Buffer(self):
        return np.array(self.state['o'])

    def allStates(self):
        #return np.array([self.state[k] for k in self.state.keys()])
        return np.array([self.state['p'],self.state['v'],self.state['g'],self.state['h'],self.state['f'],self.state['c'],self.state['reward'], self.state['coc'], self.state['he'], self.state['ge'], self.state['ve'], self.state['MC'], self.state['fb'], self.state['oc'], self.state['gs_domain'], self.state['gs_sys_response'], self.state['gs_phi_idx'] ])

# -----------------------------------------------------------------------------

    def step(self,delta):
        self.updateSetPoint()
        self.addAction(delta)
        self.updateFatigue()
        self.updateCurrentOperationalCost()
        self.updateOperationalCostConvolution()
        self.updateGS()
        self.updateOperationalCosts()
        self.cost() # Update Reward
        return self.markovState()

    def updateSetPoint(self):
        if self.stationary_p == True:
            return
        else:
            if self._p_step == self._p_steps:
                self.defineNewSequence()

            new_p = self.state['p'] + self._p_ch
            if new_p > 100 or new_p < 0:

                if np.random.rand() > 0.5:
                    self._p_ch *= -1

            new_p = np.clip(new_p, 0, 100)

            self.state['p'] = new_p

            self._p_step += 1

    def addAction(self,delta):
        delta = np.clip(delta,-1,1)
        self.state['v'] = np.clip(self.state['v'] + self.dv*delta[0],0.,100.)
        self.state['g'] = np.clip(self.state['g'] + self.dg*delta[1],0.,100.)
        self.state['h'] = np.clip(self.state['h'] + self.dh*delta[2],0.,100.)
        self.state['he'] = np.clip(self.gsScale*self.state['h']/100. - self.gsSetPointDependency*self.state['p'] - self.gsBound,-self.gsBound,self.gsBound)


    def updateFatigue(self):
        expLambda = 0.1 # => scale = 1/lambda
        actionTolerance = 0.05
        fatigueAmplification = 1.1    
        fatigueAmplificationMax = 5.0
        fatigueAmplificationStart = 1.2 

        dyn = 0.0
        velocity = self.state['v']
        gain = self.state['g']
        setpoint = self.state['p']

        hidden_gain = self.state['hg']
        hidden_velocity = self.state['hv']

        effAct =  EffectiveAction(velocity,gain,setpoint)
        effAct_velocity_alpha = effAct.getVelocityAlpha()
        effAct_gain_beta = effAct.getGainBeta()

        self.state['ge'] = effAct_gain_beta
        self.state['ve'] = effAct_velocity_alpha

        noise_e_g = np.random.exponential(expLambda);
        noise_e_v = np.random.exponential(expLambda);
        noise_u_g = np.random.rand();
        noise_u_v = np.random.rand();
        
        noise_b_g = np.float(np.random.binomial(1, np.clip(effAct_gain_beta,0.001, 0.999)))
        noise_b_v = np.float(np.random.binomial(1, np.clip(effAct_velocity_alpha,0.001, 0.999)))
        

        noise_gain = 2.0 * (1.0/(1.0+np.exp(-noise_e_g)) - 0.5);
        noise_velocity = 2.0 * (1.0/(1.0+np.exp(-noise_e_v)) - 0.5);

        noise_gain += (1-noise_gain) * noise_u_g * noise_b_g * effAct_gain_beta;
        noise_velocity += (1-noise_velocity) * noise_u_v * noise_b_v * effAct_velocity_alpha;


        if hidden_gain  >= fatigueAmplificationStart:
            hidden_gain = np.minimum(fatigueAmplificationMax,fatigueAmplification*hidden_gain)
        elif effAct_gain_beta > actionTolerance:
            hidden_gain = 0.9*hidden_gain + noise_gain/3.

        if hidden_velocity  >= fatigueAmplificationStart:
            hidden_velocity = np.minimum(fatigueAmplificationMax,fatigueAmplification*hidden_velocity)
        elif effAct_velocity_alpha > actionTolerance:
            hidden_velocity = 0.9*hidden_velocity + noise_velocity/3.


        if effAct_velocity_alpha <= actionTolerance:
            hidden_velocity = effAct_velocity_alpha

        if effAct_gain_beta <= actionTolerance:
            hidden_gain = effAct_gain_beta


        if np.maximum(hidden_velocity,hidden_gain) == fatigueAmplificationMax:
            alpha = 1.0 / (1.0 + np.exp(-4.0*np.random.normal(0.6,0.1)))
        else:
            alpha = np.maximum(noise_velocity,noise_gain)

        fb = np.maximum(0,((30000. / ((5*velocity) + 100)) - 0.01 * (gain**2)))
        self.state['hv'] = hidden_velocity
        self.state['hg'] = hidden_gain
        self.state['f'] = (fb*(1+2*alpha)) / 3.
        self.state['fb'] = fb

    def updateCurrentOperationalCost(self):
        CostSetPoint = 2.
        CostVelocity = 4.
        CostGain = 2.5

        gain = self.state['g']
        velocity = self.state['v']
        setpoint = self.state['p']

        costs = CostSetPoint * setpoint + CostGain * gain + CostVelocity * velocity;
        o = np.exp(costs / 100.)
        self.state['coc'] = o

        if self.init == True:
            self.state['o'] += o
            self.init = False
        else:
            self.state['o'][:-1] = self.state['o'][1:]
            self.state['o'][-1] = o



    def updateOperationalCostConvolution(self):
        ConvArray=np.array([0.11111,0.22222,0.33333,0.22222,0.11111,0.,0.,0.,0.,0.])
        self.state['oc'] = np.dot(self.state['o'],ConvArray)


    def updateGS(self):
        setpoint = self.state['p']
        shift = self.state['h']
        effective_shift = self.state['he']

        reward = -self.gsEnvironment.state_transition(effective_shift)
        self.state['MC'] = reward
        self.state['gs_domain'] = self.gsEnvironment._dynamics._domain.value 
        self.state['gs_sys_response'] = self.gsEnvironment._dynamics._system_response.value
        self.state['gs_phi_idx'] = self.gsEnvironment._dynamics._Phi_idx


    def updateOperationalCosts(self):
        rGS = self.state['MC']
        eNewHidden = self.state['oc'] - (self.CRGS * (rGS - 1.0))
        operationalcosts = eNewHidden - np.random.randn()*(1+0.005*eNewHidden)
        self.state['c'] = operationalcosts


    # DONE: cost = reward und negativ
    def cost(self):
        # Dynmaics
        rD = -(self.state['f'])
        fatigue = self.state['f']
        # Consumtion
        rE = - (self.state['c'])
        consumption = self.state['c']
        self.state['cost'] = self.CRD*fatigue + self.CRE*consumption # DONE: MINUS --> cost in reward umwandeln mit rD und rE ?????
        self.state['reward'] = self.CRD*rD + self.CRE*rE



    def defineNewSequence(self):

        length = np.random.randint(1,100)
        self._p_steps = length
        self._p_step = 0
        p_ch = 2 * np.random.rand() -1
        if np.random.rand() < 0.1:
            p_ch *= 0.
        self._p_ch =  p_ch

