/*
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
package com.siemens.industrialbenchmark.datavector.state;

import java.util.ArrayList;
import java.util.List;

import com.siemens.industrialbenchmark.datavector.DataVectorDescriptionImpl;

/**
 * Markovian state description for the industrial benchmark.
 *
 * @author Alexander Hentschel, Michel Tokic
 */
public class MarkovianStateDescription extends DataVectorDescriptionImpl {

	public static final String CURRENT_OPERATIONAL_COST = "CurrentOperationalCost";
	public static final String FATIGUE_LATENT_2 = "FatigueLatent2";
	public static final String FATIGUE_LATENT_1 = "FatigueLatent1";

	// gold stone
	public static final String EFFECTIVE_SHIFT = "EffectiveShift";
	public static final String MIS_CALIBRATION_DOMAIN = "MisCalibrationDomain";
	public static final String MIS_CALIBRATION_SYSTEM_RESPONSE = "MisCalibrationSystemResponse";
	public static final String MIS_CALIBRATION_PHI_IDX = "MisCalibrationPhiIdx";

	public static final String EFFECTIVE_ACTION_GAIN_BETA = "EffectiveActionGainBeta";
	public static final String EFFECTIVE_ACTION_VELOCITY_ALPHA = "EffectiveActionVelocityAlpha";

	public static final String REWARD_FATIGUE = "RewardFatigue";
	public static final String REWARD_FATIGUE_WEIGHTED = "RewardFatigueWeighted";
	public static final String REWARD_CONSUMPTION = "RewardConsumption";
	public static final String REWARD_CONSUMPTION_WEIGHTED = "RewardConsumptionWeighted";
	public static final String MIS_CALIBRATION = "MisCalibration";
	public static final String RANDOM_SEED = "RandomSeed";

	/* observables */
	public static final String SET_POINT = "SetPoint";
	public static final String ACTION_VELOCITY = "Velocity";
	public static final String ACTION_GAIN = "Gain";
	public static final String ACTION_SHIFT = "Shift";
	public static final String FATIGUE = "Fatigue";
	public static final String FATIGUE_BASE = "FatigueBase"; // without bifurcation aspects
	public static final String OPERATIONAL_COSTS_CONV = "OperationalCostsConv";
	public static final String CONSUMPTION = "Consumption";
	public static final String REWARD_TOTAL = "RewardTotal";

	private static final List<String> STATE_VARS = new ArrayList<String>();

	static {
		// hidden state variables
		STATE_VARS.add(CURRENT_OPERATIONAL_COST);
		STATE_VARS.add(FATIGUE_LATENT_2);
		STATE_VARS.add(FATIGUE_LATENT_1);
		STATE_VARS.add(EFFECTIVE_SHIFT);
		STATE_VARS.add(MIS_CALIBRATION_DOMAIN);
		STATE_VARS.add(MIS_CALIBRATION_SYSTEM_RESPONSE);
		STATE_VARS.add(MIS_CALIBRATION_PHI_IDX);
		STATE_VARS.add(EFFECTIVE_ACTION_GAIN_BETA);
		STATE_VARS.add(EFFECTIVE_ACTION_VELOCITY_ALPHA);
		STATE_VARS.add(REWARD_CONSUMPTION);
		STATE_VARS.add(REWARD_FATIGUE);
		STATE_VARS.add(MIS_CALIBRATION);
		STATE_VARS.add(REWARD_CONSUMPTION_WEIGHTED);
		STATE_VARS.add(REWARD_FATIGUE_WEIGHTED);
		STATE_VARS.add(RANDOM_SEED);
		STATE_VARS.add(CONSUMPTION);

		// observables:
		STATE_VARS.add(SET_POINT);
		STATE_VARS.add(ACTION_VELOCITY);
		STATE_VARS.add(ACTION_GAIN);
		STATE_VARS.add(ACTION_SHIFT);
		STATE_VARS.add(FATIGUE);
		STATE_VARS.add(FATIGUE_BASE);
		STATE_VARS.add(OPERATIONAL_COSTS_CONV);
		STATE_VARS.add(REWARD_TOTAL);
	}

	/**
	 * Constructs a new description.
	 * @param names state/action description names
	 */
	public MarkovianStateDescription(final List<String> names) {
		super(names);
		//System.out.println("MarkovianState names: " + names.toString());
	}

	/**
	 * @return the list of non-convoluted internal variables
	 */
	public static List<String> getNonConvolutedInternalVariables() {
		return STATE_VARS;
	}
}

