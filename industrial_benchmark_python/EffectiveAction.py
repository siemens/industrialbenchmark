# coding=utf-8
from __future__ import print_function
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
class EffectiveAction(object):

    def __init__(self, velocity,gain, setpoint):
        self.setpoint = setpoint
        self.effectiveA = self.calcEffectiveA(velocity, setpoint)
        self.effectiveB = self.calcEffectiveB(gain, setpoint)
        self.alpha = self.calcAlphaScaled(velocity, gain, setpoint)
        self.beta = self.calcBetaScaled(gain, setpoint)

    def getEffectiveA(self):
        return self.effectiveA

    def getEffectiveB(self):
        return self.effectiveB

    def calcAlphaScaled(self, a, b, setpoint):
        minAlphaUnscaled = self.calcAlphaUnscaled(self.calcEffectiveA(100, setpoint), self.calcEffectiveB(0, setpoint))
        maxAlphaUnscaled = self.calcAlphaUnscaled(self.calcEffectiveA(0, setpoint), self.calcEffectiveB(100, setpoint))
        alphaUnscaled = self.calcAlphaUnscaled(self.calcEffectiveA(a, setpoint), self.calcEffectiveB(b, setpoint))
        return (alphaUnscaled - minAlphaUnscaled) / (maxAlphaUnscaled - minAlphaUnscaled)

    def calcBetaScaled(self, b, setpoint):
        minBetaUnscaled = self.calcBetaUnscaled(self.calcEffectiveB(100, setpoint))
        maxBetaUnscaled = self.calcBetaUnscaled(self.calcEffectiveB(0, setpoint))
        betaUnscaled = self.calcBetaUnscaled(self.calcEffectiveB(b, setpoint))
        return (betaUnscaled - minBetaUnscaled) / (maxBetaUnscaled - minBetaUnscaled)

    def calcEffectiveA(self, a, setpoint):
        return a + 101. - setpoint

    def calcEffectiveB(self, b, setpoint):
        return b + 1. + setpoint

    def calcAlphaUnscaled(self, effectiveA, effectiveB):
        return (effectiveB + 1.0) / effectiveA

    def calcBetaUnscaled(self, effectiveB):
        return 1.0 / effectiveB

    def getVelocityAlpha(self):
        return self.alpha

    def getGainBeta(self):
        return self.beta


