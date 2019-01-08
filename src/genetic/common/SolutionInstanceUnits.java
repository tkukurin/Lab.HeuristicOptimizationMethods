package genetic.common;

import genetic.GAMeta;
import genetic.GeneticAlgorithm;
import genetic.GeneticAlgorithm.Combinator;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Mutator;
import genetic.GeneticAlgorithm.Pair;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.UnitAndFitness;
import genetic.GeneticAlgorithm.UnitGenerator;
import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import hmo.solver.GreedySolver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SolutionInstanceUnits {
  private static final Logger logger = Logger.getLogger(SolutionInstanceUnits.class.toString());

  private Random random;
  private Problem problem;

  public SolutionInstanceUnits(Random random, Problem problem) {
    this.random = random;
    this.problem = problem;
  }

  public Stream<Pair<PopulationInfo, Callable<UnitAndFitness<SolutionInstance>>>> evaluate(
      Function<SolutionInstance, Double> fitnessFunction,
      IterationBounds iterationBounds,
      GAMeta meta,
      List<Parameters> parameters) {
    return parameters
      .stream()
      .map(parameter -> new Pair<>(
          parameter.populationInfo,
          new GeneticAlgorithm<>(
              unitGenerator(meta),
              parameter.populationInfo,
              iterationBounds,
              fitnessFunction::apply,
              crossover(meta),
              mutator(meta),
              random,
              logger)))
      .map(ga -> new Pair<>(ga.first, ga.second::iterate));
  }

  public Mutator<SolutionInstance> mutator(GAMeta meta) {
    return this::modify;
  }

  private SolutionInstance modify(SolutionInstance solutionInstance) {
    solutionInstance.resetVehiclePool();
    Vehicle vehicle = solutionInstance.pollUnusedVehicle(random);
    if (vehicle == null) {
      return swapTracks(solutionInstance);
    }

    List<TrackInstance> allowedTracks = solutionInstance.getAllowedTracks(vehicle);
    TrackInstance chosenTrack = Utils.randomElement(allowedTracks, random);
    if (chosenTrack != null && solutionInstance.canAssign(vehicle, chosenTrack.getTrack())) {
      solutionInstance.assign(vehicle, chosenTrack.getTrack());
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
    solutionInstance.swapParkedVehicles(fstTrack.getTrack(), sndTrack.getTrack());

//    TrackInstance fillFirst = solutionInstance.getProblem().getBlockedTrack(fstTrack, sndTrack);
//    if (random.nextBoolean()) {
//      fillFirst = solutionInstance.getProblem().getBlockedTrack(sndTrack, fstTrack);
//    }
//
//    TrackInstance fillSecond = fstTrack == fillFirst ? sndTrack : fstTrack;
//
//    List<VehicleInstance> fstVehicles = fillFirst.getParkedVehicles();
//    List<VehicleInstance> sndVehicles = fillSecond.getParkedVehicles();
//
//    MergeIterator mergeIterator = new MergeIterator(fstVehicles, sndVehicles);
//    TrackInstance newFirst = new TrackInstance(fillFirst.getTrack());
//    TrackInstance newSecond = new TrackInstance(fillSecond.getTrack());
//
//    while (mergeIterator.hasNext()) {
//      VehicleInstance nextInstance = mergeIterator.next();
//      if (!newFirst.canAdd(nextInstance.getVehicle())) {
//        break;
//      }
//      newFirst.add(nextInstance);
//    }
//
//    while (mergeIterator.hasNext()) {
//      VehicleInstance nextInstance = mergeIterator.next();
//      if (!newSecond.canAdd(nextInstance.getVehicle())) {
//        solutionInstance.returnToPool(nextInstance.getVehicle());
//        break;
//      }
//      newSecond.add(nextInstance);
//    }
//
//    while (mergeIterator.hasNext()) {
//      solutionInstance.returnToPool(mergeIterator.next().getVehicle());
//    }

    return solutionInstance;
  }

  public Combinator<SolutionInstance> crossover(GAMeta meta) {
    return (first, second) -> {
      Comparator<TrackInstance> compareNumParkedVehicles = Comparator.comparingInt(
          TrackInstance::nParkedVehicles);
      Function<Pair<TrackInstance, TrackInstance>, TrackInstance> chooseLonger = pair -> Utils
          .argmax(i -> i.getParkedVehicles().size(), pair.first, pair.second);
      return combineSolutions(
          first, second, chooseLonger, compareNumParkedVehicles);
    };
  }

  private SolutionInstance combineSolutions(
      SolutionInstance first,
      SolutionInstance second,
      Function<Pair<TrackInstance, TrackInstance>, TrackInstance> pickFunction,
      Comparator<TrackInstance> comparator) {
    List<TrackInstance> trackInstances1 = new ArrayList<>(first.getTrackInstancesInorder());
    List<TrackInstance> trackInstances2 = new ArrayList<>(second.getTrackInstancesInorder());
    Set<Vehicle> usedVehicles = new HashSet<>();
    Map<Track, List<VehicleInstance>> tracks = new HashMap<>();

    Utils.zip(trackInstances1, trackInstances2).stream()
        .map(pickFunction)
        .sorted(comparator)
        .forEach(trackInstance -> {
          List<VehicleInstance> unusedCurrent = new ArrayList<>();
          for (VehicleInstance vehicleInstance : trackInstance.getParkedVehicles()) {
            if (usedVehicles.contains(vehicleInstance.getVehicle())) {
              continue;
            }

            usedVehicles.add(vehicleInstance.getVehicle());
            unusedCurrent.add(vehicleInstance);
          }

          tracks.put(trackInstance.getTrack(), unusedCurrent);
        });

    return new SolutionInstance(problem, tracks);
  }

  public UnitGenerator<SolutionInstance> unitGenerator(GAMeta meta) {
    return new UnitGenerator<>(() -> new GreedySolver(problem, random).solve());
//    return new UnitGenerator<>(() -> new SolutionInstance(problem));
  }
}
