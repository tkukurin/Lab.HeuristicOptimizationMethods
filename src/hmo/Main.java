package hmo;

import genetic.GeneticAlgorithm.Pair;
import genetic.GeneticAlgorithm.PopulationInfo;
import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import hmo.solver.GeneticAlgorithmSolver;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

  private static  final Logger LOG = Logger.getLogger(Main.class.toString());

  public static void main(String[] args) throws IOException {
    final String inputFileName = "dummy-lesstracks.txt";
    final FileReader inputReader = new FileReader(inputFileName);

    Problem problem = readInput(inputReader);
    LOG.info(String.format("Solving problem with %s cars and %s tracks.",
        problem.getVehicles().size(),
        problem.getTracks().size()));

    ExecutorService executorService = Executors
        .newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    Iterator<Pair<PopulationInfo, SolutionInstance>> gaSolutionIterator =
        GeneticAlgorithmSolver.solve(problem, executorService);
    double bestFitness = Double.MIN_VALUE;

    while (gaSolutionIterator.hasNext()) {
      Pair<PopulationInfo, SolutionInstance> solutionPair = gaSolutionIterator.next();
      if (solutionPair == null) {
        continue;
      }

      PopulationInfo populationInfo = solutionPair.first;
      SolutionInstance gaSolution = solutionPair.second;
      LOG.info(String.format(
          "[%s] %s/%s unassigned vehicles and %s/%s used tracks.",
          populationInfo.toString(),
          gaSolution.getUnassignedVehicles().size(),
          problem.getVehicles().size(),
          gaSolution.nUsedTracks(),
          problem.getTracks().size()));

      RestrictionsHelper restrictionsHelper = new RestrictionsHelper(gaSolution);
      Map<String, Supplier<Boolean>> checks = restrictionsHelper.getRestrictionChecks();
      if (!checks.entrySet().stream().allMatch(passesNecessaryChecks(populationInfo))) {
        // TODO have this behavior later.
//        continue;
      }

      double currentFitness = new Evaluator(gaSolution).fitnessToMaximize();
      String fileName = String.format("output-%s-%s.txt", inputFileName, populationInfo.toString());
      if (currentFitness > bestFitness) {
        bestFitness = currentFitness;
        fileName = String.format("bestOutput-%s", inputFileName);
      }

      final FileWriter outputWriter = new FileWriter(fileName);
      try (BufferedWriter writer = new BufferedWriter(outputWriter)) {
        for (TrackInstance trackInstance : gaSolution.getTrackInstancesInorder()) {
          writer.write(trackInstance.toString());
          writer.newLine();
        }
      }
    }

    System.out.println("Done.");
    executorService.shutdown();
  }

  private static Predicate<Entry<String, Supplier<Boolean>>> passesNecessaryChecks(
      PopulationInfo populationInfo) {
    return entry -> {
          boolean passes = entry.getValue().get();
          if (!passes) {
            LOG.info(String.format("[%s] Failed check: %s",
                populationInfo.toString(),
                entry.getKey()));
          }
          return passes;
        };
  }

  private static Problem readInput(Reader reader) {
    List<Vehicle> vehicles = new ArrayList<>();
    List<Track> tracks = new ArrayList<>();
    Map<Integer, Collection<Integer>> blockades = new HashMap<>();
    Map<Integer, Collection<Integer>> inverseBlockades = new HashMap<>();

    try (Scanner in = new Scanner(reader)) {
      int vehicleNum = Integer.parseInt(in.nextLine().trim());
      int trackNum = Integer.parseInt(in.nextLine().trim());

      in.nextLine();
      String[] vehicleLengths = in.nextLine().trim().split(" ");

      in.nextLine();
      String[] series = in.nextLine().trim().split(" ");

      in.nextLine();
      Map<Integer, Set<Integer>> trackIdToVehicleIds = new HashMap<>();
      for (int i = 0; i < vehicleNum; i++) {
        final int vehicleId = i;
        // limitation equals 1 if car "i" can be placed on track "j"
        List<Integer> limitation = Arrays.stream(in.nextLine().split(" "))
            .map(Integer::parseInt).collect(Collectors.toList());
        assert limitation.size() == trackNum;
        IntStream.range(0, limitation.size())
            .forEach(trackId -> {
              if (limitation.get(trackId) == 1) {
                Set<Integer> vehicleIds = trackIdToVehicleIds
                    .getOrDefault(trackId, new HashSet<>());
                vehicleIds.add(vehicleId);
                trackIdToVehicleIds.put(trackId, vehicleIds);
              }
            });
      }

      in.nextLine();
      String[] trackLengths = in.nextLine().trim().split(" ");
      in.nextLine();
      String[] departureTimes = in.nextLine().trim().split(" ");
      in.nextLine();
      String[] layoutTypes = in.nextLine().trim().split(" ");
      in.nextLine();

      // fmt: "blockingTrack listOfBlockedTracks"
      while (in.hasNext()) {
        String[] lines = in.nextLine().trim().split(" ");
        if (lines.length > 0 && !lines[0].isEmpty() && !lines[0].equals("#")) {
          Collection<Integer> blocks = new HashSet<>();
          int currentTrackId = Integer.parseInt(lines[0]);

          for (int i = 1; i < lines.length; i++) {
            int blockedTrackId = Integer.parseInt(lines[i]);
            blocks.add(blockedTrackId);

            Collection<Integer> inverseList = inverseBlockades
                .getOrDefault(blockedTrackId, new HashSet<>());
            inverseList.add(currentTrackId);
            inverseBlockades.put(blockedTrackId, inverseList);
          }

          blockades.put(currentTrackId, blocks);
        }
      }

      for (int i = 0; i < vehicleNum; i++) {
        int len = Integer.parseInt(vehicleLengths[i]);
        int ser = Integer.parseInt(series[i]);
        int dep = Integer.parseInt(departureTimes[i]);
        int lay = Integer.parseInt(layoutTypes[i]);
        vehicles.add(new Vehicle(i + 1, len, ser, dep, lay));
      }

      for (int i = 0; i < trackNum; i++) {
        int len = Integer.parseInt(trackLengths[i].trim());
        tracks.add(new Track(i + 1, len, trackIdToVehicleIds.get(i)));
      }

      assert vehicles.size() == vehicleNum;
      assert tracks.size() == trackNum;
    }

    return new Problem(tracks, vehicles, blockades, inverseBlockades);
  }

  private static void outputProblem(Problem problem) {
    System.out.println("Vehicles:");
    System.out.println("-----------------------------------------");
    for (Vehicle v : problem.getVehicles()) {
      System.out.println(v.toString());
    }
    System.out.println("Tracks:");
    System.out.println("-----------------------------------------");
    for (Track t : problem.getTracks()) {
      System.out.println(t.toString());
    }
  }

}
