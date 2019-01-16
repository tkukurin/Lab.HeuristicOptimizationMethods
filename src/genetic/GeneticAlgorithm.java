package genetic;

import genetic.bayes.BayesValue;
import genetic.common.*;
import hmo.Evaluator;
import hmo.instance.SolutionInstance;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Maximizes given function. */
public class GeneticAlgorithm<T> {

    private final int ONE_MINUTE = 1 * 60 * 1000;
    private final int FIVE_MINUTES = 5 * 60 * 1000;
    private final int TEN_MINUTES = 10 * 60 * 1000;
    private final int ITERATION_WITHOUT_IMPROVEMENT_LIMIT = 20000;

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

  private BayesValue bayesHi;
  private BayesValue bayesLo;
  private final PopulationInfo initialPopulation;

  private LinearRegression regressionLo = null;
  private LinearRegression regressionHi = null;

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

    this.bayesLo = new BayesValue(0.05, 0.05);
    this.bayesHi = new BayesValue(0.15, 0.05);
  }

  private List<Double> deltas = new ArrayList<>();
  private List<Double> los = new ArrayList<>();
  private List<Double> his = new ArrayList<>();

  public UnitAndFitness<T> iterate() {
    // TODO these are parameters you can change
    boolean doLinearRegression = false;
    boolean sampleDeltaAdjustment = false;

    int iterations = 0;
    int lastImprovementIteration = 0;
      //double lastImprovement = 0.;
    UnitAndFitness<T> best = new UnitAndFitness<>(new Unit<>(null), 0.0);

    double oldPopulationValues =
        population.stream()
        .mapToDouble(UnitAndFitness::getFitness)
        .filter(Double::isFinite)
        .sum();

    double loSample = bayesLo.sample();
    double hiSample = bayesHi.sample();
      //while (!iterationBounds.timeRanOut(ONE_MINUTE)) {
      //while(((iterations - lastImprovementIteration) <= ITERATION_WITHOUT_IMPROVEMENT_LIMIT) && (lastImprovement == 0 || lastImprovement > 1)) {
      while (!iterationBounds.timeRanOut(FIVE_MINUTES)) {
          iterations++;
      double oldPopulationDelta = deltaBestWorst();

      population = evolve(population);
      double currentPopulationValues =
          population.stream().mapToDouble(UnitAndFitness::getFitness)
              .filter(Double::isFinite).sum();
      if (currentPopulationValues > oldPopulationValues) {
        if (regressionLo == null) {
          lastImprovementIteration = iterations;
          bayesLo.update(loSample);
          bayesHi.update(hiSample);
        }

        if (doLinearRegression) {
          deltas.add(oldPopulationDelta);
          los.add(loSample);
          his.add(hiSample);
        }
      } else {
        bayesLo.updateBad(loSample);
        bayesHi.updateBad(hiSample);
      }

      if (iterations - 1000 > lastImprovementIteration) {
        double lo = bayesLo.sample();
        double up = bayesHi.sample();
        bayesLo = new BayesValue((lo + bayesLo.getMean0()) / 2, (lo + bayesLo.getMean0()) / 2);
        bayesHi = new BayesValue((up + bayesHi.getMean0()) / 2, (lo + up) / 2);
        regressionLo = null;
        regressionHi = null;
        lastImprovementIteration = iterations;
      }

      if (doLinearRegression && iterations % 3000 == 0) {
        double[] ysLo = new double[los.size()];
        double[] ysHi = new double[his.size()];
        double[] xsDeltas = new double[his.size()];
        for (int i = 0; i < los.size(); i++) {
          ysLo[i] = los.get(i);
          ysHi[i] = his.get(i);
          xsDeltas[i] = deltas.get(i);
        }

        regressionLo = new LinearRegression(xsDeltas, ysLo);
        regressionHi = new LinearRegression(xsDeltas, ysHi);

        // start recording from the beginning
        los = new ArrayList<>();
        his = new ArrayList<>();
        deltas = new ArrayList<>();
      }

      oldPopulationValues = currentPopulationValues;
      double currentBest = population.get(0).getFitness();

      if (currentBest > best.getFitness()) {
          //lastImprovement = currentBest - best.getFitness();
        best = population.get(0);
        lastImprovementIteration = iterations;
      }

      // TODO test this with different parameters, because it seems to work fairly well.
      // also test without this delta adjustment
      if (sampleDeltaAdjustment) {
        double newPopulationDelta = deltaBestWorst();
        loSample = regressionLo == null ? bayesLo.sample() :
            regressionLo.predict(newPopulationDelta) + random.nextGaussian() * bayesLo
                .getVariance0();
        hiSample = regressionHi == null ? bayesHi.sample()
            : regressionHi.predict(newPopulationDelta) + random.nextGaussian() * bayesHi
                .getVariance0();
        deltaAdjustment(loSample, hiSample);
      } else {
        deltaAdjustment(0.03, 0.05);
      }

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
    double deltaBestWorstNormalized = deltaBestWorst();

    // if all fitnesses are too similar, then increase mutation probability and decrease crossover.
    // otherwise, decrease mutation probability and increase crossover
    if (deltaBestWorstNormalized <= lowerBound) {
      populationInfo.mutationProbability = Math.min(1.0,
          populationInfo.mutationProbability * 1.05);
      populationInfo.crossoverProbability = Math.max(0.3,
          populationInfo.crossoverProbability * 0.95);
    } else if (deltaBestWorstNormalized >= upperBound) {
      populationInfo.crossoverProbability = Math.min(1.0,
          populationInfo.crossoverProbability * 1.05);
      populationInfo.mutationProbability = Math.max(0.3,
          populationInfo.mutationProbability * 0.95);
    }
  }

  private double deltaBestWorst() {
    double normalization = population.stream().mapToDouble(UnitAndFitness::getFitness).sum();
    return (population.get(0).getFitness() - population.get(population.size() - 1).getFitness())
        / normalization;
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
      mutate = Math.min(1.0, mutate * 1.1);
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