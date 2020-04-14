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
package com.siemens.industrialbenchmark.datavector.state;

import java.util.ArrayList;
import java.util.List;

import com.siemens.industrialbenchmark.datavector.DataVectorDescription;

/** 
 * Markovian state description for the industrial benchmark
 * 
 * @author Alexander Hentschel, Michel Tokic
 */
public class MarkovianStateDescription extends DataVectorDescription {

    public static final String CurrentOperationalCost = "CurrentOperationalCost";
    public static final String FatigueLatent2 = "FatigueLatent2";
    public static final String FatigueLatent1 = "FatigueLatent1";
    
    // gold stone
    public static final String EffectiveShift = "EffectiveShift";
    public static final String MisCalibrationDomain = "MisCalibrationDomain";
    public static final String MisCalibrationSystemResponse = "MisCalibrationSystemResponse";
    public static final String MisCalibrationPhiIdx = "MisCalibrationPhiIdx";

    public static final String EffectiveActionGainBeta = "EffectiveActionGainBeta";
    public static final String EffectiveActionVelocityAlpha = "EffectiveActionVelocityAlpha";
    
    public static final String RewardFatigue="RewardFatigue";
    public static final String RewardFatigueWeighted="RewardFatigueWeighted";
    public static final String RewardConsumption="RewardConsumption";
    public static final String RewardConsumptionWeighted="RewardConsumptionWeighted";
    public static final String MisCalibration ="MisCalibration";
	public static final String RandomSeed = "RandomSeed";
    
    /* observables */
	public static final String SetPoint = "SetPoint"; 
    public static final String Action_Velocity = "Velocity";
    public static final String Action_Gain = "Gain";
    public static final String Action_Shift = "Shift";
    public static final String Fatigue = "Fatigue";
    public static final String FatigueBase = "FatigueBase"; // without bifurcation aspects
    public static final String OperationalCostsConv = "OperationalCostsConv";
    public static final String Consumption = "Consumption";
    public static final String RewardTotal = "RewardTotal";


    private static List<String> mStateVars = new ArrayList<String>();

    static{
    	// hidden state variables:
    	mStateVars.add(CurrentOperationalCost);
    	mStateVars.add(FatigueLatent2);
    	mStateVars.add(FatigueLatent1);	
    	mStateVars.add(EffectiveShift);
    	mStateVars.add(MisCalibrationDomain);	
    	mStateVars.add(MisCalibrationSystemResponse);	
    	mStateVars.add(MisCalibrationPhiIdx);	
    	mStateVars.add(EffectiveActionGainBeta);
    	mStateVars.add(EffectiveActionVelocityAlpha);	
    	mStateVars.add(RewardConsumption);
    	mStateVars.add(RewardFatigue);
    	mStateVars.add(MisCalibration); 
    	mStateVars.add(RewardConsumptionWeighted);
    	mStateVars.add(RewardFatigueWeighted);
    	mStateVars.add(RandomSeed);
    	mStateVars.add(Consumption);
    	
    	// observables:
    	mStateVars.add(SetPoint);
    	mStateVars.add(Action_Velocity);
    	mStateVars.add(Action_Gain);
    	mStateVars.add(Action_Shift);
    	mStateVars.add(Fatigue);
    	mStateVars.add(FatigueBase);
    	mStateVars.add(OperationalCostsConv);
    	mStateVars.add(RewardTotal);
    }

    /**
     * constructor with operationalcost_XXX
     * @param names
     */
    public MarkovianStateDescription (List<String> names) {
    	super (names);
    	//System.out.println ("MarkovianState names: " + names.toString());
    }
    
    /**
     * returns the list of non-convoluted internal variables
     * @return the list of non-convoluted internal variables
     * 
     */
    public static List<String> getNonConvolutedInternalVariables() {
    	return mStateVars;
    }
}
