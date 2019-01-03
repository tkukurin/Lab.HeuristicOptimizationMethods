package genetic;

import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.Unit;
import genetic.generators.BinaryUnits;
import genetic.generators.Generator;
import genetic.generators.RealValuedUnits;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class Assignments {

  public static class Parameters {

    public final PopulationInfo populationInfo;

    public Parameters(PopulationInfo populationInfo) {
      this.populationInfo = populationInfo;
    }

    @Override
    public String toString() {
      return "Parameters{" +
          "populationInfo=" + populationInfo +
          '}';
    }
  }

  public static void main(String[] args) {
    Random random = new Random(0L);
    ExecutorService executor = Executors
        .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    List<Callable<Void>> runnables = Arrays.asList(
        () -> zad1(random, new BinaryUnits()),
        () -> zad2(random, new BinaryUnits()),
        () -> zad3(random),
        () -> zad4(random),
        () -> zad5(random, new BinaryUnits()));

    runnables.forEach(executor::submit);
  }

  private static <T> Void zad1(Random random, Generator<T> generator) throws IOException {
    IterationBounds iterationBounds = new IterationBounds(25_000, 1e-6);

    Path path = Paths.get("zad1.txt");
    path = Files.exists(path) ? path : Files.createFile(path);
    PrintStream out = new PrintStream(path.toFile());

    List<Integer> dimensions = Arrays.asList(2, 5, 2, 2);
    List<Function<List<Double>, Double>> fs = Functions.getAll();

    for (int i = 0; i < fs.size(); i++) {
      Function<List<Double>, Double> function = fs.get(i);
      Meta meta = new Meta(20, -50, 150, dimensions.get(i));
      List<Parameters> parameters = Arrays.asList(
          new Parameters(new PopulationInfo(10, 2, 0.1, 0.68)),
          new Parameters(new PopulationInfo(20, 4, 0.2, 0.75)),
          new Parameters(new PopulationInfo(30, 4, 0.2, 0.75)),
          new Parameters(new PopulationInfo(100, 4, 0.35, 0.85)),
          new Parameters(new PopulationInfo(150, 4, 0.04, 0.75)),
          new Parameters(new PopulationInfo(200, 5, 0.5, 0.9))
      );

      List<Unit<List<T>>> results = generator.evaluate(
          function, random, iterationBounds, meta, parameters);

      out.format("\n\nResults (function %d):\n", i);
      outputResults(generator, out, function, meta, parameters.get(i), results);
    }
    return null;
  }

  private static <T> Void zad2(Random random, Generator<T> generator) throws IOException {
    IterationBounds iterationBounds = new IterationBounds(25_000, 1e-6);

    List<Function<List<Double>, Double>> fs = Arrays
        .asList(Functions::function6, Functions::function7);
    List<Integer> dimensions = Arrays.asList(1, 3, 6, 10);

    Path path = Paths.get("zad2.txt");
    path = Files.exists(path) ? path : Files.createFile(path);
    PrintStream out = new PrintStream(path.toFile());

    for (int i = 0; i < fs.size(); i++) {
      for (int dimension : dimensions) {
        Function<List<Double>, Double> function = fs.get(i);
        Meta meta = new Meta(20, -50, 150, dimension);
        List<Parameters> parameters = Arrays.asList(
            new Parameters(new PopulationInfo(10, 2, 0.1, 0.68)),
            new Parameters(new PopulationInfo(20, 4, 0.2, 0.75)),
            new Parameters(new PopulationInfo(100, 4, 0.35, 0.85)),
            new Parameters(new PopulationInfo(200, 5, 0.5, 0.9)),
            new Parameters(new PopulationInfo(150, 4, 0.04, 0.75))
        );

        List<Unit<List<T>>> results = generator.evaluate(
            function, random, iterationBounds, meta, parameters);

        out.format("\n\nResults (function %d):\n", i);
        outputResults(generator, out, function, meta, parameters.get(i), results);
      }
    }
    return null;
  }

  private static Void zad3(Random random) throws IOException {
    IterationBounds iterationBounds = new IterationBounds(100_000, 1e-6);

    List<Function<List<Double>, Double>> fs = Arrays.asList(
        Functions::function6, Functions::function7);
    List<Integer> dimensions = Arrays.asList(3, 6);

    Path path = Paths.get("zad3.txt");
    path = Files.exists(path) ? path : Files.createFile(path);
    PrintStream out = new PrintStream(path.toFile());

    BinaryUnits binaryUnits = new BinaryUnits();
    RealValuedUnits realValuedUnits = new RealValuedUnits();

    for (int i = 0; i < fs.size(); i++) {
      for (int dimension : dimensions) {
        Function<List<Double>, Double> function = fs.get(i);
        Meta meta = new Meta(21, -50, 150, dimension);
        List<Parameters> parameters = Arrays.asList(
            new Parameters(new PopulationInfo(10, 2, 0.1, 0.68)),
            new Parameters(new PopulationInfo(20, 4, 0.2, 0.75)),
            new Parameters(new PopulationInfo(100, 4, 0.35, 0.85)),
            new Parameters(new PopulationInfo(200, 5, 0.5, 0.9)),
            new Parameters(new PopulationInfo(150, 4, 0.04, 0.75))
        );

        List<Unit<List<Boolean[]>>> resultsBinary = binaryUnits.evaluate(
            function, random, iterationBounds, meta, parameters);
        List<Unit<List<Double>>> resultsReal = realValuedUnits.evaluate(
            function, random, iterationBounds, meta, parameters);

        out.format("\n\nResults (function %d, binary-encoded):\n", i);
        outputResults(binaryUnits, out, function, meta, parameters.get(i), resultsBinary);

        out.format("\n\nResults (function %d, real-valued):\n", i);
        outputResults(realValuedUnits, out, function, meta, parameters.get(i), resultsReal);
      }
    }
    return null;
  }

  private static Void zad4(Random random) throws IOException {
    IterationBounds iterationBounds = new IterationBounds(25_000, 1e-6);

    Path path = Paths.get("zad4.txt");
    path = Files.exists(path) ? path : Files.createFile(path);
    PrintStream out = new PrintStream(path.toFile());

    Function<List<Double>, Double> f = Functions::function6;

    List<Integer> populationSizes = Arrays.asList(30, 50, 100, 200);
    List<Double> mutationProbabilities = Arrays.asList(0.1, 0.3, 0.6, 0.9);
    RealValuedUnits generator = new RealValuedUnits();
    Meta meta = new Meta(20, -50, 150, 2);

    for (Integer populationSize : populationSizes) {
      for (Double mutationProbability : mutationProbabilities) {
        out.printf(
            "population size %d, mutation probability %f\n",
            populationSize, mutationProbability);

        List<Parameters> parameters = Collections.singletonList(new Parameters(
            new PopulationInfo(populationSize, 2, mutationProbability, 0.68)));
        List<Unit<List<Double>>> results = generator.evaluate(
            f, random, iterationBounds, meta, parameters);

        outputResults(generator, out, f, meta, parameters.get(0), results);
      }
    }
    return null;
  }


  private static <T> Void zad5(Random random, Generator<T> generator) throws IOException {
    IterationBounds iterationBounds = new IterationBounds(25_000, 1e-6);

    Path path = Paths.get("zad5.txt");
    path = Files.exists(path) ? path : Files.createFile(path);
    PrintStream out = new PrintStream(path.toFile());

    List<Integer> dimensions = Arrays.asList(2, 5, 2, 2);

    // razlicite f-je cilja: f i 2^f
    Function<List<Double>, Double> originalFunction = Functions::function6;
    List<Function<List<Double>, Double>> fitnessFunctions = Arrays.asList(
        originalFunction,
        vector -> Math.pow(2, Functions.function6(vector))
    );

    for (int i = 0; i < fitnessFunctions.size(); i++) {
      Function<List<Double>, Double> fitnessFunction = fitnessFunctions.get(i);
      Meta meta = new Meta(20, -50, 150, dimensions.get(i));
      List<Parameters> parameters = Arrays.asList(
          new Parameters(new PopulationInfo(10, 2, 0.1, 0.68)),
          new Parameters(new PopulationInfo(20, 4, 0.2, 0.75)),
          new Parameters(new PopulationInfo(100, 4, 0.35, 0.85)),
          new Parameters(new PopulationInfo(200, 5, 0.5, 0.9)),
          new Parameters(new PopulationInfo(150, 4, 0.04, 0.75))
      );

      List<Unit<List<T>>> results = generator.evaluate(
          fitnessFunction, random, iterationBounds, meta, parameters);

      out.format("==\nResults (function %d):\n", i);
      outputResults(generator, out, originalFunction, meta, parameters.get(i), results);
    }

    return null;
  }

  private static <T> void outputResults(Generator<T> generator, PrintStream out,
      Function<List<Double>, Double> function, Meta meta, Parameters parameters,
      List<Unit<List<T>>> results) {
    results.stream().map(r -> generator.asDoubles(meta, r.value)).forEach(values -> {
      out.println("==");
      out.println("point: " + values);
      out.println("function value: " + function.apply(values));
      out.println("parameters: " + parameters);
    });
  }
}
