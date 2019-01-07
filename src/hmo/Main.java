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
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

  private static  final Logger LOG = Logger.getLogger(Main.class.toString());

  public static void main(String[] args) throws IOException, InterruptedException {
    final FileReader inputReader = new FileReader("instanca1.txt");

    Problem problem = readInput(inputReader);
    LOG.info(String.format("Solving problem with %s cars and %s tracks.",
        problem.getVehicles().size(),
        problem.getTracks().size()));

    Iterator<Pair<PopulationInfo, SolutionInstance>> gaSolutionIterator =
        GeneticAlgorithmSolver.solve(problem);

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

      final FileWriter outputWriter = new FileWriter(
          String.format("output-%s.txt", populationInfo.toString()));
      try (BufferedWriter writer = new BufferedWriter(outputWriter)) {
        for (TrackInstance trackInstance : gaSolution.getTrackInstancesInorder()) {
          writer.write(trackInstance.toString());
          writer.newLine();
        }
      }
    }

    System.out.println("Done.");

//    SolutionInstance solution = null;
//    int totalIterations = 1_000_000;
//    for (int i = 0; i < totalIterations; i++) {
//      Solver greedySolver = new GreedySolver(problem, new Random());
//      solution = greedySolver.solve();
//      Evaluator evaluator = new Evaluator(solution);
//      evaluator.maximizationFunction();
//
//      if (solution.getUnassignedVehicles().isEmpty()) {
//        // found solution without unassigned vehicles.
//        break;
//      }
//    }
//
  }

  private static Problem readInput(Reader reader) {
    List<Vehicle> vehicles = new ArrayList<>();
    List<Track> tracks = new ArrayList<>();
    Map<Integer, Collection<Integer>> blockades = new HashMap<>();
    Map<Integer, Collection<Integer>> inverseBlockades = new HashMap<>();

    try (Scanner sc = new Scanner(reader)) {
      int vehicleNum = Integer.parseInt(sc.nextLine().trim());
      int trackNum = Integer.parseInt(sc.nextLine().trim());
      sc.nextLine();

      String[] vehicleLengths = sc.nextLine().trim().split(" ");
      sc.nextLine();
      String[] series = sc.nextLine().trim().split(" ");
      sc.nextLine();

      Map<Integer, Set<Integer>> trackIdToVehicleIds = new HashMap<>();
      for (int i = 0; i < vehicleNum; i++) {
        final int vehicleId = i;
        // limitation equals 1 if car "i" can be placed on track "j"
        List<Integer> limitation = Arrays.stream(sc.nextLine().split(" "))
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

      sc.nextLine();
      String[] trackLengths = sc.nextLine().trim().split(" ");
      sc.nextLine();
      String[] departureTimes = sc.nextLine().trim().split(" ");
      sc.nextLine();
      String[] layoutTypes = sc.nextLine().trim().split(" ");
      sc.nextLine();

      // fmt: "blockingTrack listOfBlockedTracks"
      while (sc.hasNext()) {
        String[] lines = sc.nextLine().trim().split(" ");
        if (lines.length > 0 && !lines[0].isEmpty()) {
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

      for (int vehicleId = 0; vehicleId < vehicleNum; vehicleId++) {
        int len = Integer.parseInt(vehicleLengths[vehicleId]);
        int ser = Integer.parseInt(series[vehicleId]);
        int dep = Integer.parseInt(departureTimes[vehicleId]);
        int lay = Integer.parseInt(layoutTypes[vehicleId]);
        vehicles.add(new Vehicle(vehicleId, len, ser, dep, lay));
      }

      for (int trackId = 0; trackId < trackNum; trackId++) {
        int len = Integer.parseInt(trackLengths[trackId].trim());
        tracks.add(new Track(trackId, len, trackIdToVehicleIds.get(trackId)));
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
