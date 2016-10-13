Industrial Benchmark: 
=====================

Requires: Java 8 and Apache Maven 3.x

Compilation: 
============
	
	mvn clean package

Running the example:
==================== 
	
	java -jar industrialbenchmark-<VERSION>.jar <OPTIONAL_CONFIG_FILE>
	
	For example: java -jar target/industrialbenchmark-<VERSION>-SNAPSHOT-jar-with-dependencies.jar src/main/resources/sim.properties 

	=> a random trajectory is generated 
	=> all observable state variables are written to file dyn-observable.csv.
	=> all markov state variables are written to file dyn-markov.csv

Documentation: 
==============

	The documentation is available online at: https://arxiv.org/abs/1610.03793
	
	Source: D. Hein, A. Hentschel, V. Sterzing, M. Tokic and S. Udluft. Introduction to the “Industrial Benchmark”. 
		CoRR, arXiv:1610.03793 [cs.LG], pages 1-11. 2016. 
	
