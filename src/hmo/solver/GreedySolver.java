package hmo.solver;

import hmo.Evaluator;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;

import java.util.Collection;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Logger;

public class GreedySolver extends Solver {

  private static final Logger LOG = Logger.getLogger(GreedySolver.class.toString());

  private Random random;
  private SolutionInstance solutionInstance;

  public GreedySolver(Problem problem, Random random) {
    this(new SolutionInstance(problem), random);
  }

  public GreedySolver(SolutionInstance solutionInstance, Random random) {
    super(solutionInstance.getProblem());
    this.random = random;
    this.solutionInstance = solutionInstance;
  }

  @Override
  public SolutionInstance solve() {
    solutionInstance.resetVehiclePool();

    LOG.info("Starting greedy algorithm.");
    while (!solutionInstance.getVehiclePool().isEmpty()) {
      double val = 0.0;
      Track best = null;
      Vehicle nextVehicle = solutionInstance.pollUnusedVehicle(random);

      for (Track track : problem.getTracks()) {
        if (solutionInstance.canAssign(nextVehicle, track)) {
          solutionInstance.assign(nextVehicle, track);
          Evaluator evaluator = new Evaluator(solutionInstance);
          if (evaluator.totalGoal() > val) {
            val = evaluator.totalGoal();
            best = track;
          }
          solutionInstance.removeVehicle(track, nextVehicle);
        }
      }
      solutionInstance.assign(nextVehicle, best);
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
