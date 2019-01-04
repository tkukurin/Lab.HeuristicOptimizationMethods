package hmo.solver;

import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.Random;
import java.util.logging.Logger;

public class GreedySolver extends Solver {

  private static final Logger LOG = Logger.getLogger(GreedySolver.class.toString());

  private Random random;
  int assignedCounter;

  public GreedySolver(Problem problem, Random random) {
    super(problem);
    this.random = random;
  }

  @Override
  public SolutionInstance solve() {
    SolutionInstance solutionInstance = new SolutionInstance(problem);

    LOG.info("Starting greedy algorithm.");
    while (!solutionInstance.getUnassignedVehicles().isEmpty()) {
      Vehicle nextVehicle = solutionInstance.randomUnassignedVehicle(random);
      for (Track track : problem.getTracks()) {
        if (solutionInstance.canAssign(nextVehicle, track)) {
          solutionInstance.assign(nextVehicle, track);
          assignedCounter++;
          break;
        }
      }
    }

    LOG.info("Completed greedy algorithm.");
    LOG.info(String.format("Assigned %s cars.", assignedCounter));
    return solutionInstance;
  }
}
