package genetic;

import genetic.bayes.BayesValue;
import genetic.common.IterationBounds;
import genetic.common.LinearRegression;
import genetic.common.Pair;
import genetic.common.PopulationInfo;
import genetic.common.Unit;
import hmo.Evaluator;
import hmo.instance.SolutionInstance;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Maximizes given function. */
public class GeneticAlgorithm<T> {

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

  private BayesValue upper;
  private BayesValue lower;
  private final PopulationInfo initialPopulation;

  private LinearRegression lrLo = null;
  private LinearRegression lrHi = null;


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
    this.initialPopulation = new PopulationInfo(populationInfo);

    this.lower = new BayesValue(0.05, 0.05);
    this.upper = new BayesValue(0.15, 0.05);
  }

  private List<Double> deltas = new ArrayList<>();
  List<Double> los = new ArrayList<>();
  List<Double> his = new ArrayList<>();

  public UnitAndFitness<T> iterate() {
    int iterations = 0;
    UnitAndFitness<T> best = new UnitAndFitness<>(new Unit<>(null), 0.0);

    int improvement = 0;
    double oldPopulationValues = //population.get(0).getFitness();
        population.stream()
        .mapToDouble(UnitAndFitness::getFitness)
        .filter(Double::isFinite)
        .sum();

    while (!iterationBounds.isComplete(iterations++)) {
      double loSample = lower.sample();
      double hiSample = upper.sample();

      population = evolve(population);
      double currentPopulationValues = // population.get(0).getFitness();
          population.stream().mapToDouble(UnitAndFitness::getFitness)
              .filter(Double::isFinite).sum();
      if (lrLo != null && currentPopulationValues > oldPopulationValues) {
        deltas.add(oldPopulationValues - currentPopulationValues);
        los.add(loSample);
        his.add(hiSample);
      } else if (lrLo == null && currentPopulationValues > oldPopulationValues) {
//        improvement = iterations;
        lower.update(loSample);
        upper.update(hiSample);
//        bayesUpdatesLo.add(loSample);
//        bayesUpdatesHi.add(hiSample);

//        if (bayesUpdatesHi.size() == 100) {
//          lower.update(bayesUpdatesLo);
//          upper.update(bayesUpdatesHi);
//          bayesUpdatesLo = new ArrayList<>();
//          bayesUpdatesHi = new ArrayList<>();
//        }
      } else {
//        lower.updateBad(loSample);
//        upper.updateBad(hiSample);
      }

//      if (iterations - 5000 > improvement) {
//        System.out.println("reset");
//        double lo = lower.sample();
//        double up = upper.sample();
//        lower = new BayesValue((lo + 0.05) / 2, (lo + 0.05) / 2);
//        upper = new BayesValue((up + 0.15) / 2, (lo + up) / 2);
//        improvement = iterations;
//      }

      if (iterations % 5000 == 0) {
        double[] xsLo = new double[los.size()];
        double[] xsHi = new double[his.size()];
        double[] ys = new double[his.size()];
        for (int i = 0; i < los.size(); i++) {
          xsLo[i] = los.get(i);
          xsHi[i] = his.get(i);
          ys[i] = deltas.get(i);
        }

        lrLo = new LinearRegression(ys, xsLo);
        lrHi = new LinearRegression(ys, xsHi);

        // start recording from the beginning
        los = new ArrayList<>();
        his = new ArrayList<>();
        deltas = new ArrayList<>();
      }

      oldPopulationValues = currentPopulationValues;
      double currentBest = population.get(0).getFitness();

      if (currentBest > best.getFitness()) {
        best = population.get(0);
        improvement = iterations;
      }

      // TODO test this with different parameters, because it seems to work fairly well.
      // also test without this delta adjustment
      deltaAdjustment(loSample, hiSample);

      if (iterations % 1000 == 0) {
        logger.info(String.format(
            "Completed %s steps. Best fitness: %s (%.4f)", iterations, best.getFitness(),
            new Evaluator((SolutionInstance) best.getUnit().getValue()).totalGoal()));
      }
    }

    logger.info(String.format("Finishing with %s iterations.", iterations - 1));
    return population.get(0);
  }

  private void deltaAdjustment(double lowerBound, double upperBound) {
    double f = population.stream().mapToDouble(UnitAndFitness::getFitness).sum();
    double delta =
        population.get(0).getFitness() / f - population.get(population.size() - 1).getFitness() / f;

    deltas.add(delta);

    if (lrLo != null) {
      lowerBound = lrLo.predict(delta) + random.nextGaussian() * 0.03;
      upperBound = lrHi.predict(delta) + random.nextGaussian() * 0.03;
    }

    // if all fitnesses are too similar, then increase mutation probability and decrease crossover.
    // otherwise, decrease mutation probability and increase crossover
    if (delta <= lowerBound) {
      populationInfo.mutationProbability = Math.min(1.0,
          populationInfo.mutationProbability * 1.05);
      populationInfo.crossoverProbability = Math.max(0.3,
          populationInfo.crossoverProbability * 0.95);
    } else if (delta >= upperBound) {
      populationInfo.crossoverProbability = Math.min(1.0,
          populationInfo.crossoverProbability * 1.05);
      populationInfo.mutationProbability = Math.max(0.3,
          populationInfo.mutationProbability * 0.95);
    }

    los.add(lowerBound);
    his.add(upperBound);
  }

  private List<UnitAndFitness<T>> evolve(List<UnitAndFitness<T>> population) {
    List<UnitAndFitness<T>> newPopulation = topN(population, populationInfo.elitism);
    double mutate = populationInfo.mutationProbability;

    while (newPopulation.size() < populationInfo.size) {
      Unit<T> child;
      if (shouldPerformAction(populationInfo.crossoverProbability)) {
        Pair<Unit<T>, Unit<T>> parents = selectParents(population);
        child = crossover.apply(parents.first, parents.second);
      } else {
        child = population.get(random.nextInt(population.size())).getUnit();
      }

      if (shouldPerformAction(mutate)) {
        child = mutator.apply(child);
      }

      double currentFitness = fitnessEvaluator.apply(child.value);
      newPopulation.add(new UnitAndFitness<>(child, currentFitness));

      // TODO this also seems to work well, but try without it.
      mutate = Math.min(1.0, mutate * 1.05);
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