"""
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
"""

from __future__ import annotations


class EffectiveAction:

    def __init__(self, velocity: float, gain: float, setpoint: float) -> None:
        self.setpoint = setpoint
        self.effective_velocity = self._calc_effective_velocity(velocity, gain, setpoint)
        self.effective_gain = self._calc_effective_gain(gain, setpoint)

    def _calc_effective_velocity(self, a: float, b: float, setpoint: float) -> float:
        min_unscaled = self._calc_velocity_unscaled(self._effective_a(100, setpoint), self._effective_b(0, setpoint))
        max_unscaled = self._calc_velocity_unscaled(self._effective_a(0, setpoint), self._effective_b(100, setpoint))
        unscaled = self._calc_velocity_unscaled(self._effective_a(a, setpoint), self._effective_b(b, setpoint))
        return (unscaled - min_unscaled) / (max_unscaled - min_unscaled)

    def _calc_effective_gain(self, b: float, setpoint: float) -> float:
        min_unscaled = self._calc_gain_unscaled(self._effective_b(100, setpoint))
        max_unscaled = self._calc_gain_unscaled(self._effective_b(0, setpoint))
        unscaled = self._calc_gain_unscaled(self._effective_b(b, setpoint))
        return (unscaled - min_unscaled) / (max_unscaled - min_unscaled)

    @staticmethod
    def _effective_a(a: float, setpoint: float) -> float:
        return a + 101.0 - setpoint

    @staticmethod
    def _effective_b(b: float, setpoint: float) -> float:
        return b + 1.0 + setpoint

    @staticmethod
    def _calc_velocity_unscaled(effective_a: float, effective_b: float) -> float:
        return (effective_b + 1.0) / effective_a

    @staticmethod
    def _calc_gain_unscaled(effective_b: float) -> float:
        return 1.0 / effective_b

    def getEffectiveVelocity(self) -> float:
        return self.effective_velocity

    def getEffectiveGain(self) -> float:
        return self.effective_gain
