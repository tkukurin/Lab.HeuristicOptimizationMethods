package genetic;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Functions {

    public static List<Function<List<Double>, Double>> getAll() {
        return Arrays.asList(
                Functions::function1,
                Functions::function3,
                Functions::function6,
                Functions::function7);
    }

    public static double function1(List<Double> vector) {
        return 100 * Math.pow(vector.get(1) - Math.pow(vector.get(0), 2), 2) + Math.pow(1 - vector.get(0), 2);
    }

    public static double function3(List<Double> vector) {
        return IntStream.range(0, vector.size())
                .mapToDouble(i -> (vector.get(i) - (i + 1)))
                .map(val -> Math.pow(val, 2))
                .sum();
    }

    public static double function6(List<Double> vector) {
        double squareSum = vector.stream().map(x -> Math.pow(x, 2)).mapToDouble(x -> x).sum();
        double top = Math.pow(Math.sin(Math.sqrt(squareSum)), 2) - 0.5;
        double bottom = Math.pow(1 + 0.001 * squareSum, 2);
        return 0.5 + top / bottom;
    }

    public static double function7(List<Double> vector) {
        double squareSum = vector.stream().map(x -> Math.pow(x, 2)).mapToDouble(x -> x).sum();
        double first = Math.pow(squareSum, 0.25);
        double second = 1 + Math.pow(Math.sin(50 * Math.pow(squareSum, 0.1)), 2);
        return first * second;
    }

}
