package genetic.generators;

import genetic.Assignments.Parameters;
import genetic.GeneticAlgorithm;
import genetic.GeneticAlgorithm.Combinator;
import genetic.GeneticAlgorithm.FitnessEvaluator;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Mutator;
import genetic.GeneticAlgorithm.Unit;
import genetic.GeneticAlgorithm.UnitGenerator;
import genetic.Meta;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.logging.Logger;

public class RealValuedUnits implements Generator<Double> {

  public Mutator<List<Double>> mutator(Random random,
      Meta meta) {
    return vector -> {
      ArrayList<Double> vals = new ArrayList<>();
      for (int i = 0; i < vector.size(); i++) {
        double amount = random.nextDouble() * (meta.upperBound - meta.lowerBound);
        double sign = Math.signum(random.nextDouble() - 1);
        double newValue = vector.get(i) + sign * amount;

        if (newValue > meta.upperBound) {
          newValue = meta.upperBound;
        } else if (newValue < meta.lowerBound) {
          newValue = meta.lowerBound;
        }

        vals.add(newValue);
      }

      return vals;
    };
  }

  public Combinator<List<Double>> uniformCrossover(Random random, Meta meta) {
    return (fst, snd) -> {
      ArrayList<Double> vals = new ArrayList<>();

      for (int i = 0; i < fst.size(); i++) {
        double fstEl = fst.get(i), sndEl = snd.get(i);
        double alpha = random.nextDouble();
        vals.add(alpha * fstEl + (1 - alpha) * sndEl);
      }

      return vals;
    };
  }

  @Override
  public FitnessEvaluator<List<Double>> fitnessEvaluator(Function<List<Double>, Double> function,
      Meta meta) {
    return (FitnessEvaluator<List<Double>>) function;
  }

  public UnitGenerator<List<Double>> unitGenerator(Random random, Meta meta) {
    return new UnitGenerator<>(() -> {
      ArrayList<Double> vals = new ArrayList<>();
      for (int i = 0; i < meta.dimension; i++) {
        double hi = meta.upperBound, lo = meta.lowerBound;
        vals.add(random.nextDouble() * random.nextInt((int) (hi - lo)) + lo);
      }
      return vals;
    });
  }

  @Override
  public List<Double> asDoubles(Meta meta, List<Double> value) {
    return value;
  }

  public List<Unit<List<Double>>> evaluate(
      Function<List<Double>, Double> function,
      Random random,
      IterationBounds iterationBounds,
      Meta meta,
      List<Parameters> params) {
    UnitGenerator<List<Double>> randomBitSequenceSupplier = unitGenerator(random, meta);
    FitnessEvaluator<List<Double>> fitnessEvaluator = function::apply;
    Combinator<List<Double>> uniformCrossover = uniformCrossover(random, meta);
    Mutator<List<Double>> mutator = mutator(random, meta);

    List<Unit<List<Double>>> results = new ArrayList<>();
    for (Parameters parameters : params) {

      GeneticAlgorithm<List<Double>> geneticAlgorithm = new GeneticAlgorithm<>(
          randomBitSequenceSupplier, parameters.populationInfo, iterationBounds, fitnessEvaluator,
          uniformCrossover, mutator, random, Logger.getLogger("Iteration info"));

      GeneticAlgorithm.Unit<List<Double>> result = geneticAlgorithm.iterate();
      results.add(result);
    }

    return results;
  }
}
