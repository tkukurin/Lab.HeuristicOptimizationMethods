package genetic.common;

import genetic.GAMeta;
import genetic.GeneticAlgorithm;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Pair;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.UnitAndFitness;
import genetic.GeneticAlgorithm.UnitGenerator;
import hmo.Evaluator;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class SolutionInstanceGenerator {
  private static final Logger logger = Logger.getLogger(SolutionInstanceUnits.class.toString());

  Random random;
  Problem problem;
  GAMeta meta;

  public SolutionInstanceGenerator(Random random, Problem problem, GAMeta meta) {
    this.random = random;
    this.problem = problem;
    this.meta = meta;
  }

  public Stream<Pair<PopulationInfo, Callable<UnitAndFitness<SolutionInstance>>>> evaluate(
      IterationBounds iterationBounds,
      List<Parameters> parameters) {
    return parameters
        .stream()
        .map(parameter -> new Pair<>(
            parameter.populationInfo,
            new GeneticAlgorithm<>(
                new UnitGenerator<>(this::unitGenerator),
                parameter.populationInfo,
                iterationBounds,
                this::fitnessFunction,
                this::crossover,
                this::mutator,
                random,
                logger)))
        .map(ga -> new Pair<>(ga.first, ga.second::iterate));
  }

  private double fitnessFunction(SolutionInstance solutionInstance) {
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
