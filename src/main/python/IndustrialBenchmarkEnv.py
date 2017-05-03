import jpype
import gym
import os

# These will be set in load_constants()
DELTA_VELOCITY = None
DELTA_GAIN = None
DELTA_SHIFT = None
RANDOM_SEED = None

def load_constants():
	'''Set some constants to values from Java.
	'''
	global DELTA_VELOCITY
	global DELTA_GAIN
	global DELTA_SHIFT
	global RANDOM_SEED
	jpkg_action = jpype.JPackage("com.siemens.industrialbenchmark.datavector.action")
	jpkg_state = jpype.JPackage("com.siemens.industrialbenchmark.datavector.state")
	DELTA_VELOCITY = jpkg_action.ActionDeltaDescription.DELTA_VELOCITY
	DELTA_GAIN = jpkg_action.ActionDeltaDescription.DELTA_GAIN
	DELTA_SHIFT = jpkg_action.ActionDeltaDescription.DELTA_SHIFT
	RANDOM_SEED = jpkg_state.MarkovianStateDescription.RANDOM_SEED

def start_jvm(jvm_path = None, ind_bench_jar = None):
	'''Starts the Java Virtual Machine (JVM) for use with this class.
	Note: The JVM can only be started (and stopped) a single time per process!
	So if you want to use it over many simulations in a single process,
	only stop it in the very end, after all Java related activities have ended.

	Keyword arguments:
	sim_props_file -- file path string to a *.properties;
			example: industrialbenchmark/src/main/resources/sim.properties
	'''
	if jpype.isJVMStarted():
		raise Exception("The JVM was already started by this process!")
	if (jvm_path == None):
		jvm_path = jpype.getDefaultJVMPath()
	if (ind_bench_jar == None):
		# FIXME System and version dependent paths:
		this_files_dir = os.path.dirname(os.path.realpath(__file__))
		# We just assume we are in the siemens/industrialbenchmark sources
		project_source_root = os.path.join(this_files_dir, '../../..')
		ind_bench_jar = os.path.join(project_source_root, 'target/industrialbenchmark-1.1.2-SNAPSHOT-jar-with-dependencies.jar')

	classpath = ind_bench_jar
	jpype.startJVM(jvm_path, "-Djava.class.path=%s" % classpath)
	load_constants()

def stop_jvm():
	'''Shuts down the Java Virtual Machine (JVM).
	Note: The JVM can only be started (and stopped) a single time per process!
	So if you want to use it over many simulations in a single process,
	only stop it in the very end, after all Java related activities have ended.
	'''
	if jpype.isJVMStarted():
		jpype.shutdownJVM()

def is_jvm_running():
	'''Indicates whether a Java Virtual Machine (JVM) is running for this process.
	'''
	return jpype.isJVMStarted()


def to_java_list(py_list):
	"""Convert from a Python to a Java list.

    Keyword arguments:
    py_list -- a python list, for example: [a, b, c]

    Returns:
    java_list -- a java.util.List with the same content as py_list
    """
	java_list = jpype.java.util.ArrayList()
	for item in py_list:
		java_list.add(item)
	return java_list

def from_java_list(java_list):
	"""Convert from a Java to a Python list.

    Keyword arguments:
    java_list -- a java.util.List

    Returns:
    py_list -- a python list (like [a, b, c]) with the same content as java_list
    """
	py_list = [None] * java_list.size()
	for it in range(0, java_list.size()):
		py_list[it] = java_list.get(it)
	return py_list

def into_java_dictionary(java_dict, py_dict):
	'''Copies values from a Python to a Java dicionary.

    Keyword arguments:
    java_dict -- the target (an implementation of com.siemens.rl.interfaces.DataVector)
    py_dict -- the source (a python dictionary, as in `myDic['key'] = value`)
	'''
	for key in py_dict:
		java_dict.setValue(key, float(py_dict[key]))


class IndustrialBenchmarkEnv(gym.Env):
	'''An OpenAI Gym environment implementation/wrapper
	of the (Siemens) Industrial Benchmark simulator of gas and wind turbines
	(written in Java).
	The JPype library is used as the Python <=> Java bridge.

	To install prerequisites:
		# Python 2
		sudo pip install Numpy
		sudo pip install JPype1
		sudo pip install gym
		# Python 3
		sudo pip3 install Numpy
		sudo pip3 install JPype1-py3
		sudo pip3 install gym

	See:
	* https://github.com/openai/gym/blob/master/gym/core.py
	* https://gym.openai.com/docs
	* https://github.com/siemens/industrialbenchmark
	'''

	def __init__(self, sim_props_file):
		'''Creates a new IndustrialBenchmarkEnv on the basis
		of simulation properties from a properties file.

		Keyword arguments:
		sim_props_file -- file path string to a *.properties;
				example: industrialbenchmark/src/main/resources/sim.properties
		'''
		if not is_jvm_running():
			raise Exception("The JVM needs to be running!")

		load_constants()

		self._sim_props_file = sim_props_file
		self._jpkg_ind_bench_ = jpype.JPackage("com.siemens.industrialbenchmark")
		self._jpkg_ind_bench_dyn = jpype.JPackage("com.siemens.industrialbenchmark.dynamics")
		self._jpkg_ind_bench_properties = jpype.JPackage("com.siemens.industrialbenchmark.properties")
		self._jpkg_ind_bench_data_vec = jpype.JPackage("com.siemens.industrialbenchmark.datavector")
		self._jpkg_ind_bench_data_vec_action = jpype.JPackage("com.siemens.industrialbenchmark.datavector.action")
		self._jpkg_ind_bench_data_vec_state = jpype.JPackage("com.siemens.industrialbenchmark.datavector.state")

		self._props = self._jpkg_ind_bench_properties.PropertiesUtil.loadSetPointProperties(jpype.java.io.File(self._sim_props_file));
		self._java_env = self._jpkg_ind_bench_dyn.IndustrialBenchmarkDynamics(self._props)
		self._action = self._jpkg_ind_bench_data_vec_action.ActionDelta(0.0, 0.0, 0.0)
		self._seed_data_vec = self._jpkg_ind_bench_data_vec.DataVectorImpl(to_java_list([RANDOM_SEED]))

		# Override (from gym.Env)
		self.reward_range = (
				int(self._props.getProperty(self._jpkg_ind_bench_data_vec_state.ObservableStateDescription.REWARD_TOTAL + "_MIN")),
				int(self._props.getProperty(self._jpkg_ind_bench_data_vec_state.ObservableStateDescription.REWARD_TOTAL + "_MAX")))
		self.action_space = from_java_list(self._action.getDescription().getVarNames()) # returns java.util.List<String>
		self.observation_space = from_java_list(self._java_env.getMarkovState().getDescription().getVarNames()) # returns java.util.List<String>

	def _step(self, action):
		# XXX what would be better?
		# XXX option 1:
		#self._action.setDeltaVelocity(action.delta_velocity)
		#self._action.setDeltaGain(action.delta_gain)
		#self._action.setDeltaShift(action.delta_shift)
		# XXX option 2:
		self._action.setDeltaVelocity(action[DELTA_VELOCITY])
		self._action.setDeltaGain(action[DELTA_GAIN])
		self._action.setDeltaShift(action[DELTA_SHIFT])
		# XXX option 3:
		# action being a jpype.com.siemens.industrialbenchmark.datavector.action.ActionDelta:
		#into_java_dictionary(self._action, action)

		reward = self._java_env.step(self._action)

		observation = self._java_env.getMarkovState().getValuesArray() # returns double[]
		done = False # never finnished
		info = None # XXX Anything useful we could put here?
		return [observation, reward, done, info]

	def _reset(self):
		self._java_env.reset()

	def _render(self, mode='human', close=False):
		'''We do not support any visualization.
		'''
		return

	def _seed(self, seed=None):
		self._seed_data_vec.setValue(RANDOM_SEED, float(seed))
		print(from_java_list(self._seed_data_vec.getKeys()))
		self._java_env.setMarkovState(self._seed_data_vec)
		return [seed]
