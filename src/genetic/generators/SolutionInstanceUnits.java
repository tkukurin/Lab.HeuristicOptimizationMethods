package genetic.generators;

import genetic.Assignments.Parameters;
import genetic.GeneticAlgorithm.Combinator;
import genetic.GeneticAlgorithm.FitnessEvaluator;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Mutator;
import genetic.GeneticAlgorithm.Pair;
import genetic.GeneticAlgorithm.Unit;
import genetic.GeneticAlgorithm.UnitGenerator;
import genetic.Meta;
import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SolutionInstanceUnits implements Generator<SolutionInstance> {

  private Random random;

  public SolutionInstanceUnits(Random random) {
    this.random = random;
  }

  @Override
  public List<Unit<List<SolutionInstance>>> evaluate(
      Function<List<Double>, Double> function, Random random,
      IterationBounds iterationBounds, Meta meta, List<Parameters> params) {
    return null;
  }

  @Override
  public Mutator<List<SolutionInstance>> mutator(Random random, Meta meta) {
    return vector -> vector.stream().map(this::modify).collect(Collectors.toList());
  }

  private SolutionInstance modify(SolutionInstance solutionInstance) {
    Vehicle vehicle = solutionInstance.pollRandomVehicle(random);
    if (vehicle == null) {
      return swapTracks(solutionInstance);
    }

    for (Track track : solutionInstance.getProblem().getTracks()) {
      if (solutionInstance.canAssign(vehicle, track)) {
        solutionInstance.assign(vehicle, track);
      }
    }

    return solutionInstance;
  }

  static class MergeIterator implements Iterator<VehicleInstance> {
    private List<VehicleInstance> first;
    private List<VehicleInstance> second;
    private int iFirst = 0;
    private int iSecond = 0;

    public MergeIterator(List<VehicleInstance> first,
        List<VehicleInstance> second) {
      this.first = first;
      this.second = second;
    }

    @Override
    public boolean hasNext() {
      return iFirst < first.size() || iSecond < second.size();
    }

    @Override
    public VehicleInstance next() {
      VehicleInstance nextFirst = iFirst < first.size() ? first.get(iFirst) : null;
      VehicleInstance nextSecond = iSecond < second.size() ? second.get(iSecond) : null;

      if (nextSecond == null || (nextFirst != null &&
          nextFirst.getVehicle().getDeparture() < nextSecond.getVehicle().getDeparture())) {
        iFirst++;
        return nextFirst;
      }

      iSecond++;
      return nextSecond;
    }
  }

  private SolutionInstance swapTracks(SolutionInstance solutionInstance) {
    TrackInstance fstTrack = solutionInstance.getRandomTrack(random);
    TrackInstance sndTrack = solutionInstance.getRandomTrack(random,
        fstTrack.getAllowedVehicleSeries());
    TrackInstance fillFirst = solutionInstance.getProblem().getBlockedTrack(fstTrack, sndTrack);
    if (fillFirst != null) {
      TrackInstance fillSecond = fstTrack == fillFirst ? sndTrack : fstTrack;
      List<VehicleInstance> fstVehicles = fillFirst.getParkedVehicles();
      List<VehicleInstance> sndVehicles = fillSecond.getParkedVehicles();

      MergeIterator mergeIterator = new MergeIterator(fstVehicles, sndVehicles);
      TrackInstance newFirst = new TrackInstance(fillFirst.getTrack());
      TrackInstance newSecond = new TrackInstance(fillSecond.getTrack());

      while (mergeIterator.hasNext()) {
        VehicleInstance nextInstance = mergeIterator.next();
        if (!newFirst.canAdd(nextInstance.getVehicle())) {
          break;
        }
        newFirst.add(nextInstance);
      }

      while (mergeIterator.hasNext()) {
        VehicleInstance nextInstance = mergeIterator.next();
        if (!newSecond.canAdd(nextInstance.getVehicle())) {
          solutionInstance.returnToPool(nextInstance.getVehicle());
          continue;
        }
        newSecond.add(nextInstance);
      }
    }

    return solutionInstance;
  }

  @Override
  public Combinator<List<SolutionInstance>> uniformCrossover(Random random, Meta meta) {
    return (l1, l2) -> Utils.zip(l1, l2).stream()
        .map(this::combine)
        .collect(Collectors.toList());
  }

  private SolutionInstance combine(Pair<SolutionInstance, SolutionInstance> instances) {
    List<TrackInstance> trackInstances1 = instances.first.getTrackInstancesInorder();
    List<TrackInstance> trackInstances2 = instances.second.getTrackInstancesInorder();
    List<TrackInstance> longer = Utils.zip(trackInstances1, trackInstances2)
        .stream()
        .map(pair -> Utils.argmax(i -> i.getParkedVehicles().size(), pair.first, pair.second))
        .collect(Collectors.toList());
    Set<Vehicle> usedVehicles = new HashSet<>();
    for (TrackInstance trackInstance : longer) {
      List<VehicleInstance> unusedCurrent = new ArrayList<>();
      for (VehicleInstance vehicleInstance : trackInstance.getParkedVehicles()) {
        if (usedVehicles.contains(vehicleInstance.getVehicle())) {
          continue;
        }

        usedVehicles.add(vehicleInstance.getVehicle());
        unusedCurrent.add(vehicleInstance);
      }
      trackInstance.setParkedVehicles(unusedCurrent);
    }

    // TODO
    SolutionInstance fst = instances.first;
    SolutionInstance snd = instances.second;

    TrackInstance fstTrack = fst.getRandomTrack(random);
    TrackInstance sndTrack = snd.getRandomTrack(random, fstTrack.getAllowedVehicleSeries());
    while (sndTrack == null) {
      fstTrack = fst.getRandomTrack(random);
      sndTrack = snd.getRandomTrack(random, fstTrack.getAllowedVehicleSeries());
    }

    return swap(fst, snd, fstTrack, sndTrack);
  }

  private SolutionInstance swap(
      SolutionInstance fst, SolutionInstance snd,
      TrackInstance fstTrack, TrackInstance sndTrack) {

    TrackInstance fillFirst = Utils.argmax(t -> t.getTrack().getTrackLength(), fstTrack, sndTrack);
    // TODO
    return null;
  }

  @Override
  public FitnessEvaluator<List<SolutionInstance>> fitnessEvaluator(
      Function<List<Double>, Double> function,
      Meta meta) {
    return null;
  }

  @Override
  public UnitGenerator<List<SolutionInstance>> unitGenerator(Random random, Meta meta) {

    return null;
  }

  @Override
  public List<Double> asDoubles(Meta meta, List<SolutionInstance> value) {
    return null;
  }
}
