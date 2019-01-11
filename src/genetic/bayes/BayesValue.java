package genetic.bayes;

import genetic.common.Pair;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;

public class BayesValue {

  public double getMean0() {
    return mean0;
  }

  public double getVariance0() {
    return variance0;
  }

  private final double variance0;
  private final double mean0;
  private double mean;
  private double variance;
  private double step = 1;

  private final Random random = new Random();

  public BayesValue(double mean, double variance) {
    this.mean = mean;
    this.variance = variance;
    this.mean0 = mean;
    this.variance0 = variance;
  }

  public BayesValue update(double value) {
    mean = (step / (step + 1)) * mean + 1 / (step + 1) * value;
    variance = variance0 / (step + 1);
    step++;
    return this;
  }

  public BayesValue updateBad(double value) {
    // "go away from the bad value"
    mean = (step / (step + 1)) * mean + (mean - value) * 1 / (step + 1);
    variance = variance * 0.9 + variance0 * 0.1;
    step++;
    return this;
  }

  public BayesValue update(List<Double> goodValues) {
    double currentMean = goodValues.stream()
        .mapToDouble(d -> d).average().getAsDouble();
    double currentVariance = goodValues.stream()
        .mapToDouble(f -> Math.pow(f - currentMean, 2)).sum();
    // bayes rule update:
    int sampleSize = goodValues.size();
    mean = (currentVariance * mean + sampleSize * currentVariance * currentMean) /
        (variance + variance0);
    variance = (currentVariance * variance) / (sampleSize * currentVariance + variance);
    return this;
  }

  private Pair<Double, Double> sampleMeanVariance(Collection<Double> values) {
    double currentMean = values.stream()
        .mapToDouble(d -> d).average().getAsDouble();
    double currentVariance = values.stream()
        .mapToDouble(f -> Math.pow(f - currentMean, 2)).sum();
    return new Pair<>(currentMean, currentVariance);
  }

  public double sample() {
    return random.nextGaussian() * variance + mean;
  }
}
