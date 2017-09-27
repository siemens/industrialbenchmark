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
package com.siemens.industrialbenchmark.dynamics.goldstone;

import com.siemens.industrialbenchmark.dynamics.goldstone.GoldStoneEnvironmentDynamics.Domain;
import com.siemens.industrialbenchmark.dynamics.goldstone.GoldStoneEnvironmentDynamics.SystemResponse;

public class GoldstoneEnvironment {

	final GoldStoneEnvironmentDynamics dynamics;
	private double controlPosition;
	
	public GoldstoneEnvironment(int numberSteps, double maxRequiredStep, double safeZone) {
		dynamics = new GoldStoneEnvironmentDynamics(numberSteps, maxRequiredStep, safeZone);
		this.reset();
	}

	public void reset() {
		this.reset(0);
	}
	
	public void reset(double controlStartValue) {
		this.controlPosition = controlStartValue;
	}
	
	public double reward() {
		return this.dynamics.rewardAt(controlPosition);
	}
	
	public double optimalPosition() {
		return this.dynamics.optimalPosition();
	}
	
	public double optimalReward() {
		return this.dynamics.optimalReward();
	}
	
	/**
	 * Applies action and returns reward
	 */
	public double stateTransition(double controlValueChange) {
		this.controlPosition += controlValueChange;
		this.dynamics.stateTransition(controlPosition);
		return this.reward();
	}
	
	public PenaltyFunction getRewardFunction() {
		return this.dynamics.getPenaltyFunction();
	}

	public double getControlPosition() {
		return controlPosition;
	}

	public void setControlPosition(double controlPosition) {
		this.controlPosition = controlPosition;
		this.dynamics.stateTransition(controlPosition);
		reward();
	}
	
	public float getDomain(){
		return dynamics.getDomain().getValue();
	}
	
	public void setDomain(double double1){
		dynamics.setDomain(Domain.fromDouble(double1));
	}
	
	public float getSystemResponse(){
		return dynamics.getSystemResponse().getValue();
	}
	
	public void setSystemResponse(double systemResponse){
		dynamics.setSystemResponse(SystemResponse.fromDouble(systemResponse));
	}
	
	public float getPhiIdx(){
		return dynamics.getPhiIdx();
	}
	
	public void setPhiIdx(double phiIdx){
		dynamics.setPhiIdx((int) phiIdx);
	}
}
