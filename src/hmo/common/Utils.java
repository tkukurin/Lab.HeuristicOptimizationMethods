package hmo.common;

import genetic.common.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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

  public static <T> Set<T> difference(Collection<T> fst, Collection<T> snd) {
    Set<T> diff = new HashSet<>(fst);
    diff.removeAll(snd);
    return diff;
  }

  public interface ThrowableSupplier<T> {
    T get() throws Exception;
  }

  public static <T> T unchecked(ThrowableSupplier<T> function) {
    try {
      return function.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T randomElement(List<T> list, Random random) {
    if (list.isEmpty()) {
      return null;
    }
    return list.get(random.nextInt(list.size()));
  }

  public static <T> boolean containsAny(Set<T> set, Collection<T> objects) {
    for (T object : objects) {
      if (set.contains(object)) {
        return true;
      }
    }
    return false;
  }
}
