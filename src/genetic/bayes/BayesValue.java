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

  private final double variance0;
  private final double mean0;
  private final Queue<Double> means;
  private double mean;
  private double variance;
  private double step = 1;

  private final Random random = new Random();

  public BayesValue(double mean, double variance) {
    this.means = new LinkedList<>();
    this.mean = mean;
    this.variance = variance;
    this.mean0 = mean;
    this.variance0 = variance;
  }

  public BayesValue update(double value) {

    means.offer(value);
    if (means.size() > 20) {
      means.poll();
    }

    mean = (step / (step + 1)) * mean + 1 / (step + 1) * value;
    variance = variance0 / (step + 1);
    step++;
    return this;
  }

  public BayesValue updateBad(double value) {
    // "go away from the bad value"
//    mean = (step / (step + 1)) * mean + 1 / (step + 1) * (mean0 + mean0 - value);
//    variance = (variance + variance0 * (step / 2)) / (step + 1);
    means.offer(value);
    if (means.size() > 20) {
      means.poll();
    }

    mean = (step / (step + 1)) * mean + (mean - value) * 1 / (step + 1);
    variance = sampleMeanVariance(means).second * 0.9 + variance0 * 0.1;
    //variance * 0.9 + variance0 * 0.1;
    step++;
    return this;
  }

  public BayesValue update(List<Double> goodValues) {
    double currentMean = goodValues.stream()
        .mapToDouble(d -> d).average().getAsDouble();
    double currentVariance = goodValues.stream()
        .mapToDouble(f -> Math.pow(f - currentMean, 2)).sum();
//    mean += (currentMean - mean) * (1 - currentVariance);
//    variance = (variance + currentVariance) / 2;

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

  public static void main(String[] args) {
    BayesValue bayesValue = new BayesValue(0.05, 0.002);
    List<Double> vs = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      vs.add(bayesValue.sample());
      System.out.println(vs.get(vs.size() - 1));
    }
    System.out.println();
    System.out.println();
    System.out.println();
    bayesValue.update(vs);
    for (int i = 0; i < 20; i++) {
      System.out.println(bayesValue.sample());
    }
  }

}
