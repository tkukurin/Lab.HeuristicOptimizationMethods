package genetic.generator;

import genetic.GAMeta;
import genetic.common.MergeIterator;
import hmo.RestrictionsHelper;
import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

public class SmarterGenerator extends TabooGenerator {

  public SmarterGenerator(Random random, Problem problem, GAMeta meta) {
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

    if (coinFlip(meta.longestTrackCombinatorProbability)) {
      return longestTrackCombinator(s1, s2);
    }

    SolutionInstance modified = coinFlip(0.5) ? s1 : s2;
    SolutionInstance nonModified = modified == s1 ? s2 : s1;

    for (VehicleInstance instance : nonModified.getAssignedVehicles()) {
      if (modified.canAssign(instance.getVehicle(), instance.getTrack())) {
        modified.assign(instance.getVehicle(), instance.getTrack());
      }
    }

    return modified;
  }

  private SolutionInstance longestTrackCombinator(SolutionInstance s1, SolutionInstance s2) {
    Comparator<TrackInstance> comparator = Comparator.<TrackInstance>comparingDouble(
        t -> t.getAvailableSpace() / t.getTrack().getTrackLength());
    List<TrackInstance> t1 =
        s1.getTrackInstances().stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    List<TrackInstance> t2 =
        s1.getTrackInstances().stream()
            .sorted(comparator)
            .collect(Collectors.toList());

    MergeIterator<TrackInstance> mergeIterator = new MergeIterator<>(t1, t2, comparator);
    SolutionInstance solutionInstance = new SolutionInstance(problem);
    while (mergeIterator.hasNext()) {
      TrackInstance next = mergeIterator.next();
      for (VehicleInstance vehicleInstance : next.getParkedVehicles()) {
        if (solutionInstance.canAssign(vehicleInstance.getVehicle(), next.getTrack())) {
          solutionInstance.assign(vehicleInstance.getVehicle(), next.getTrack());
        }
      }
    }

    return solutionInstance;
  }

  @Override
  SolutionInstance mutatorImpl(SolutionInstance solutionInstance) {
    Vehicle vehicle = solutionInstance.pollUnusedVehicle(random);
    double percentAssignedVehicles =
        (double) solutionInstance.getAssignedVehicles().size() / problem.getVehicles().size();

    if (vehicle == null || coinFlip(percentAssignedVehicles * meta.assignedVehiclesMultiplierProbability)) {
      solutionInstance.resetVehiclePool();
      solutionInstance.pollUsedVehicle(Utils.randomElement(problem.getTracks(), random), random);
      return solutionInstance;
    }

    List<TrackInstance> allowedTracks = solutionInstance.getAllowedTracks(vehicle);
    TrackInstance chosenInstance = Utils.randomElement(allowedTracks, random);
    if (chosenInstance != null && solutionInstance.canAssign(vehicle, chosenInstance.getTrack())) {
      solutionInstance.assign(vehicle, chosenInstance.getTrack());
    }

    Map<Track, Collection<Track>> restrictions =
        new RestrictionsHelper(solutionInstance).collectBlockers();
    for (Entry<Track, Collection<Track>> restriction : restrictions.entrySet()) {
      Collection<Track> blockers = restriction.getValue();
      blockers.forEach(solutionInstance::removeParkedVehicles);
    }

    if (coinFlip(meta.trackSwapProbability)) {
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
