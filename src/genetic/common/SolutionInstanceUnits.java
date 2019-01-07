package genetic.common;

import genetic.GeneticAlgorithm;
import genetic.GeneticAlgorithm.Combinator;
import genetic.GeneticAlgorithm.FitnessEvaluator;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.Mutator;
import genetic.GeneticAlgorithm.Pair;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.UnitGenerator;
import genetic.GeneticAlgorithm.UnitAndFitness;
import genetic.Meta;
import hmo.common.Utils;
import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import hmo.solver.GreedySolver;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SolutionInstanceUnits {
  private static final Logger logger = Logger.getLogger(SolutionInstanceUnits.class.toString());

  private Random random;
  private Problem problem;
  private ExecutorService executorService;

  public SolutionInstanceUnits(Random random, Problem problem,
      ExecutorService executorService) {
    this.random = random;
    this.problem = problem;
    this.executorService = executorService;
  }

  public Stream<Pair<PopulationInfo, Future<UnitAndFitness<SolutionInstance>>>> evaluate(
      Function<SolutionInstance, Double> fitnessFunction,
      IterationBounds iterationBounds,
      Meta meta,
      List<Parameters> parameters) {
    Stream<Pair<PopulationInfo, Callable<UnitAndFitness<SolutionInstance>>>> callableStream =
        parameters
          .stream()
          .map(param -> new Pair<>(
              param.populationInfo,
              new GeneticAlgorithm<>(
                  unitGenerator(random, meta), param.populationInfo, iterationBounds,
                  fitnessFunction::apply, uniformCrossover(random, meta),
                  mutator(random, meta), random, logger)))
          .map(ga -> new Pair<>(ga.first, ga.second::iterate));

    return callableStream.map(pair ->
        new Pair<>(pair.first, executorService.submit(pair.second)));
  }

  public Mutator<SolutionInstance> mutator(Random random, Meta meta) {
    return this::modify;
  }

  private SolutionInstance modify(SolutionInstance solutionInstance) {
    Vehicle vehicle = solutionInstance.getRandomVehicle(random);
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

  public Combinator<SolutionInstance> uniformCrossover(Random random, Meta meta) {
    return (s1, s2) -> combineLongestTrackInstanceSequence(new Pair<>(s1, s2));
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

  public FitnessEvaluator<SolutionInstance> fitnessEvaluator(
      Function<SolutionInstance, Double> function,
      Meta meta) {
    return function::apply;
  }

  public UnitGenerator<SolutionInstance> unitGenerator(Random random, Meta meta) {
    return new UnitGenerator<>(() -> new GreedySolver(problem, random).solve());
  }
}
