# Industrial Benchmark

Requires: Java 8 and Apache Maven 3.x

Documentation: The documentation is available online at: https://arxiv.org/abs/1610.03793
	Source: D. Hein, A. Hentschel, V. Sterzing, M. Tokic and S. Udluft. Introduction to the
	"Industrial Benchmark". CoRR, arXiv:1610.03793 [cs.LG], pages 1-11. 2016.


## Inclusion as a dependency to your Java/Maven project

	<dependency>
		<groupId>com.siemens.oss.industrialbenchmark</groupId>
		<artifactId>industrialbenchmark</artifactId>
		<version>1.1.1</version>
	</dependency>


## Compilation + Run

NOTE: It is important to run the maven clean phase (`mvn clean`, like below)
when working with this project for the first time.
This will install the `RLGlue:JavaRLGlueCodec:1.0` dependency into the local repo.
Without it, compilation will fail.

	mvn clean package
	java -jar target/industrialbenchmark-*-jar-with-dependencies.jar
	# or
	java -jar target/industrialbenchmark-*-jar-with-dependencies.jar src/main/resources/sim.properties

* => a random trajectory is generated
* => all observable state variables are written to file dyn-observable.csv.
* => all markov state variables are written to file dyn-markov.csv

# Sample usage in code

An example usage of the industrial benchmark can be found in the class `com.siemens.industrialbenchmark.ExampleMain`.
It is intented to be a template for data generation.


