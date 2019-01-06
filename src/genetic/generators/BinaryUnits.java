package genetic.generators;

import genetic.Assignments.Parameters;
import genetic.GeneticAlgorithm;
import genetic.GeneticAlgorithm.Combinator;
import genetic.GeneticAlgorithm.FitnessEvaluator;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Mutator;
import genetic.GeneticAlgorithm.Unit;
import genetic.GeneticAlgorithm.UnitGenerator;
import genetic.Meta;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BinaryUnits implements Generator<Boolean[]> {

    public List<Unit<List<Boolean[]>>> evaluate(
            Function<List<Double>, Double> function,
            Random random,
            IterationBounds iterationBounds,
            Meta meta,
            List<Parameters> params) {

        UnitGenerator<List<Boolean[]>> randomBitSequenceSupplier = unitGenerator(random, meta);
        FitnessEvaluator<List<Boolean[]>> fitnessEvaluator = fitnessEvaluator(function, meta);
        Combinator<List<Boolean[]>> uniformCrossover = uniformCrossover(random, meta);
        Mutator<List<Boolean[]>> mutator = mutator(random, meta);

        List<Unit<List<Boolean[]>>> results = new ArrayList<>();
        for (Parameters parameters : params) {

            GeneticAlgorithm<List<Boolean[]>> geneticAlgorithm = new GeneticAlgorithm<>(
                    randomBitSequenceSupplier, parameters.populationInfo, iterationBounds, fitnessEvaluator,
                    uniformCrossover, mutator, random, Logger.getLogger("Iteration info"));

            GeneticAlgorithm.Unit<List<Boolean[]>> result = geneticAlgorithm.iterate();
            results.add(result);
        }

        return results;
    }

    public GeneticAlgorithm.Mutator<List<Boolean[]>> mutator(Random random, Meta meta) {
        return vector -> {
            ArrayList<Boolean[]> vals = new ArrayList<>();

            for (int i = 0; i < vector.size(); i++) {
                Boolean[] newEl = new Boolean[meta.nBits];
                for (int j = 0; j < meta.nBits; j++) {
                    newEl[j] = vector.get(i)[j] ^ random.nextBoolean();
                }
                vals.add(newEl);
            }

            return vals;
        };
    }

    public GeneticAlgorithm.Combinator<List<Boolean[]>> uniformCrossover(
            Random random, Meta meta) {
        return (fst, snd) -> {
            ArrayList<Boolean[]> vals = new ArrayList<>();

            for (int i = 0; i < fst.size(); i++) {
                Boolean[] fstEl = fst.get(i), sndEl = snd.get(i);
                Boolean[] newEl = new Boolean[meta.nBits];
                for (int j = 0; j < meta.nBits; j++) {
                    boolean bit = random.nextBoolean() ? fstEl[j] : sndEl[j];
                    newEl[j] = bit;
                }
                vals.add(newEl);
            }

            return vals;
        };
    }

    public GeneticAlgorithm.FitnessEvaluator<List<Boolean[]>> fitnessEvaluator(
            Function<List<Double>, Double> function, Meta meta) {
        return vector -> 1.0 / function.apply(binaryListToDoubles(meta, vector));
    }

    public GeneticAlgorithm.UnitGenerator<List<Boolean[]>> unitGenerator(
            Random random, Meta meta) {
        return new GeneticAlgorithm.UnitGenerator<>(() -> {
            ArrayList<Boolean[]> vals = new ArrayList<>();
            for (int i = 0; i < meta.dimension; i++) {
                Boolean[] booleans = new Boolean[meta.nBits];
                for (int j = 0; j < meta.nBits; j++) {
                    booleans[j] = random.nextBoolean();
                }
                vals.add(booleans);
            }
            return vals;
        });
    }

    @Override
    public List<Double> asDoubles(Meta meta, List<Boolean[]> value) {
        return BinaryUnits.binaryListToDoubles(meta, value);
    }

    public static List<Double> binaryListToDoubles(Meta meta, List<Boolean[]> vector) {
        return vector.stream().map(v -> toValue(meta, v, 0, v.length)).collect(Collectors.toList());
    }

    private static double toValue(Meta meta, Boolean[] bits, int start, int end) {
        double delta = meta.upperBound - meta.lowerBound;
        double step = delta / (Math.pow(2, end - start) - 1);
        double value = 0;
        for (int i = start; i < end; i++) {
            if (bits[i]) {
                value += step;
            }
            step = 2 * step;
        }
        return meta.lowerBound + value;
    }
}
