# Industrial Benchmark (Java)

Requires: Java 8 and Apache Maven 3.x

Documentation: The documentation is available online at: https://arxiv.org/abs/1709.09480
	Source: D. Hein, S. Depeweg, M. Tokic, S. Udluft, A. Hentschel, T.A. Runkler, and V. Sterzing. 
	"A Benchmark Environment Motivated by Industrial Control Problems". arXiv preprint arXiv:1709.09480, 2017. 

## Citing Industrial Benchmark

To cite Industrial Benchmark, please reference:
	D. Hein, S. Depeweg, M. Tokic, S. Udluft, A. Hentschel, T.A. Runkler, and V. Sterzing. "A Benchmark Environment 
		Motivated by Industrial Control Problems". arXiv preprint arXiv:1709.09480, 2017. 

	D. Hein, S. Udluft, M. Tokic, A. Hentschel, T.A. Runkler, and V. Sterzing. "Batch Reinforcement 
		Learning on the Industrial Benchmark: First Experiences," in 2017 International Joint Conference on Neural
		Networks (IJCNN), 2017, pp. 4214–4221.

	S. Depeweg, J. M. Hernández-Lobato, F. Doshi-Velez, and S. Udluft. "Learning and
		policy search in stochastic dynamical systems with bayesian neural networks." arXiv
		preprint arXiv:1605.07127, 2016.

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
* => all observable state variables are written to file _dyn-observable.csv_.
* => all markov state variables are written to file _dyn-markov.csv_

# Sample usage in code

An example usage of the industrial benchmark can be found in the class `com.siemens.industrialbenchmark.ExampleMain`.
It is intended to be a template for data generation.

