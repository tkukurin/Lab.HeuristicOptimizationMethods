package genetic.generator;

import genetic.GAMeta;
import hmo.Evaluator;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import java.util.Random;
import java.util.logging.Logger;

public abstract class SolutionInstanceGenerator {

  final Logger logger;
  final Random random;
  final Problem problem;
  final GAMeta meta;

  public SolutionInstanceGenerator(Random random, Problem problem, GAMeta meta) {
    this.logger = Logger.getLogger(this.getClass().getName());
    this.random = random;
    this.problem = problem;
    this.meta = meta;

    logger.info(String.format("Instantiated %s", this.getClass().getSimpleName()));
  }

  double fitnessFunction(SolutionInstance solutionInstance) {
    return new Evaluator(solutionInstance).fitnessToMaximize();
  }

  abstract SolutionInstance unitGenerator();
  SolutionInstance crossover(SolutionInstance s1, SolutionInstance s2) {
    return crossoverImpl(new SolutionInstance(s1), new SolutionInstance(s2));
  }
  abstract SolutionInstance crossoverImpl(SolutionInstance s1, SolutionInstance s2);

  SolutionInstance mutator(SolutionInstance solutionInstance) {
    return mutatorImpl(new SolutionInstance(solutionInstance));
  }
  abstract SolutionInstance mutatorImpl(SolutionInstance solutionInstance);
}
