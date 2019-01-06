package genetic.demos;

import genetic.Functions;
import genetic.GeneticAlgorithm;
import genetic.GeneticAlgorithm.Combinator;
import genetic.GeneticAlgorithm.FitnessEvaluator;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Mutator;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.UnitGenerator;

import genetic.Meta;
import genetic.generators.RealValuedUnits;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class DemoDouble {
  public static void main(String[] args) {
    Random random = new Random(0L);
    Meta meta = new Meta(-1, -50, 150, 2);

    PopulationInfo populationInfo = new PopulationInfo(80, 2, 0.35, 0.65);
    IterationBounds iterationBounds = new IterationBounds(50_000, 1e-6);
    FitnessEvaluator<List<Double>> fitnessEvaluator = vector ->
            1.0 / Functions.function3(vector);
    RealValuedUnits realValuedUnits = new RealValuedUnits();

    UnitGenerator<List<Double>> unitGenerator = realValuedUnits.unitGenerator(random, meta);
    Combinator<List<Double>> uniformCrossover = realValuedUnits.uniformCrossover(random, meta);
    Mutator<List<Double>> mutator = realValuedUnits.mutator(random, meta);

    GeneticAlgorithm<List<Double>> geneticAlgorithm = new GeneticAlgorithm<>(
            unitGenerator, populationInfo, iterationBounds, fitnessEvaluator, uniformCrossover, mutator,
            random, Logger.getLogger("Iteration info"));

    System.out.println(geneticAlgorithm.iterate().getValue());
  }


}
