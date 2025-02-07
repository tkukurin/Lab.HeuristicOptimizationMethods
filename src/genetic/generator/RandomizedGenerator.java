package genetic.generator;

import genetic.GAMeta;
import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import hmo.problem.Problem;
import hmo.problem.Vehicle;
import java.util.List;
import java.util.Random;

public class RandomizedGenerator extends SolutionInstanceGenerator {

  public RandomizedGenerator(Random random, Problem problem, GAMeta meta) {
    super(random, problem, meta);
  }

  @Override
  SolutionInstance unitGenerator() {
    return new SolutionInstance(problem);
  }

  @Override
  SolutionInstance crossoverImpl(SolutionInstance s1, SolutionInstance s2) {
    s1.resetVehiclePool();
    s2.resetVehiclePool();

    SolutionInstance modified = coinFlip(0.5) ? s1 : s2;
    SolutionInstance nonModified = modified == s1 ? s2 : s1;

    for (VehicleInstance instance : nonModified.getAssignedVehicles()) {
      if (modified.canAssign(instance.getVehicle(), instance.getTrack())) {
        modified.assign(instance.getVehicle(), instance.getTrack());
      }
    }

    return modified;
  }

  @Override
  SolutionInstance mutatorImpl(SolutionInstance solutionInstance) {
    Vehicle vehicle = solutionInstance.pollUnusedVehicle(random);
    double percentAssignedVehicles =
        (double) solutionInstance.getAssignedVehicles().size() / problem.getVehicles().size();
    if (vehicle == null || coinFlip(percentAssignedVehicles)) {
      solutionInstance.resetVehiclePool();
      solutionInstance.pollUsedVehicle(Utils.randomElement(problem.getTracks(), random), random);
      return solutionInstance;
    }

    List<TrackInstance> allowedTracks = solutionInstance.getAllowedTracks(vehicle);
    TrackInstance chosenInstance = Utils.randomElement(allowedTracks, random);
    if (chosenInstance != null && solutionInstance.canAssign(vehicle, chosenInstance.getTrack())) {
      solutionInstance.assign(vehicle, chosenInstance.getTrack());
    }

    if (coinFlip(0.5)) {
      solutionInstance.swapParkedVehicles(
          solutionInstance.getRandomTrack(random).getTrack(),
          solutionInstance.getRandomTrack(random).getTrack()
      );
    }

    return solutionInstance;
  }

  private boolean coinFlip(double probabilityTrue) {
    return random.nextDouble() <= probabilityTrue;
  }
}
