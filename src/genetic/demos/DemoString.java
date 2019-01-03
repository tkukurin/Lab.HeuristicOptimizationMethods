package genetic.demos;

import genetic.GeneticAlgorithm;
import genetic.GeneticAlgorithm.Combinator;
import genetic.GeneticAlgorithm.FitnessEvaluator;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Mutator;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.UnitGenerator;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DemoString {

  private static final String TARGET_WORD = "hello world";

  public static void main(String[] args) {
    String chars = "abcdefghijklmnopqrstuvwxyz ";
    Random random = new Random(0L);

    UnitGenerator<String> unitGenerator = new UnitGenerator<>(() -> {
      int length = random.nextInt(20);
      return IntStream.range(0, length)
          .mapToObj(i -> randomString(chars, random))
          .collect(Collectors.joining(""));
    });

    PopulationInfo populationInfo = new PopulationInfo(50, 5, 0.4, 0.6);
    IterationBounds iterationBounds = new IterationBounds(50_000, 1e-6);
    FitnessEvaluator<String> fitnessEvaluator = currentWord ->
        1.0 / editDistance(currentWord, TARGET_WORD);

    Combinator<String> crossover = (a, b) -> {
      StringBuilder stringBuilder = new StringBuilder();
      int length = random.nextDouble() < 0.5 ? a.length() : b.length();

      for (int i = 0; i < length; i++) {
        char c1 = i < a.length() ? a.charAt(i) : b.charAt(i);
        char c2 = i < b.length() ? b.charAt(i) : c1;
        stringBuilder.append(random.nextDouble() < 0.5 ? c1 : c2);
      }

      return stringBuilder.toString();
    };

    Mutator<String> mutator = string  -> {
      StringBuilder stringBuilder = new StringBuilder();
      for (int i = 0; i < string.length(); i++) {
        char c = string.charAt(i);

        if (random.nextDouble() < 0.3) {
          stringBuilder.append(randomString(chars, random));
        }
        if (random.nextDouble() < 0.85) {
          stringBuilder.append(c);
        }
      }
      return stringBuilder.toString();
    };

    GeneticAlgorithm<String> geneticAlgorithm = new GeneticAlgorithm<>(
            unitGenerator, populationInfo, iterationBounds, fitnessEvaluator, crossover, mutator,
            random, Logger.getLogger("Iteration info"));

    System.out.println(geneticAlgorithm.iterate().getValue());
  }

  public static int editDistance(String s1, String s2) {
    // simuliraj 2d array => usteda memorije
    int[] count = new int[s1.length() + 1];
    int[] newCount = new int[s1.length() + 1];

    for (int i = 0; i <= s1.length(); i++) {
      // ako je s2 prazan, onda imamo ukupno s1.length() insertionsa
      count[i] = i;
    }

    for (int i = 1; i <= s2.length(); i++) {
      // ako je s1 prazan, imat cemo ukupno s2.length() insertionsa
      newCount[0] = i;

      for (int j = 1; j <= s1.length(); j++) {
        int matching = s1.charAt(j - 1) != s2.charAt(i - 1) ? 1 : 0;
        newCount[j] = IntStream.of(count[j - 1] + matching, // zamjena
            newCount[j - 1] + 1,     // pomak "desno" = insertion u s2
            count[j] + 1             // pomak "dolje" = deletion iz s2
        ).min().getAsInt();
      }

      int[] tmp = count;
      count = newCount;
      newCount = tmp;
    }

    return count[count.length - 1];
  }

  private static String randomString(String chars, Random random) {
    return "" + chars.charAt(random.nextInt(chars.length()));
  }
}