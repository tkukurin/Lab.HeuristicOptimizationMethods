package hmo.solver;

import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Pair;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.UnitAndFitness;
import genetic.GAMeta;
import genetic.common.Parameters;
import genetic.common.RandomizedGenerator;
import genetic.common.SolutionInstanceGenerator;
import genetic.common.SolutionInstanceUnits;
import hmo.Evaluator;
import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneticAlgorithmSolver {

  public static Iterator<Pair<PopulationInfo, SolutionInstance>> solve(
      Problem problem,
      ExecutorService executorService) {
    IterationBounds iterationBounds = new IterationBounds(5_000, 1);
    Random random = new Random(42L);
    GAMeta meta = new GAMeta();
    List<Parameters> parameters = Arrays.asList(
      new Parameters(new PopulationInfo(50, 4, 0.95, 0.6))
//      new Parameters(new PopulationInfo(100, 2, 0.88, 0.85))
//      new Parameters(new PopulationInfo(500, 5, 0.88, 0.75)),
//      new Parameters(new PopulationInfo(500, 10, 0.7, 0.9)),
//      new Parameters(new PopulationInfo(500, 2, 0.5, 0.5))
    );

    SolutionInstanceGenerator generator = new RandomizedGenerator(random, problem, meta);

    Stream<Pair<PopulationInfo, Future<UnitAndFitness<SolutionInstance>>>> resultsStream =
        generator.evaluate(iterationBounds, parameters)
            .map(pair -> new Pair<>(pair.first,
                CompletableFuture.completedFuture(
                    Utils.unchecked(() -> pair.second.call()))));
//            .map(pair -> new Pair<>(pair.first, executorService.submit(pair.second)));

    List<Pair<PopulationInfo, Future<UnitAndFitness<SolutionInstance>>>> results =
        resultsStream.collect(Collectors.toList());

    return new Iterator<Pair<PopulationInfo, SolutionInstance>>() {
      @Override
      public boolean hasNext() {
        return !results.isEmpty();
      }

      @Override
      public Pair<PopulationInfo, SolutionInstance> next() {
        for (Pair<PopulationInfo, Future<UnitAndFitness<SolutionInstance>>> pair : results) {
          if (pair.second.isDone()) {
            results.remove(pair);
            SolutionInstance solutionInstance = Utils.unchecked(pair.second::get).getUnit().getValue();
            return new Pair<>(pair.first, solutionInstance);
          }
        }
        return null;
      }
    };
  }
}
