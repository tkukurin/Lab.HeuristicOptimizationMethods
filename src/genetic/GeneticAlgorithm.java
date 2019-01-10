package genetic;

import genetic.common.Unit;
import hmo.Evaluator;
import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import java.util.*;
import java.util.function.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Maximizes given function. */
public class GeneticAlgorithm<T> {

  public static class Pair<A, B> {
    public A first;
    public B second;

    public Pair(A first, B second) {
      this.first = first;
      this.second = second;
    }
  }

  public static class PopulationInfo {
    int size;
    int elitism;

    double mutationProbability;
    double crossoverProbability;

    public PopulationInfo(int size, int elitism, double mutationProbability, double crossoverProbability) {
      this.size = size;
      this.elitism = elitism;
      this.mutationProbability = mutationProbability;
      this.crossoverProbability = crossoverProbability;
    }

    @Override
    public String toString() {
      return "PopulationInfo{" +
              "size=" + size +
              ", elitism=" + elitism +
              ", mutationProbability=" + mutationProbability +
              ", crossoverProbability=" + crossoverProbability +
              '}';
    }
  }

  public static class IterationBounds {
    int numIterations;
    double deltaThreshold;


    public IterationBounds(int numIterations, double deltaThreshold) {
      this.numIterations = numIterations;
      this.deltaThreshold = deltaThreshold;
    }

    boolean complete(int numIterations) {
//      Double lastError = lastErrors.peekLast();
//      ArrayDeque<Double> errorsShifted = new ArrayDeque<>(lastErrors);
//      errorsShifted.addFirst(errorsShifted.pollLast());
//
//      double maxDeltaBetweenIterations = Utils
//          .zip(new ArrayList<>(lastErrors), new ArrayList<>(errorsShifted))
//          .stream()
//          .map(pair -> Math.abs(pair.first - pair.second))
//          .max(Comparator.comparingDouble(d -> d))
//          .orElse(Double.MAX_VALUE);

      return numIterations == this.numIterations;
//          || lastError <= this.deltaThreshold
//          || (errorsShifted.size() > 1 && maxDeltaBetweenIterations <= this.deltaThreshold);
    }
  }

  public static class UnitGenerator<T> {
    private final Supplier<T> supplier;

    public UnitGenerator(Supplier<T> supplier) {
      this.supplier = supplier;
    }

    List<Unit<T>> init(int size) {
      return IntStream.range(0, size)
          .mapToObj(i -> new Unit<>(supplier.get()))
          .collect(Collectors.toList());
    }
  }

  public interface FitnessEvaluator<T> extends Function<T, Double> {
  }

  public static class UnitAndFitness<T> extends AbstractMap.SimpleImmutableEntry<
      Unit<T>, Double> {
    public UnitAndFitness(Unit<T> key, Double value) {
      super(key, value);
    }

    public Unit<T> getUnit() {
      return super.getKey();
    }

    public Double getFitness() {
      return super.getValue();
    }
  }

  public interface Mutator<T> extends Function<T, T> {
    default Unit<T> apply(Unit<T> unit) {
      return new Unit<>(apply(unit.value));
    }
  }

  public interface Combinator<T> extends BiFunction<T, T, T> {
    default Unit<T> apply(Unit<T> a, Unit<T> b) {
      return new Unit<>(apply(a.value, b.value));
    }
  }

  private PopulationInfo populationInfo;
  private IterationBounds iterationBounds;
  private FitnessEvaluator<T> fitnessEvaluator;
  private List<UnitAndFitness<T>> population;
  private Combinator<T> crossover;
  private Mutator<T> mutator;
  private Random random;
  private Logger logger;

  public GeneticAlgorithm(
      UnitGenerator<T> unitGenerator,
      PopulationInfo populationInfo,
      IterationBounds iterationBounds,
      FitnessEvaluator<T> fitnessEvaluator,
      Combinator<T> crossover,
      Mutator<T> mutator,
      Random random,
      Logger logger) {
    this.populationInfo = populationInfo;
    this.iterationBounds = iterationBounds;
    this.fitnessEvaluator = fitnessEvaluator;
    this.crossover = crossover;
    this.mutator = mutator;
    this.random = random;
    this.population = sortedByDescendingFitness(unitGenerator.init(populationInfo.size).stream()
        .map(unit -> new UnitAndFitness<>(unit, fitnessEvaluator.apply(unit.value))));
    this.logger = logger;
  }

  public UnitAndFitness<T> iterate() {
    int iterations = 0;
    UnitAndFitness<T> best = new UnitAndFitness<>(new Unit<>(null), 0.0);

    while (!iterationBounds.complete(iterations++)) {
      population = evolve(population);
      Double currentBest = population.get(0).getFitness();

      if (currentBest > best.getFitness()) {
        best = population.get(0);
      }

      // TODO test this with different parameters, because it seems to work fairly well.
      // also test without this delta adjustment
      deltaAdjustment();

      if (iterations % 1000 == 0) {
        logger.info(String.format(
            "Completed %s steps. Best fitness: %s (%.4f)", iterations, best.getFitness(),
            new Evaluator((SolutionInstance) best.getUnit().getValue()).totalGoal()));
      }
    }

    logger.info(String.format("Finishing with %s iterations.", iterations - 1));
    return population.get(0);
  }

  private void deltaAdjustment() {
    double f = population.stream().mapToDouble(UnitAndFitness::getFitness).sum();
    double delta =
        population.get(0).getFitness() / f - population.get(population.size() - 1).getFitness() / f;

    if (delta <= 0.03) {
      populationInfo.mutationProbability = Math.min(1.0,
          populationInfo.mutationProbability * 1.05);
      populationInfo.crossoverProbability = Math.max(0.3,
          populationInfo.crossoverProbability * 0.95);
    } else if (delta >= 0.05) {
      populationInfo.crossoverProbability = Math.min(1.0,
          populationInfo.crossoverProbability * 1.05);
      populationInfo.mutationProbability = Math.max(0.3,
          populationInfo.mutationProbability * 0.95);
    }
  }

  private List<UnitAndFitness<T>> evolve(List<UnitAndFitness<T>> population) {
    List<UnitAndFitness<T>> newPopulation = topN(population, populationInfo.elitism);
    while (newPopulation.size() < populationInfo.size) {
      Unit<T> child;
      if (shouldPerformAction(populationInfo.crossoverProbability)) {
        Pair<Unit<T>, Unit<T>> parents = selectParents(population);
        child = crossover.apply(parents.first, parents.second);
      } else {
        child = population.get(random.nextInt(population.size())).getUnit();
      }

      // this is just a test thing. idea was that, the closer a child is to the best unit, the lower
      // its mutation probability is. however it doesn't seem to work as well.
//      double childFitness = fitnessEvaluator.apply(child.getValue());
//      double childToBestRatio = childFitness / bestFitness;
//      double mutationMultiplier = 1;
      if (shouldPerformAction(populationInfo.mutationProbability)) {
        child = mutator.apply(child);
      }

      newPopulation.add(new UnitAndFitness<>(child, fitnessEvaluator.apply(child.value)));
    }

    return sortedByDescendingFitness(newPopulation);
  }

  boolean shouldPerformAction(double percentage) {
    return random.nextDouble() <= percentage;
  }

  private List<UnitAndFitness<T>> sortedByDescendingFitness(List<UnitAndFitness<T>> list) {
    return sortedByDescendingFitness(list.stream());
  }

  private List<UnitAndFitness<T>> sortedByDescendingFitness(Stream<UnitAndFitness<T>> stream) {
    return stream
        .sorted(Comparator.comparingDouble(UnitAndFitness<T>::getFitness).reversed())
        .collect(Collectors.toList());
  }

  private List<UnitAndFitness<T>> topN(List<UnitAndFitness<T>> population, int elitism) {
    return population.stream().limit(elitism).collect(Collectors.toList());
  }

  private Pair<Unit<T>, Unit<T>> selectParents(List<UnitAndFitness<T>> units) {
    double first = random.nextDouble();
    double second = random.nextDouble();
    double totalFitness = units.stream().map(AbstractMap.SimpleImmutableEntry::getValue)
        .reduce((a, b) -> a + b).get();

    List<Unit<T>> parents = new ArrayList<>();
    double prevFitness = 0;
    for (UnitAndFitness<T> unitAndFitness : units) {
      double fitness = unitAndFitness.getFitness() / totalFitness + prevFitness;

      if (first <= fitness && first >= prevFitness) {
        parents.add(unitAndFitness.getUnit());
      }

      if (second <= fitness && second >= prevFitness) {
        parents.add(unitAndFitness.getUnit());
      }

      prevFitness = fitness;
    }

    // e.g. if fitness values are NaN
    while (parents.size() < 2) {
      parents.add(units.get(random.nextInt(units.size())).getUnit());
    }

    return new Pair<>(parents.get(0), parents.get(1));
  }

}