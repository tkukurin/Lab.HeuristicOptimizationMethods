package genetic.generator;

import genetic.GAMeta;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public abstract class TabooGenerator extends SolutionInstanceGenerator {
  private static final int ITERATIONS_BEFORE_GIVING_UP = 100;

  private Set<SolutionInstance> recentInstances;
  private Queue<SolutionInstance> recentInstancesQueue;
  private final int tabooSize;

  public TabooGenerator(Random random, Problem problem, GAMeta meta) {
    super(random, problem, meta);
    this.recentInstances = new HashSet<>();
    this.recentInstancesQueue = new LinkedList<>();
    this.tabooSize = 50;

    for (int i = 0; i < tabooSize; i++) {
      this.recentInstancesQueue.add(new SolutionInstance(problem));
    }
  }

  @Override
  SolutionInstance crossover(SolutionInstance s1, SolutionInstance s2) {
    return taboo(
        super.crossover(s1, s2),
        si -> super.crossover(si, random.nextBoolean() ? s1 : s2));
  }

  @Override
  SolutionInstance mutator(SolutionInstance solutionInstance) {
    return taboo(super.mutator(solutionInstance), si -> super.mutator(solutionInstance));
  }

  private SolutionInstance taboo(
      SolutionInstance initialInstance,
      Function<SolutionInstance, SolutionInstance> function) {
    int iteration = 0;
    while (recentInstances.contains(initialInstance) && iteration++ < ITERATIONS_BEFORE_GIVING_UP) {
      initialInstance = function.apply(initialInstance);
    }
    recentInstances.add(initialInstance);
    recentInstancesQueue.offer(initialInstance);
    recentInstances.remove(recentInstancesQueue.poll());
    return initialInstance;
  }
}
