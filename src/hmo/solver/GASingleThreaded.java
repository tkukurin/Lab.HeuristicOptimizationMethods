package hmo.solver;

import genetic.GAMeta;
import genetic.GeneticAlgorithm.UnitAndFitness;
import genetic.common.IterationBounds;
import genetic.common.Pair;
import genetic.common.Parameters;
import genetic.common.PopulationInfo;
import genetic.generator.GARunner;
import genetic.generator.SmarterGenerator;
import genetic.generator.SolutionInstanceGenerator;
import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class GASingleThreaded {

  public Iterator<Pair<PopulationInfo, UnitAndFitness<SolutionInstance>>> solve(
      Problem problem,
      Parameters parameters) {
    IterationBounds iterationBounds = new IterationBounds(50_000, 1);
    Random random = new Random(42L);
    GAMeta meta = new GAMeta();

    SolutionInstanceGenerator generator = new SmarterGenerator(random, problem, meta);
    GARunner runner = new GARunner(generator);

    Stream<Pair<PopulationInfo, Future<UnitAndFitness<SolutionInstance>>>> resultsStream =
        runner.evaluate(iterationBounds, Collections.singletonList(parameters))
            .map(pair -> new Pair<>(pair.first,
                CompletableFuture.completedFuture(Utils.unchecked(() -> pair.second.call()))));

    return Collections.singletonList(resultsStream
        .map(p -> new Pair<>(p.first, Utils.unchecked(() -> p.second.get())))
        .findFirst().get()).iterator();
  }
}
