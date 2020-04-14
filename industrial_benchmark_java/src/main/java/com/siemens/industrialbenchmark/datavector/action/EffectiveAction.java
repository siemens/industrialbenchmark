/**
Copyright 2016 Siemens AG.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.siemens.industrialbenchmark.datavector.action;

public class EffectiveAction {
	
	ActionAbsolute action;

	final double  effectiveVelocity;
	final double  effectiveGain; 
	final double  setpoint;
	
	public EffectiveAction(ActionAbsolute action, double  setpoint) {
		this.action = action;
		this.setpoint = setpoint; 
		
		effectiveVelocity = calcEffectiveVelocity(action.getVelocity(), action.getGain(), setpoint);
		effectiveGain = calcEffectiveGain(action.getGain(), setpoint); 
	}
	
	private double  calcEffectiveVelocity(double  a, double  b, double  setpoint) {
		final double  minAlphaUnscaled = calcAlphaUnscaled(calcEffectiveA(100, setpoint), calcEffectiveB(0,   setpoint));
		final double  maxAlphaUnscaled = calcAlphaUnscaled(calcEffectiveA(0,   setpoint), calcEffectiveB(100, setpoint));
		final double  alphaUnscaled    = calcAlphaUnscaled(calcEffectiveA(a,   setpoint), calcEffectiveB(b,   setpoint)); 
		
		return (alphaUnscaled - minAlphaUnscaled) / (maxAlphaUnscaled - minAlphaUnscaled);
	}
	
	private double  calcEffectiveGain(double  b, double  setpoint) {
		final double  minBetaUnscaled = calcBetaUnscaled(calcEffectiveB(100, setpoint));
		final double  maxBetaUnscaled = calcBetaUnscaled(calcEffectiveB(0,   setpoint));
		final double  betaUnscaled    = calcBetaUnscaled(calcEffectiveB(b,   setpoint));
		
		return (betaUnscaled - minBetaUnscaled) / (maxBetaUnscaled - minBetaUnscaled); 
	}
	
	private double  calcEffectiveA (double  a, double  setpoint) {
		return a + 101.f - setpoint;
	}
	
	private double  calcEffectiveB (double  b, double  setpoint) {
		return b + 1.f + setpoint;
	}
	
	private double  calcAlphaUnscaled (double  effectiveA, double  effectiveB) {
		return (effectiveB + 1.0f) / effectiveA;
	}
	
	private double  calcBetaUnscaled (double  effectiveB) {
		return 1.0f / effectiveB;
	}
	
	public double  getEffectiveVelocity () {
		return this.effectiveVelocity;
	}
	
	public double  getEffectiveGain() {
		return this.effectiveGain;
	}
}
