# coding=utf-8
from __future__ import division
'''
The MIT License (MIT)

Copyright 2017 Siemens AG

Author: Alexander Hentschel

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

from dynamics import dynamics

class environment:
    def __init__(self, number_steps, max_required_step, safe_zone):
        self._dynamics = dynamics(number_steps, max_required_step, safe_zone)
        self.reset_position_zero()

    def reset_position_zero(self):
        self._control_position = self.reset_position(0)

    def reset_position(self, control_start_value):
        self._control_position = control_start_value

    def reward(self):
        return self._dynamics.reward(self._control_position)

    def state_transition(self, control_value):
        self._control_position = control_value
        self._dynamics.state_transition(self._control_position)
        return self.reward()