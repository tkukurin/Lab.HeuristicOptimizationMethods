package hmo.solver;

import genetic.common.IterationBounds;
import genetic.common.Pair;
import genetic.common.PopulationInfo;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GAMultiThreaded {

  public Iterator<Pair<PopulationInfo, SolutionInstance>> solve(
      Problem problem,
      ExecutorService executorService,
      Parameters ... parameters) {
    IterationBounds iterationBounds = new IterationBounds(50_000, 1);
    Random random = new Random(42L);
    GAMeta meta = new GAMeta();

    SolutionInstanceGenerator generator = new SmarterGenerator(random, problem, meta);
    GARunner runner = new GARunner(generator);

    Stream<Pair<PopulationInfo, Future<UnitAndFitness<SolutionInstance>>>> resultsStream =
        runner.evaluate(iterationBounds, Arrays.asList(parameters))
            .map(pair -> new Pair<>(pair.first, executorService.submit(pair.second)));

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
