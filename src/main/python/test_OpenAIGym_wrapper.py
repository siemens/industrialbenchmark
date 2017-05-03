#!/usr/bin/env python

import IndustrialBenchmarkEnv
import os

# FIXME System dependent paths:
this_files_dir = os.path.dirname(os.path.realpath(__file__))
# We just assume we are in the siemens/industrialbenchmark sources
project_source_root = os.path.join(this_files_dir, '../../..')

# Start the JVM
# NOTE Make sure you execute `mvn package` (once) before this script
# FIXME System and version dependent path:
ind_bench_jar = os.path.join(project_source_root, 'target/industrialbenchmark-1.1.2-SNAPSHOT-jar-with-dependencies.jar')
IndustrialBenchmarkEnv.start_jvm(None, ind_bench_jar)

# Create the environment
# FIXME System dependent path:
sim_props_file = os.path.join(project_source_root, 'src/main/resources/sim.properties')
indBenEnv = IndustrialBenchmarkEnv.IndustrialBenchmarkEnv(sim_props_file)

print("\nindBenEnv.reward_range:")
print(indBenEnv.reward_range)
print("\nindBenEnv.action_space (len: %d):" % len(indBenEnv.action_space))
print(indBenEnv.action_space)
print("\nindBenEnv.observation_space (len: %d):" % len(indBenEnv.observation_space))
print(indBenEnv.observation_space)

# Execute a single step
#jpkg_ind_bench_DatavecAction = jpype.JPackage("com.siemens.industrialbenchmark.datavector.action")
#action = {
#		jpkg_ind_bench_DatavecAction.ActionDeltaDescription.DELTA_VELOCITY: 0.1,
#		jpkg_ind_bench_DatavecAction.ActionDeltaDescription.DELTA_GAIN: 0.1,
#		jpkg_ind_bench_DatavecAction.ActionDeltaDescription.DELTA_SHIFT: 0.1}
#action = object()
#action.delta_velocity = 0.1
#action.delta_gain = 0.1
#action.delta_shift = 0.1
action = {
		IndustrialBenchmarkEnv.DELTA_VELOCITY: 0.1,
		IndustrialBenchmarkEnv.DELTA_GAIN: 0.1,
		IndustrialBenchmarkEnv.DELTA_SHIFT: 0.1}
[observation, reward, done, info] = indBenEnv.step(action)

print("\nobservation (len: %d):" % len(observation))
print(observation)
print("\nreward:")
print(reward)
print("\ndone:")
print(done)
print("\ninfo:")
print(info)
print("")

# Some more testing
indBenEnv.step(action)
indBenEnv.seed(12345)
indBenEnv.step(action)
indBenEnv.seed(123456)
indBenEnv.reset()
indBenEnv.step(action)

# Stop the JVM
IndustrialBenchmarkEnv.stop_jvm()
