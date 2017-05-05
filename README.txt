Industrial Benchmark: 
=====================

Requires: Java 8 and Apache Maven 3.x

Documentation: The documentation is available online at: https://arxiv.org/abs/1610.03793
	Source: D. Hein, A. Hentschel, V. Sterzing, M. Tokic and S. Udluft. "Introduction to the 
	Industrial Benchmark". CoRR, arXiv:1610.03793 [cs.LG], pages 1-11. 2016. 
	
Citing Industrial Benchmark:
============================
To cite Industrial Benchmark, please reference:
	D. Hein, A. Hentschel, V. Sterzing, M. Tokic and S. Udluft. "Introduction to the 
		Industrial Benchmark". CoRR, arXiv:1610.03793 [cs.LG], pages 1-11. 2016.
		
	D. Hein, S. Udluft, M. Tokic, A. Hentschel, T.A. Runkler, and V. Sterzing. "Batch Reinforcement 
		Learning on the Industrial Benchmark: First Experiences." Neural Networks (IJCNN), 2017 
		International Joint Conference on. IEEE, 2017. (accepted) (to be published)
		
	S. Depeweg, J. M. Hernández-Lobato, F. Doshi-Velez, and S. Udluft. "Learning and 
		policy search in stochastic dynamical systems with bayesian neural networks." arXiv 
		preprint arXiv:1605.07127, 2016.
		
Inclusion as a dependency to your Java/Maven project: 
=====================================================

	<dependency>
	    <groupId>com.siemens.oss.industrialbenchmark</groupId>
	    <artifactId>industrialbenchmark</artifactId>
	    <version>1.1.1</version>
	</dependency>


Compilation + Run:  
==================
	mvn clean package
	java -jar industrialbenchmark-<VERSION>.jar <OPTIONAL_CONFIG_FILE>
	
	E.g.: java -jar target/industrialbenchmark-<VERSION>-SNAPSHOT-jar-with-dependencies.jar src/main/resources/sim.properties 

	=> a random trajectory is generated 
	=> all observable state variables are written to file dyn-observable.csv.
	=> all markov state variables are written to file dyn-markov.csv

Example main()-Function:
========================

	An example usage of the industrial benchmark can be found in the class com.siemens.industrialbenchmark.ExampleMain.
	This class is intented to be a template for data generation. 
	
	
