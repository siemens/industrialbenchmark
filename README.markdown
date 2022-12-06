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

To test whether it works you can run

    cd industrial_benchmark_python
    python test_gym_wrapper.py

and to check out how current RL methods implemented in the stable_baselines package do on the benchmark (stable baselines needs to be installed):

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
	
	A. Kumar. "Policy Optimization for Industrial Benchmark Using Deep Reinforcement Learning."
		PhD Thesis, Colorado State University, 2020.
	
	R. Qin, S. Gao, X. Zhang, Z. Xu, S. Huang, Z. Li, Z. Weinan, and Y. Yu. "NeoRL: A Near 
		Real-World Benchmark for Offline Reinforcement Learning." arXiv preprint 
		arXiv:2102.00714, 2021.
	
	P. Swazinna, S. Udluft, D. Hein, and T.A. Runkler. "Behavior Constraining in Weight 
		Space for Offline Reinforcement Learning." arXiv preprint arXiv:2107.05479, 2021.
		
	V. Kurenkov, and S. Kolesnikov. "Showing Your Offline Reinforcement Learning Work: Online 
		Evaluation Budget Matters." arXiv preprint arXiv:2110.04156, 2021.
	
	P. Swazinna, S. Udluft, and T.A. Runkler. "Measuring Data Quality for Dataset Selection in 
		Offline Reinforcement Learning," in 2021 IEEE Symposium Series on Computational 
		Intelligence (SSCI), 2021, pp. 1-8.
	
	P. Swazinna, S. Udluft, D. Hein, and T.A. Runkler. "Comparing Model-free and Model-based 
		Algorithms for Offline Reinforcement Learning." arXiv preprint arXiv:2201.05433, 2022.
	
	C. Feng, and G. Jinyan. "Reliable Offline Model-based Optimization for Industrial Process 
		Control." arXiv preprint arXiv:2205.07250, 2022.
		
	P. Swazinna, S. Udluft, and T.A. Runkler. "User-Interactive Offline Reinforcement Learning." 
		arXiv preprint arXiv:2205.10629, 2022.

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
	
	A. Zubow, S. Rösler, P. Gawłowicz, F. Dressler. "GrGym: When GNU Radio goes to (AI) Gym," in 
		Proceedings of the 22nd International Workshop on Mobile Computing Systems and 
		Applications, 2021, pp. 8-14.
		
	J. McLeod, H. Stojic, V. Adam, D. Kim, J. Grau-Moya, P. Vrancx, and F. Leibfried. "Bellman: 
		A Toolbox for Model-Based Reinforcement Learning in TensorFlow." arXiv preprint 
		arXiv:2103.14407, 2021.
		
	T. Xu, and Y. Liang. "Provably Efficient Offline Reinforcement Learning with Trajectory-Wise 
		Reward." arXiv preprint arXiv:2206.06426, 2022.
		
	F.M. Luo, T. Xu, H. Lai, X.H. Chen, W. Zhang, and Y. Yu. "A survey on model-based reinforcement 
		learning." arXiv preprint arXiv:2206.09328, 2022.

	B. Han, Z. Ren, Z. Wu, Y. Zhou, and J. Peng. "Off-Policy Reinforcement Learning with 
		Delayed Rewards." arXiv preprint arXiv:2106.11854, 2021.
		
	M. Kaiser. "Structured Models with Gaussian Processes." Doctoral dissertation, Technische 
		Universität München, 2021.
		
	M. Videau. "Découverte de Politiques Interprétables pour l'Apprentissage par Renforcement 
		via la Programmation Génétique." Doctoral dissertation, Université Paris Dauphine-PSL,
		2021.
	
	Z. Ren, R. Guo, Y. Zhou, and J. Peng. "Learning Long-Term Reward Redistribution via Randomized 
		Return Decomposition." arXiv preprint arXiv:2111.13485, 2021.
	
	M. Videau, A. Leite, O. Teytaud, and M. Schoenauer. "Multi-objective Genetic Programming for 
		Explainable Reinforcement Learning," in European Conference on Genetic Programming 
		(Part of EvoStar), Springer, Cham, 2022, pp. 278-293.
	
	X.-Y. Liu, Z. Xia, J. Rui, J. Gao, H. Yang, M. Zhu, C.D. Wang, Z. Wang, and J. Guo. 
		"FinRL-Meta: Market Environments and Benchmarks for Data-Driven Financial 
		Reinforcement Learning." arXiv preprint arXiv:2211.03107, 2022.
		
	T. Xu. "Towards the Understanding of Sample Efficient Reinforcement Learning Algorithms." 
		Doctoral dissertation, The Ohio State University, 2022.
	
	M. Schlappa, J. Hegemann, and S. Spinler. "Optimizing Control of Waste Incineration Plants 
		Using Reinforcement Learning and Digital Twins." IEEE Transactions on Engineering 
		Management, 2022.
		
	F. Vignat, N. Béraud, and T.T.D. Montcel. "Toolpath Calculation Using Reinforcement Learning 
		in Machining," in International Joint Conference on Mechanics, Design Engineering & 
		Advanced Manufacturing, Springer, Cham, 2023, pp. 1149-1158.
		
	F. Huang, J. Xu, D. Wu, Y. Cui, Z. Yan, W. Xing, and X.Zhang. "A general motion controller 
		based on deep reinforcement learning for an autonomous underwater vehicle with 
		unknown disturbances." Engineering Applications of Artificial Intelligence 117, 2023.
