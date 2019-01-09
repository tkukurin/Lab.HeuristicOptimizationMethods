package genetic.common;

import genetic.GAMeta;
import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.problem.Problem;
import hmo.problem.Vehicle;
import hmo.solver.GreedyOrderedSolver;
import java.util.List;
import java.util.Random;

public class RandomizedGenerator extends SolutionInstanceGenerator {

  public RandomizedGenerator(Random random, Problem problem, GAMeta meta) {
    super(random, problem, meta);
  }

  @Override
  SolutionInstance unitGenerator() {
//    return new GreedyOrderedSolver(problem, random).solve();
    return new SolutionInstance(problem);
  }

  @Override
  SolutionInstance crossoverImpl(SolutionInstance s1, SolutionInstance s2) {
    double s1assigned = s1.getAssignedVehicles().size();
    double s2Assigned = s2.getAssignedVehicles().size();
    return coinFlip(s1assigned / (s1assigned + s2Assigned)) ? s1 : s2;
  }

  @Override
  SolutionInstance mutatorImpl(SolutionInstance solutionInstance) {
    solutionInstance.resetVehiclePool();

    Vehicle vehicle = solutionInstance.pollUnusedVehicle(random);
    double percentAssignedVehicles =
        (double) solutionInstance.getAssignedVehicles().size() / problem.getVehicles().size();
    if (vehicle == null || coinFlip(percentAssignedVehicles)) {
      solutionInstance.pollUsedVehicle(Utils.randomElement(problem.getTracks(), random), random);
      return solutionInstance;
    }

    List<TrackInstance> allowedTracks = solutionInstance.getAllowedTracks(vehicle);
    TrackInstance chosenInstance = Utils.randomElement(allowedTracks, random);
    if (chosenInstance != null && solutionInstance.canAssign(vehicle, chosenInstance.getTrack())) {
      solutionInstance.assign(vehicle, chosenInstance.getTrack());
    }

    return solutionInstance;
  }

  private boolean coinFlip(double probaTrue) {
    return random.nextDouble() <= probaTrue;
  }
}
