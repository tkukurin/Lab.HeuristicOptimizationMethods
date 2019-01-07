package genetic;

import genetic.common.Unit;
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
    double errorThreshold;

    public IterationBounds(int numIterations, double errorThreshold) {
      this.numIterations = numIterations;
      this.errorThreshold = errorThreshold;
    }

    boolean complete(int numIterations, double error) {
      return numIterations == this.numIterations || error <= this.errorThreshold;
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

  public class UnitKeyFitnessValue extends AbstractMap.SimpleImmutableEntry<Unit<T>, Double> {
    public UnitKeyFitnessValue(Unit<T> key, Double value) {
      super(key, value);
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
  private List<UnitKeyFitnessValue> population;
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
        .map(unit -> new UnitKeyFitnessValue(unit, fitnessEvaluator.apply(unit.value))));
    this.logger = logger;
  }

  public UnitKeyFitnessValue iterate() {
    int iterations = 0;
    double error = Double.MAX_VALUE;

    while (!iterationBounds.complete(iterations++, error)) {
      population = evolve(population);
      double newError = 1 / population.get(0).getValue();

      if (newError < error) {
        logger.info("step: " + iterations);
        logger.info("error: " + newError);
        logger.info("unit: " + population.get(0));
      }

      error = newError;
    }

    logger.info("step: " + iterations);
    logger.info("error: " + error);
    logger.info("unit: " + population.get(0));

    return population.get(0);
  }

  private List<UnitKeyFitnessValue> evolve(List<UnitKeyFitnessValue> population) {
    List<UnitKeyFitnessValue> newPopulation = topN(population, populationInfo.elitism);

    for (int i = populationInfo.elitism; i < populationInfo.size; i++) {
      Unit<T> child = population.get(random.nextInt(population.size())).getKey();

      if (shouldPerformAction(populationInfo.crossoverProbability)) {
        Pair<Unit<T>, Unit<T>> parents = selectParents(population);
        child = crossover.apply(parents.first, parents.second);
      }

      if (shouldPerformAction(populationInfo.mutationProbability)) {
        child = mutator.apply(child);
      }

      newPopulation.add(new UnitKeyFitnessValue(child, fitnessEvaluator.apply(child.value)));
    }

    return sortedByDescendingFitness(newPopulation);
  }

  boolean shouldPerformAction(double percentage) {
    return random.nextDouble() <= percentage;
  }

  private List<UnitKeyFitnessValue> sortedByDescendingFitness(List<UnitKeyFitnessValue> list) {
    return sortedByDescendingFitness(list.stream());
  }

  private List<UnitKeyFitnessValue> sortedByDescendingFitness(Stream<UnitKeyFitnessValue> stream) {
    return stream.sorted(Comparator.<UnitKeyFitnessValue, Double>comparing(Map.Entry::getValue).reversed())
        .collect(Collectors.toList());
  }

  private List<UnitKeyFitnessValue> topN(List<UnitKeyFitnessValue> population, int elitism) {
    return population.stream()
        .filter(kv -> Double.isFinite(kv.getValue()))
        .limit(elitism).collect(Collectors.toList());
  }

  private Pair<Unit<T>, Unit<T>> selectParents(List<UnitKeyFitnessValue> units) {
    double first = random.nextDouble();
    double second = random.nextDouble();
    double totalFitness = units.stream().map(AbstractMap.SimpleImmutableEntry::getValue)
        .reduce((a, b) -> a + b).get();

    List<Unit<T>> parents = new ArrayList<>();
    double prevFitness = 0;
    for (UnitKeyFitnessValue unitKeyFitnessValue : units) {
      double fitness = unitKeyFitnessValue.getValue() / totalFitness + prevFitness;

      if (first <= fitness && first >= prevFitness) {
        parents.add(unitKeyFitnessValue.getKey());
      }

      if (second <= fitness && second >= prevFitness) {
        parents.add(unitKeyFitnessValue.getKey());
      }

      prevFitness = fitness;
    }

    // e.g. if fitness values are NaN
    while (parents.size() < 2) {
      parents.add(units.get(random.nextInt(units.size())).getKey());
    }

    return new Pair<>(parents.get(0), parents.get(1));
  }

}