package genetic.common;

import genetic.GeneticAlgorithm;
import genetic.GeneticAlgorithm.Combinator;
import genetic.GeneticAlgorithm.FitnessEvaluator;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Mutator;
import genetic.GeneticAlgorithm.Pair;
import genetic.GeneticAlgorithm.UnitGenerator;
import genetic.GeneticAlgorithm.UnitKeyFitnessValue;
import genetic.Meta;
import hmo.Evaluator;
import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SolutionInstanceUnits {

  private Random random;
  private Problem problem;

  public SolutionInstanceUnits(Random random, Problem problem) {
    this.random = random;
    this.problem = problem;
  }

  public List<UnitKeyFitnessValue> evaluate(
      Function<List<Double>, Double> function, Random random,
      IterationBounds iterationBounds, Meta meta, List<Parameters> params) {
    List<UnitKeyFitnessValue> solution = new ArrayList<>();
    for (Parameters parameters : params) {
      GeneticAlgorithm<List<SolutionInstance>> geneticAlgorithm = new GeneticAlgorithm<>(
          unitGenerator(random, meta), parameters.populationInfo, iterationBounds,
          fitnessEvaluator(function, meta), uniformCrossover(random, meta),
          mutator(random, meta), random, Logger.getLogger(SolutionInstanceUnits.class.toString()));
      solution.add(geneticAlgorithm.iterate());
    }
    return solution;
  }

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
        break;
      }
    }

    return solutionInstance;
  }

  /** Iterates based on departure time from two lists. */
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
          break;
        }
        newSecond.add(nextInstance);
      }

      while (mergeIterator.hasNext()) {
        solutionInstance.returnToPool(mergeIterator.next().getVehicle());
      }
    }

    return solutionInstance;
  }

  public Combinator<List<SolutionInstance>> uniformCrossover(Random random, Meta meta) {
    return (l1, l2) -> Utils.zip(l1, l2).stream()
        .map(this::combineLongestTrackInstanceSequence)
        .collect(Collectors.toList());
  }

  /** Create a new solution, using the TrackInstances with most parked vehicles per track. */
  private SolutionInstance combineLongestTrackInstanceSequence(
      Pair<SolutionInstance, SolutionInstance> instances) {
    List<TrackInstance> trackInstances1 = new ArrayList<>(instances.first.getTrackInstances());
    List<TrackInstance> trackInstances2 = new ArrayList<>(instances.second.getTrackInstances());
    List<TrackInstance> longer = Utils.zip(trackInstances1, trackInstances2).stream()
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

    return new SolutionInstance(instances.first.getProblem(), longer);
  }

  public FitnessEvaluator<List<SolutionInstance>> fitnessEvaluator(
      Function<List<Double>, Double> function,
      Meta meta) {
    return l -> function.apply(asDoubles(meta, l));
  }

  public List<Double> asDoubles(Meta meta, List<SolutionInstance> value) {
    return value.stream().map(Evaluator::new).map(Evaluator::rate).collect(Collectors.toList());
  }

  public UnitGenerator<List<SolutionInstance>> unitGenerator(Random random, Meta meta) {
    return new UnitGenerator<>(() -> Collections.singletonList(new SolutionInstance(problem)));
  }
}
