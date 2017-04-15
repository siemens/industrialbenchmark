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
	private Domain domain = Domain.INITIAL;
	private SystemResponse systemResponse = SystemResponse.ADVANTAGEOUS;
	private PenaltyFunction currentPenaltyFunction = null;
	private int phiIdx = 0;
	private final double safeZone;
	private final PenaltyFunction[] penaltyFunctionsArray;
	final static Logger LOGGER = LoggerFactory.getLogger(GoldStoneEnvironmentDynamics.class);

	public enum Domain {
		POSITIVE(+1),
		INITIAL(0),
		NEGATIVE(-1);

		private final int id;
		Domain(int id) { this.id = id; }
		public int getValue() { return id; }
		public static Domain fromDouble (double id) {
			if (id == -1.0) return  NEGATIVE;
			if (id == 0.0) return INITIAL;
			if (id == 1.0) return POSITIVE;
			throw new IllegalArgumentException("id must be either [-1, 0, 1], but is " + id);
		}
	}

	public enum SystemResponse {
		ADVANTAGEOUS(+1),
		DISADVANTAGEOUS(-1),
		NEUTRAL(0);

		private final int id;
		SystemResponse(int id) { this.id = id; }
		public int getValue() { return id; }
		public static SystemResponse fromDouble (double id) {
			if (id == -1.0) return  DISADVANTAGEOUS;
			if (id == 0.0) return NEUTRAL;
			if (id == 1.0) return ADVANTAGEOUS;
			throw new IllegalArgumentException("id must be either [-1, 0, 1], but is " + id);
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
		this.domain = Domain.INITIAL;
		phiIdx = 0;
		systemResponse = SystemResponse.ADVANTAGEOUS;
		currentPenaltyFunction = getPenaltyFunction();
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

		final Domain oldDomain = this.domain;

		// (0) compute new domain
		this.domain = this.computeDomain(newControlValue);

		// (1) if domain change: system response <- advantageous
		if (this.domain != oldDomain) {
			this.systemResponse = SystemResponse.ADVANTAGEOUS;
			LOGGER.trace("  turning sys behavior -> advantageous");
		}

		// (2) compute & apply turn direction
		this.phiIdx += computeAngularStep(newControlValue);

		// (3) update system response if necessary
		this.systemResponse = updateSystemResponse (this.phiIdx, newControlValue);

		// (4) if Phi_index == 0: reset internal state
		if (this.phiIdx == 0 && Math.abs(newControlValue) <= this.safeZone) {
			this.reset();
		}

		// (5) apply symmetry
		this.phiIdx = this.applySymmetry(this.phiIdx);

		LOGGER.trace ("  phiIdx = " + phiIdx);
		this.currentPenaltyFunction = this.getPenaltyFunction();
	}

	/**
	 *
     * Compute the new domain of control action.
     * Note:
     * if control action is in safe zone, domain remains unchanged
     * as 'penalty landscape' turn direction is independent of exact position
     * in safe zone, reset to Domain.initial can be applied later.
     *
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
	 * only changes system response to if turn angle hits 90deg
     * in domain of current position, i.e.:
     * new_position > self.__safe_zone and new_Phi_idx = 90deg
     * new_position < -self.__safe_zone and new_Phi_idx = -90deg
     *
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
	 *
     * By employing reflection symmetry with respect to x-axis:
     *    o Phi -> pi -Phi
     *    o turn direction -> turn direction
     *    o x -> x
     *  moves 'penalty landscape' rotation angle in domain [-90deg ... +90deg]
     *  corresponding to Phi_index in
     *      [ -strongest_penality_abs_idx*angular_speed, ..., -angular_speed,
     *         0,
     *         angular_speed, ..., strongest_penality_abs_idx*angular_speed ]

	 * @param phiIdx
	 * @return
	 */
	private int applySymmetry(int phiIdx) {

		/*
		 * Do nothing if 'penalty landscape' rotation angle is in [-90deg ... +90deg]
         * corresponding to angle indices
         *  [-self.__strongest_penality_abs_idx, ...-1,0,1, ..., self.__strongest_penality_abs_idx-]
		 */
        if (Math.abs(phiIdx) <= strongestPenaltyAbsIdx) {
        	return phiIdx;
        }

        /*
         *  Otherwise:
         * Use 2pi symmetry to move angle index p in domain
         *   [0 ... 360deg)
         * corresponding to angle indices
         *   [0, ..., 4*self.__strongest_penality_abs_idx-1]
         * But we are only executing the following code, if the angle is in
         *   (90deg, ..., 270deg)
         * corresponding to angle indices
         *   [self.__strongest_penality_abs_idx+1, ..., 3*self.__strongest_penality_abs_idx-1]
         * Therefore, the reflection-symmetry operation
         *    p <- 2*self.__strongest_penality_abs_idx - p
         * will transform p back into the desired angle indices domain
         *    [-self.__strongest_penality_abs_idx, ...-1,0,1, ..., self.__strongest_penality_abs_idx-]
         * */
        phiIdx = phiIdx % (4*strongestPenaltyAbsIdx);
        if (phiIdx < 0) {
        	phiIdx += (4*strongestPenaltyAbsIdx);
        }
        phiIdx = 2*strongestPenaltyAbsIdx - phiIdx;

        return phiIdx;
	}

	public PenaltyFunction getPenaltyFunction() {
		return getPenaltyFunction(this.phiIdx);
	}

	public PenaltyFunction getPenaltyFunction(int phiIdx) {
		int idx = strongestPenaltyAbsIdx + this.applySymmetry(phiIdx);
		if (idx < 0) {
			idx += penaltyFunctionsArray.length;
		}
		return penaltyFunctionsArray[idx];
	}


	/**
	   """
        Parameters
        ----------------
        * Number_steps:
        the number of steps required for one full cycle of the optimal policy.
        For easy numerics, it is required that number_steps is positive and an integer multiple of 4.

        By employing reflection symmetry with respect to x-axis:
         o Phi -> pi -Phi
         o turn direction -> turn direction
         o x -> x
        The required rewards functions can be restricted to turn angles Phi in [-90deg ... +90deg]
        of the 'penalty landscape'. One quarter-segment (e.g [0 ... 90deg]) of the entire
        'penalty landscape' is divided into Number_steps / 4 steps. Note that Number_steps / 4
        is an integer per requirement from above.


        Implementation:
        ----------------
        According to the above explanation, the 'penalty landscape' turns in each state transition
            angular_speed = 360deg / number_steps
        or does not turn at all. Per convention, the 'penalty landscape' positions are confined to a
        homogeneously spaced grid of turn angles
            [ 0, angular_speed, 2*angular_speed, ... , (number_steps -1)*angular_speed ]
        Lets define
            strongest_penality_abs_idx = number_steps / 4
        Hence,
            strongest_penality_abs_idx*angular_speed = (number_steps / 4) * (360deg / number_steps)= 90deg
        It follows that
           o Phi = - strongest_penality_abs_idx*angular_speed  maximized the control penalties
             for the positive domain (i.e. where x > 0)
           o Phi = + strongest_penality_abs_idx*angular_speed  maximized the control penalties
             for the negative domain (i.e. where x < 0)
        Exploiting reflection symmetry the required grid of reward functions can be reduced to
            [ -strongest_penality_abs_idx*angular_speed, ..., -angular_speed,
              0,
              angular_speed, ..., strongest_penality_abs_idx*angular_speed ]
        This has the advantage, that either end of the grid represents the worst case points of the
        """

	 * @param numberSteps
	 * @param maxRequiredStep
	 */
	private PenaltyFunction[] defineRewardFunctions(int numberSteps, double maxRequiredStep) {

		final int k = computeStrongestPenaltyAbsIdx(numberSteps);
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

