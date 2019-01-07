package hmo.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTest {

  @Test
  void testSetDifference() {
    Collection<Integer> fst = Arrays.asList(1, 2, 3);
    Collection<Integer> snd = Arrays.asList(2, 3, 4, 5);

    Set<Integer> diff = Utils.difference(fst, snd);

    Assertions.assertEquals(diff.size(), 1);
    Assertions.assertArrayEquals(
        new int[] { diff.stream().findFirst().get() },
        new int[] { 1 });
  }
}
