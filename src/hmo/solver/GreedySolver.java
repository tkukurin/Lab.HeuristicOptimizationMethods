package hmo.solver;

import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

public class GreedySolver extends Solver {

  private static final Logger LOG = Logger.getLogger(GreedySolver.class.toString());

  private Random random;

  public GreedySolver(Problem problem, Random random) {
    super(problem);
    this.random = random;
  }

  @Override
  public SolutionInstance solve() {
    SolutionInstance solutionInstance = new SolutionInstance(problem);

    LOG.info("Starting greedy algorithm.");
    while (!solutionInstance.getVehiclePool().isEmpty()) {
      Vehicle nextVehicle = solutionInstance.pollUnusedVehicle(random);

      for (Track track : problem.getTracks()) {
        if (solutionInstance.canAssign(nextVehicle, track)) {
          solutionInstance.assign(nextVehicle, track);
          break;
        }
      }
    }

    LOG.info("Completed greedy algorithm.");
    LOG.info(String.format("Assigned %s cars.", solutionInstance.getVehicleInstances().size()));
    solutionInstance.resetVehiclePool();
    return solutionInstance;
  }

  // currently unused.
  private void adjust(SolutionInstance solutionInstance) {
    Stack<Integer> stack = new Stack<>();
    stack.push(0);

    while (!stack.isEmpty()) {
      int top = stack.pop();
      Collection<Integer> blocks = problem.getBlocks(top);
      for (int other : problem.getBlocks(top)) {

      }
    }
  }
}
