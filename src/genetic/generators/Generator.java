package genetic.generators;

import genetic.Assignments.Parameters;
import genetic.GeneticAlgorithm;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Unit;
import genetic.Meta;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public interface Generator<T> {

  List<Unit<List<T>>> evaluate(
      Function<List<Double>, Double> function,
      Random random,
      IterationBounds iterationBounds,
      Meta meta,
      List<Parameters> params);

  GeneticAlgorithm.Mutator<List<T>> mutator(Random random, Meta meta);

  GeneticAlgorithm.Combinator<List<T>> uniformCrossover(
      Random random, Meta meta);

  GeneticAlgorithm.FitnessEvaluator<List<T>> fitnessEvaluator(
      Function<List<Double>, Double> function, Meta meta);

  GeneticAlgorithm.UnitGenerator<List<T>> unitGenerator(
      Random random, Meta meta);

  List<Double> asDoubles(Meta meta, List<T> value);
}
