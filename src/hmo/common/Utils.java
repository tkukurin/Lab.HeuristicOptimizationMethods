package hmo.common;

import genetic.GeneticAlgorithm.Pair;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Utils {
  private Utils() {}

  public static <A, B> List<Pair<A, B>> zip(List<A> as, List<B> bs) {
    return IntStream.range(0, Math.min(as.size(), bs.size()))
        .mapToObj(i -> new Pair<>(as.get(i), bs.get(i)))
        .collect(Collectors.toList());
  }

  public static <T> T argmax(Function<T, Integer> f, T ... objects) {
    return Arrays.stream(objects).max(Comparator.comparingInt(f::apply)).get();
  }
}
