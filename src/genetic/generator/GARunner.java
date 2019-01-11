package genetic.generator;

import genetic.GeneticAlgorithm;
import genetic.common.IterationBounds;
import genetic.common.Pair;
import genetic.common.PopulationInfo;
import genetic.GeneticAlgorithm.UnitAndFitness;
import genetic.GeneticAlgorithm.UnitGenerator;
import genetic.common.Parameters;
import hmo.instance.SolutionInstance;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class GARunner {

  private SolutionInstanceGenerator generator;

  public GARunner(SolutionInstanceGenerator generator) {
    this.generator = generator;
  }

  public Stream<Pair<PopulationInfo, Callable<UnitAndFitness<SolutionInstance>>>> evaluate(
      IterationBounds iterationBounds,
      List<Parameters> parameters) {
    return parameters
        .stream()
        .map(parameter -> new Pair<>(
            new PopulationInfo(parameter.populationInfo),
            new GeneticAlgorithm<>(
                new UnitGenerator<>(generator::unitGenerator),
                parameter.populationInfo,
                iterationBounds,
                generator::fitnessFunction,
                generator::crossover,
                generator::mutator,
                generator.random,
                generator.logger)))
        .map(ga -> new Pair<>(ga.first, ga.second::iterate));
  }
}
