# coding=utf-8
from __future__ import print_function
from __future__ import division
import numpy as np
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

class EffectiveAction(object):

    def __init__(self, velocity,gain, setpoint):
        self.setpoint = setpoint
        self.effectiveVelocity = self.calcEffectiveVelocity(velocity, gain, setpoint)
        self.effectiveGain = self.calcEffectiveGain(gain, setpoint)

    def calcEffectiveVelocity(self, a, b, setpoint):
        minAlphaUnscaled = self.calcEffectiveVelocityUnscaled(self.calcEffectiveA(100, setpoint), self.calcEffectiveB(0, setpoint))
        maxAlphaUnscaled = self.calcEffectiveVelocityUnscaled(self.calcEffectiveA(0, setpoint), self.calcEffectiveB(100, setpoint))
        alphaUnscaled = self.calcEffectiveVelocityUnscaled(self.calcEffectiveA(a, setpoint), self.calcEffectiveB(b, setpoint))
        return (alphaUnscaled - minAlphaUnscaled) / (maxAlphaUnscaled - minAlphaUnscaled)

    def calcEffectiveGain(self, b, setpoint):
        minBetaUnscaled = self.calcEffectiveGainUnscaled(self.calcEffectiveB(100, setpoint))
        maxBetaUnscaled = self.calcEffectiveGainUnscaled(self.calcEffectiveB(0, setpoint))
        betaUnscaled = self.calcEffectiveGainUnscaled(self.calcEffectiveB(b, setpoint))
        return (betaUnscaled - minBetaUnscaled) / (maxBetaUnscaled - minBetaUnscaled)

    def calcEffectiveA(self, a, setpoint):
        return a + 101. - setpoint

    def calcEffectiveB(self, b, setpoint):
        return b + 1. + setpoint

    def calcEffectiveVelocityUnscaled(self, effectiveA, effectiveB):
        return (effectiveB + 1.0) / effectiveA

    def calcEffectiveGainUnscaled(self, effectiveB):
        return 1.0 / effectiveB

    def getEffectiveVelocity(self):
        return self.effectiveVelocity

    def getEffectiveGain(self):
        return self.effectiveGain