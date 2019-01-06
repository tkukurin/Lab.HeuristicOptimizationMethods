package hmo.solver;

import genetic.Assignments.Parameters;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.Unit;
import genetic.Meta;
import genetic.generators.BinaryUnits;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class GeneticAlgorithmSolver {

  // TODO everything

  private static SolutionInstance solveGeneticAlgorithm(Problem problem) {
    SolutionInstance solutionInstance = new SolutionInstance(problem);
    IterationBounds iterationBounds = new IterationBounds(25_000, 1e-6);
    int dimension = 20;

    // TODO create function
    Function<List<Double>, Double> function = null;
    Meta meta = new Meta(20, -50, 150, dimension);
    List<Parameters> parameters = Arrays.asList(
        new Parameters(new PopulationInfo(10, 2, 0.1, 0.68)),
        new Parameters(new PopulationInfo(20, 4, 0.2, 0.75)),
        new Parameters(new PopulationInfo(30, 4, 0.2, 0.75)),
        new Parameters(new PopulationInfo(100, 4, 0.35, 0.85)),
        new Parameters(new PopulationInfo(150, 4, 0.04, 0.75)),
        new Parameters(new PopulationInfo(200, 5, 0.5, 0.9))
    );

    // TODO use actual problem
    Random random = new Random(42L);
    BinaryUnits generator = new BinaryUnits();
    List<Unit<List<Boolean[]>>> results = generator.evaluate(
        function, random, iterationBounds, meta, parameters);

    return solutionInstance;
  }
}
