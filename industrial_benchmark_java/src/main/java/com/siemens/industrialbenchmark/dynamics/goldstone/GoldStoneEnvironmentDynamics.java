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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class GoldStoneEnvironmentDynamics {

	private final int strongestPenaltyAbsIdx;
	private Domain domain = Domain.POSITIVE;
	private SystemResponse systemResponse = SystemResponse.ADVANTAGEOUS;
	private PenaltyFunction currentPenaltyFunction = null;
	private int phiIdx = 0;
	private final double safeZone;
	private final PenaltyFunction[] penaltyFunctionsArray;
	final static Logger LOGGER = LoggerFactory.getLogger(GoldStoneEnvironmentDynamics.class);
	
	public enum Domain {
		POSITIVE(+1),
		NEGATIVE(-1);

		private final int id;
		Domain(int id) { this.id = id; }
		public int getValue() { return id; }
		public static Domain fromDouble (double id) {
			if (id < 0.0) return  NEGATIVE;
			if (id >= 0.0) return POSITIVE;
			throw new IllegalArgumentException("id must be either [-1, 1], but is " + id);
		}
	}
	
	public enum SystemResponse {
		ADVANTAGEOUS(+1),
		DISADVANTAGEOUS(-1);

		private final int id;
		SystemResponse(int id) { this.id = id; }
		public int getValue() { return id; }
		public static SystemResponse fromDouble (double id) {
			if (id < 0.0) return  DISADVANTAGEOUS;
			if (id >= 0.0) return ADVANTAGEOUS;
			throw new IllegalArgumentException("id must be either [-1, 1], but is " + id);
		}
	}

	public GoldStoneEnvironmentDynamics(int numberSteps, double maxRequiredStep, double safeZone) {
		Preconditions.checkArgument(safeZone >= 0, "safeZone must be non-negative, but is %s.", safeZone);

		this.safeZone = safeZone;
		this.strongestPenaltyAbsIdx = computeStrongestPenaltyAbsIdx(numberSteps);
		this.penaltyFunctionsArray = this.defineRewardFunctions(numberSteps, maxRequiredStep);
		this.reset();
	}

	public void reset() {
		this.domain = Domain.POSITIVE;
		systemResponse = SystemResponse.ADVANTAGEOUS;
	}

	public double rewardAt (double pos) {
		return -currentPenaltyFunction.reward(pos);
	}

	public double optimalPosition() {
		return currentPenaltyFunction.getOptimumRadius();
	}

	public double optimalReward() {
		return -currentPenaltyFunction.getOptimumValue();
	}

	public void stateTransition(double newControlValue) {

		Domain oldDomain = this.domain;

		// (0) compute new domain
		this.domain = this.computeDomain(newControlValue);
		
		// (1) if domain change: system response <- advantageous
		if (this.domain != oldDomain) {
			this.systemResponse = SystemResponse.ADVANTAGEOUS;
			LOGGER.trace("  turning sys behavior -> advantageous");
		}
		
		int oldPhiIdx = this.phiIdx;
		
		// (2) compute & apply turn direction
		this.phiIdx += computeAngularStep(newControlValue);
		
		// (3) update system response if necessary
		this.systemResponse = updateSystemResponse (this.phiIdx, newControlValue);
		
		oldPhiIdx = this.phiIdx;
		
		// (4) apply symmetry
		this.phiIdx = this.applySymmetry(this.phiIdx);
		
		oldDomain = this.domain;

		// (5) if Phi_index == 0: reset internal state
		if (this.phiIdx == 0 && Math.abs(newControlValue) <= this.safeZone) {
			this.reset();
		}
		
		LOGGER.trace ("  phiIdx = " + phiIdx);
		this.currentPenaltyFunction = this.getPenaltyFunction();
	}

	/**
	 * Compute the new domain of control action. 
	 * @param newPosition The new position.
	 * @return The numerical value of the new domain.
	 */
	private Domain computeDomain(double newPosition) {
		if (Math.abs(newPosition) <= this.safeZone) {
			return this.domain;
		} else {			
			return Domain.fromDouble(Math.signum(newPosition));
		}
	}


	private double computeAngularStep(double newPosition) {
		// cool down: when position close to zero
		if (Math.abs(newPosition) <= this.safeZone) {
			return -Math.signum(this.phiIdx);
		}

		if (this.phiIdx == (-this.domain.getValue() * strongestPenaltyAbsIdx)) {
			LOGGER.trace ("  no turning");
			return 0;
		}

		return this.systemResponse.getValue() * Math.signum(newPosition);
	}

	/**
	 * Update system response
	 * @param phiIdx
	 * @param newControlValue
	 * @return
	 */
	private SystemResponse updateSystemResponse(int newPhiIdx, double newControlValue) {
		if (Math.abs(newPhiIdx) >= strongestPenaltyAbsIdx) {
			LOGGER.trace ("  turning sys behavior -> disadvantageous");
			return SystemResponse.DISADVANTAGEOUS;
		} else {
			return this.systemResponse;
		}
	}


	/**
	 * Apply symmetric properties on phiIdx
	 * @param phiIdx
	 * @return
	 */
	private int applySymmetry(int phiIdx) {
		if (Math.abs(phiIdx) < strongestPenaltyAbsIdx) {
			return phiIdx;
		}

		phiIdx = (phiIdx+(4*strongestPenaltyAbsIdx)) % (4*strongestPenaltyAbsIdx);
		phiIdx = 2*strongestPenaltyAbsIdx - phiIdx;
		return phiIdx;
	}

	public PenaltyFunction getPenaltyFunction() {
		return getPenaltyFunction(this.phiIdx);
	}

	public PenaltyFunction getPenaltyFunction(int phiIdx) {
		int idx = strongestPenaltyAbsIdx + phiIdx;
		if (idx < 0) {
			idx += penaltyFunctionsArray.length;
		}
		return penaltyFunctionsArray[idx];
	}


	/**
	 * Define the reward functions
	 * @param numberSteps
	 * @param maxRequiredStep
	 */
	private PenaltyFunction[] defineRewardFunctions(int numberSteps, double maxRequiredStep) {

		final int k = strongestPenaltyAbsIdx;
		double angle_gid[] = new double[k*2+1];
		for (int i=-k; i<=k; i++) {
			angle_gid[i+k] = i * 2*Math.PI / numberSteps;
		}
		PenaltyFunction[] penaltyFunctionsArray = new PenaltyFunction[angle_gid.length];
		for (int i=0; i<angle_gid.length; i++) {
			penaltyFunctionsArray[i] = new PenaltyFunction(angle_gid[i], maxRequiredStep);
		}

		return penaltyFunctionsArray;
	}

	private int computeStrongestPenaltyAbsIdx (int numberSteps) {
		Preconditions.checkArgument(numberSteps >= 1 && (numberSteps %4) == 0, 
				"numberSteps must be positive and an integer multiple of 4, but is %s", numberSteps);

		final int k = numberSteps / 4;
		return k;				
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public SystemResponse getSystemResponse() {
		return systemResponse;
	}

	public void setSystemResponse(SystemResponse systemResponse) {
		this.systemResponse = systemResponse;
	}

	public int getPhiIdx() {
		return phiIdx;
	}

	public void setPhiIdx(int phiIdx) {
		this.phiIdx = phiIdx;
	}
}

