package hmo.solver;

import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Pair;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.UnitAndFitness;
import genetic.GAMeta;
import genetic.generator.GARunner;
import genetic.common.Parameters;
import genetic.generator.SmarterGenerator;
import genetic.generator.SolutionInstanceGenerator;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneticAlgorithmSolver {

  public static Iterator<Pair<PopulationInfo, SolutionInstance>> solve(
      Problem problem,
      ExecutorService executorService) {
    IterationBounds iterationBounds = new IterationBounds(50_000, 1);
    Random random = new Random(42L);
    GAMeta meta = new GAMeta();
    List<Parameters> parameters = Arrays.asList(
//      new Parameters(new PopulationInfo(20, 1, 0.95, 0.6)),
//      new Parameters(new PopulationInfo(20, 1, 0.90, 0.70)),
//      new Parameters(new PopulationInfo(15, 1, 0.98, 0.98)),
      new Parameters(new PopulationInfo(50, 3, 0.99, 0.99))
//      new Parameters(new PopulationInfo(20, 1, 0.95, 0.9))
    );

    SolutionInstanceGenerator generator = new SmarterGenerator(random, problem, meta);
    GARunner runner = new GARunner(generator);

    Stream<Pair<PopulationInfo, Future<UnitAndFitness<SolutionInstance>>>> resultsStream =
        runner.evaluate(iterationBounds, parameters)
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
