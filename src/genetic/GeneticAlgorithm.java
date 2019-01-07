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

    boolean complete(int numIterations, Deque<Double> lastErrors) {
      Double lastError = lastErrors.peekLast();
      return numIterations == this.numIterations
          || (lastError != null && lastError <= this.errorThreshold);
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
    double error = Double.MAX_VALUE;
    Deque<Double> errors = new ArrayDeque<>(5);

    while (!iterationBounds.complete(iterations++, errors)) {
      population = evolve(population);
      errors.offerLast(1 / population.get(0).getFitness());

      if (iterations % 10_000 == 0) {
        logger.info(String.format("Completed %s steps.", iterations));
      }
    }

    logger.info("step: " + iterations);
    logger.info("error: " + error);
    logger.info("unit: " + population.get(0));

    return population.get(0);
  }

  private List<UnitAndFitness<T>> evolve(List<UnitAndFitness<T>> population) {
    List<UnitAndFitness<T>> newPopulation = topN(population, populationInfo.elitism);

    for (int i = newPopulation.size(); i < populationInfo.size; i++) {
      Unit<T> child = population.get(random.nextInt(population.size())).getUnit();

      if (shouldPerformAction(populationInfo.crossoverProbability)) {
        Pair<Unit<T>, Unit<T>> parents = selectParents(population);
        child = crossover.apply(parents.first, parents.second);
      }

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
    return stream.sorted(Comparator.comparingDouble(UnitAndFitness<T>::getFitness).reversed())
        .collect(Collectors.toList());
  }

  private List<UnitAndFitness<T>> topN(List<UnitAndFitness<T>> population, int elitism) {
    return population.stream()
        .filter(kv -> Double.isFinite(kv.getFitness()))
        .limit(elitism).collect(Collectors.toList());
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