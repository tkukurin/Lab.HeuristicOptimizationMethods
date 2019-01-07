package hmo.solver;

import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.UnitAndFitness;
import genetic.Meta;
import genetic.common.Parameters;
import genetic.common.SolutionInstanceUnits;
import hmo.Evaluator;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class GeneticAlgorithmSolver {

  public static SolutionInstance solve(Problem problem) throws InterruptedException {
    ExecutorService executorService = Executors
        .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    IterationBounds iterationBounds = new IterationBounds(100_000, 1e-6);
    Meta meta = new Meta(0, 0, 0, 0);
    Function<SolutionInstance, Double> function = si -> new Evaluator(si).maximizationFunction();

    List<Parameters> parameters = Arrays.asList(
//        new Parameters(new PopulationInfo(10, 2, 0.7, 0.68)),
//        new Parameters(new PopulationInfo(20, 4, 0.7, 0.75)),
        new Parameters(new PopulationInfo(50, 2, 0.88, 0.75)),
        new Parameters(new PopulationInfo(100, 4, 0.88, 0.85)),
        new Parameters(new PopulationInfo(200, 5, 0.88, 0.75))
//        new Parameters(new PopulationInfo(200, 5, 0.7, 0.9))
    );

    Random random = new Random(42L);
    SolutionInstanceUnits generator = new SolutionInstanceUnits(random, problem, executorService);
    List<Future<UnitAndFitness<SolutionInstance>>> results = generator.evaluate(
        function, iterationBounds, meta, parameters);
    executorService.shutdown();

    return results.stream()
        .map(future -> {
          try { return future.get(); }
          catch (Exception e) { throw new RuntimeException(e); }
        }).max(Comparator.comparingDouble(UnitAndFitness::getFitness))
        .map(uf -> uf.getUnit().getValue())
        .get();
  }
}
