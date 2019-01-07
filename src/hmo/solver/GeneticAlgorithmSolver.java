package hmo.solver;

import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.UnitKeyFitnessValue;
import genetic.Meta;
import genetic.common.Parameters;
import genetic.common.SolutionInstanceUnits;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class GeneticAlgorithmSolver {

  public static UnitKeyFitnessValue solve(Problem problem) {
    SolutionInstance solutionInstance = new SolutionInstance(problem);
    IterationBounds iterationBounds = new IterationBounds(25_000, 1e-6);
    Random random = new Random(42L);

    SolutionInstanceUnits generator = new SolutionInstanceUnits(random, problem);
    Meta meta = new Meta(0, 0, 0, 0);
    Function<List<Double>, Double> function = l -> {
      return l.get(0);
    };

    List<Parameters> parameters = Arrays.asList(
        new Parameters(new PopulationInfo(10, 2, 0.7, 0.68)),
        new Parameters(new PopulationInfo(20, 4, 0.7, 0.75)),
        new Parameters(new PopulationInfo(30, 4, 0.7, 0.75)),
        new Parameters(new PopulationInfo(100, 4, 0.7, 0.85)),
        new Parameters(new PopulationInfo(150, 4, 0.7, 0.75)),
        new Parameters(new PopulationInfo(200, 5, 0.7, 0.9))
    );

    List<UnitKeyFitnessValue> results = generator.evaluate(
        function, random, iterationBounds, meta, parameters);

    return results.stream().max(Comparator.comparingDouble(v -> (double) v.getValue())).get();
  }
}
