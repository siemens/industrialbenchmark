Industrial Benchmark: 
=====================

Requires: Java 8 and Apache Maven 3.x

Documentation: The documentation is available online at: https://arxiv.org/abs/1610.03793
	Source: D. Hein, A. Hentschel, V. Sterzing, M. Tokic and S. Udluft. Introduction to the 
	“Industrial Benchmark”. CoRR, arXiv:1610.03793 [cs.LG], pages 1-11. 2016. 
		
		
Inclusion as a dependency to your Java/Maven project: 
=====================================================

	<dependency>
	    <groupId>com.siemens.oss.industrialbenchmark</groupId>
	    <artifactId>industrialbenchmark</artifactId>
	    <version>1.1.0</version>
	</dependency>


Compilation + Run:  
==================
	mvn clean package
	java -jar industrialbenchmark-<VERSION>.jar <OPTIONAL_CONFIG_FILE>
	
	E.g.: java -jar target/industrialbenchmark-<VERSION>-SNAPSHOT-jar-with-dependencies.jar src/main/resources/sim.properties 

	=> a random trajectory is generated 
	=> all observable state variables are written to file dyn-observable.csv.
	=> all markov state variables are written to file dyn-markov.csv

