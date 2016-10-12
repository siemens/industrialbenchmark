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
