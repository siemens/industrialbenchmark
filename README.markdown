# Industrial Benchmark 
 
 The "Industrial Benchmark" is a realistic benchmark for offline RL and online RL, used to find those RL algorithms that are best suited for real-world applications.

The Industrial Benchmark includes a variety of aspects that we have identified as essential in industrial applications. It is designed to have the same difficulty and complexity as real RL applications. State- and action-space are continuous, the state-space is rather high-dimensional and only partially observable. The actions consist of three continuous components and act on three steerings. There are delayed effects.

The optimization task is multi-criterial in the sense that there are two reward components, which have opposing dependencies on the actions. The dynamical behavior is heteroskedastic with state-dependent observation noise and state-dependent probability distributions, based on latent variables. The industrial benchmark is designed in such a way that the optimal policy does not approach a fixed operating point in the three steerings.
Each specific choice is based on our experience with industrial challenges.


Requires: Java 8 and Apache Maven 3.x or Python 3.7

For the Python Version, the industrial benchmark environment is contained in industrial_benchmark_python/IDS.py, and there is an OpenAI Gym compliant wrapper in industrial_benchmark_python/IBGym.py

You can install the Benchmark as a package after cloning, using:

	pip install dist/industrial_benchmark_python-2.0-py3-none-any.whl

Or directly from PyPI:

	pip install industrial_benchmark_python

To test whether it works and to check out how current RL methods implemented in the stable_baselines package do on the benchmark:

	python industrial_benchmark_python/test_baselines.py

Documentation: The documentation is available online at: https://arxiv.org/abs/1709.09480

	Source: D. Hein, S. Depeweg, M. Tokic, S. Udluft, A. Hentschel, T.A. Runkler, and V. Sterzing. 
		"A benchmark environment motivated by industrial control problems," in 2017 IEEE 
		Symposium Series on Computational Intelligence (SSCI), 2017, pp. 1-8. 

## Citing Industrial Benchmark

To cite Industrial Benchmark, please reference:

	D. Hein, S. Depeweg, M. Tokic, S. Udluft, A. Hentschel, T.A. Runkler, and V. Sterzing. "A 
		benchmark environment motivated by industrial control problems," in 2017 IEEE Symposium 
		Series on Computational Intelligence (SSCI), 2017, pp. 1-8. 

Additional references using Industrial Benchmark:
	
	S. Depeweg, J. M. Hernández-Lobato, F. Doshi-Velez, and S. Udluft. "Learning and
		policy search in stochastic dynamical systems with Bayesian neural networks." arXiv
		preprint arXiv:1605.07127, 2016.

	D. Hein, S. Udluft, M. Tokic, A. Hentschel, T.A. Runkler, and V. Sterzing. "Batch reinforcement 
		learning on the industrial benchmark: First experiences," in 2017 International Joint 
		Conference on Neural Networks (IJCNN), 2017, pp. 4214–4221.

	S. Depeweg, J. M. Hernández-Lobato, F. Doshi-Velez, and S. Udluft. "Uncertainty decomposition 
		in Bayesian neural networks with latent variables." arXiv preprint arXiv:1605.07127, 
		2017.
		
	D. Hein, A. Hentschel, T. A. Runkler, and S. Udluft. "Particle Swarm Optimization for Model 
		Predictive Control in Reinforcement Learning Environments," in Y. Shi (Ed.), Critical 
		Developments and Applications of Swarm Intelligence, IGI Global, Hershey, PA, USA, 
		2018, pp. 401–427.
		
	S. Depeweg, J. M. Hernandez-Lobato, F. Doshi-Velez, and S. Udluft. "Decomposition of 
		Uncertainty in Bayesian Deep Learning for Efficient and Risk-sensitive Learning." 
		35th International Conference on Machine Learning, ICML 2018. Vol. 3. 2018.
	
	D. Hein, S. Udluft, and T.A. Runkler. "Interpretable policies for reinforcement learning by 
		genetic programming." Engineering Applications of Artificial Intelligence, 76, 2018, 
		pp. 158-169.
	
	D. Hein, S. Udluft, and T.A. Runkler. "Generating interpretable fuzzy controllers using 
		particle swarm optimization and genetic programming," in Proceedings of the Genetic 
		and Evolutionary Computation Conference Companion, ACM, 2018, pp. 1268-1275.
	
	N. Di Palo, and H. Valpola. "Improving Model-Based Control and Active Exploration with 
		Reconstruction Uncertainty Optimization." arXiv preprint arXiv:1812.03955, 2018.
	
	F. Linker. "Industrial Benchmark for Fuzzy Particle Swarm Reinforcement Learning." 
		http://felixlinker.de/doc/ib_fpsrl.pdf, 2019
	
	H. Zhang, A. Zhou, and X. Lin. "Interpretable policy derivation for reinforcement learning 
		based on evolutionary feature synthesis." Complex & Intelligent Systems, 2020. 
		pp. 1-13.
	
	P. Swazinna, S. Udluft, and T.A. Runkler. "Overcoming Model Bias for Robust Offline Deep 
		Reinforcement Learning." arXiv preprint arXiv:2008.05533, 2020.

Additional references mentioning Industrial Benchmark:

	Y. Li. "Deep reinforcement learning: An overview." arXiv preprint arXiv:1701.07274, 2017.
	
	D. Ha, and J. Schmidhuber. "Recurrent world models facilitate policy evolution," in Advances 
		in Neural Information Processing Systems, 2018, pp. 2450-2462.
	
	M. Schaarschmidt, A. Kuhnle, B. Ellis, K. Fricke, F. Gessert, and E. Yoneki. "Lift: 
		Reinforcement learning in computer systems by learning from demonstrations." arXiv 
		preprint arXiv:1808.07903, 2018.
	
	M. Kaiser, C. Otte, T.A. Runkler, and C.H. Ek. "Data Association with Gaussian Processes." 
		arXiv preprint arXiv:1810.07158, 2018.
	
	D. Lee, and J. McNair. "Deep reinforcement learning agent for playing 2D shooting games." Int. 
		J. Control Autom, 11, 2018, pp. 193-200.
	
	D. Marino, and M. Manic. "Modeling and planning under uncertainty using deep neural networks." 
		IEEE Transactions on Industrial Informatics, 2019.
	
	J. Fu, A. Kumar, O. Nachum, G. Tucker, and S. Levine. "Datasets for Data-Driven Reinforcement 
		Learning." arXiv preprint arXiv:2004.07219, 2020.
	
	M. Schaarschmidt. "End-to-end deep reinforcement learning in computer systems." PhD Thesis, 
		University of Cambridge, 2020.
	
	T. Gangwani, Y. Zhou, and J. Peng. "Learning Guidance Rewards with Trajectory-space Smoothing." 
		Advances in Neural Information Processing Systems 33, 2020.
