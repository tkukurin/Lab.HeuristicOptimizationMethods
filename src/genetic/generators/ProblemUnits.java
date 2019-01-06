package genetic.generators;

import genetic.Assignments.Parameters;
import genetic.GeneticAlgorithm.Combinator;
import genetic.GeneticAlgorithm.FitnessEvaluator;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Mutator;
import genetic.GeneticAlgorithm.Unit;
import genetic.GeneticAlgorithm.UnitGenerator;
import genetic.Meta;
import hmo.problem.Problem;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class ProblemUnits implements Generator<Problem> {

  // TODO this should be implemented for the genetic algorithm solution

  @Override
  public List<Unit<List<Problem>>> evaluate(
      Function<List<Double>, Double> function, Random random,
      IterationBounds iterationBounds, Meta meta, List<Parameters> params) {
    return null;
  }

  @Override
  public Mutator<List<Problem>> mutator(Random random, Meta meta) {
    return null;
  }

  @Override
  public Combinator<List<Problem>> uniformCrossover(Random random, Meta meta) {
    return null;
  }

  @Override
  public FitnessEvaluator<List<Problem>> fitnessEvaluator(Function<List<Double>, Double> function,
      Meta meta) {
    return null;
  }

  @Override
  public UnitGenerator<List<Problem>> unitGenerator(Random random, Meta meta) {

    return null;
  }

  @Override
  public List<Double> asDoubles(Meta meta, List<Problem> value) {
    return null;
  }
}
