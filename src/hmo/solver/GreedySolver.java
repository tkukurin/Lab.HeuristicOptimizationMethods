package hmo.solver;

import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.Random;

public class GreedySolver extends Solver {

  private Random random;

  public GreedySolver(Problem problem, Random random) {
    super(problem);
    this.random = random;
  }

  @Override
  public SolutionInstance solve() {
    SolutionInstance solutionInstance = new SolutionInstance(problem);

    while (!solutionInstance.getUnassignedVehicles().isEmpty()) {
      Vehicle nextVehicle = solutionInstance.randomUnassignedVehicle(random);
      for (Track track : problem.getTracks()) {
        if (solutionInstance.canAssign(nextVehicle, track)) {
          solutionInstance.assign(nextVehicle, track);
        }
      }
    }

    return solutionInstance;
  }
}
