package hmo;

import genetic.common.Pair;
import genetic.common.PopulationInfo;
import genetic.common.Parameters;
import hmo.instance.SolutionInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import hmo.solver.GAMultiThreaded;
import hmo.solver.GreedySolver;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

  private static  final Logger LOG = Logger.getLogger(Main.class.toString());

  public static void main(String[] args) throws IOException {
//    Path inputFilePath = Paths.get("inputs/dummy-lesstracks.txt");
    Path inputFilePath = Paths.get("instanca1.txt");
    String inputFileName = inputFilePath.getFileName().toString();
    final FileReader inputReader = new FileReader(inputFilePath.toFile());

    Problem problem = readInput(inputReader);
    LOG.info(String.format("Solving problem with %s cars and %s tracks.",
        problem.getVehicles().size(),
        problem.getTracks().size()));

    ExecutorService executorService = Executors
        .newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    Iterator<Pair<PopulationInfo, SolutionInstance>> gaSolutionIterator = new GAMultiThreaded()
        .solve(problem, executorService, new Parameters(new PopulationInfo(50, 3, 0.99, 0.99)));

    double highestGoal = Double.MIN_VALUE;
    while (gaSolutionIterator.hasNext()) {
      Pair<PopulationInfo, SolutionInstance> solutionPair = gaSolutionIterator.next();
      if (solutionPair == null) {
        continue;
      }

      PopulationInfo populationInfo = solutionPair.first;
      SolutionInstance gaSolution = solutionPair.second;
      Evaluator evaluator = new Evaluator(gaSolution);
      LOG.info(String.format(
          "[%s] %s/%s unassigned vehicles and %s/%s used tracks.\n",
          populationInfo.toString(),
          gaSolution.getUnassignedVehicles().size(),
          problem.getVehicles().size(),
          gaSolution.nUsedTracks(),
          problem.getTracks().size()));

      // TODO some smarter finisher maybe
      gaSolution = new GreedySolver(gaSolution, new Random(42L)).solve();

      RestrictionsHelper restrictionsHelper = new RestrictionsHelper(gaSolution);
      Collection<String> failedChecks = restrictionsHelper.getRestrictionChecks().entrySet()
          .stream()
          // collect any test that does not pass
          .filter(entry -> !entry.getValue().get())
          .map(Entry::getKey)
          .collect(Collectors.toList());
      if (!failedChecks.isEmpty()) {
        LOG.info(String.format("[%s] Failed checks: %s",
            populationInfo.toString(),
            String.join(", ", failedChecks)));
        continue;
      }

      double fst = evaluator.firstGoal();
      double snd = evaluator.secondGoal();
      double goal = snd / fst;
      LOG.info(String.format("[%s] Fitness: %.3f/%.3f = %.3f", populationInfo, snd, fst, goal));
      if (goal > highestGoal) {
        highestGoal = goal;
        // just overwrite past best solutions. can also be handled better.
        output(gaSolution, String.format("bestOutput-%s", inputFileName));
      }

      output(gaSolution, String.format(
          "output-%s-%s.txt", inputFileName, populationInfo.toString()));
    }

    System.out.println("Done.");
    executorService.shutdown();
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
        final int vehicleId = i + 1;
        // limitation equals 1 if car "i" can be placed on track "j"
        List<Integer> limitation = Arrays.stream(in.nextLine().split(" "))
            .map(Integer::parseInt).collect(Collectors.toList());
        assert limitation.size() == trackNum;
        IntStream.range(1, limitation.size() + 1)
            .forEach(trackId -> {
              if (limitation.get(trackId - 1) == 1) {
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
        int vehicleId = i + 1;
        vehicles.add(new Vehicle(vehicleId, len, ser, dep, lay));
      }

      for (int i = 0; i < trackNum; i++) {
        int len = Integer.parseInt(trackLengths[i].trim());
        int trackId = i + 1;
        tracks.add(new Track(trackId, len, trackIdToVehicleIds.get(trackId)));
      }

      assert vehicles.size() == vehicleNum;
      assert tracks.size() == trackNum;
    }

    return new Problem(tracks, vehicles, blockades, inverseBlockades);
  }

  private static void output(SolutionInstance gaSolution, String fileName) throws IOException {
    final FileWriter outputWriter = new FileWriter("outputs/" + fileName);
    try (BufferedWriter writer = new BufferedWriter(outputWriter)) {
      writer.write(gaSolution.toString());
    }
  }

  private static void printOutBlockers(RestrictionsHelper restrictionsHelper) {
    Map<Track, Collection<Track>> blockers = restrictionsHelper.collectBlockers();
    blockers.forEach((k, v) -> {
      if (!v.isEmpty()) {
        System.out.format("Blockers for track %s: [%s]\n",
            k.getId(),
            v.stream().map(Track::getId).map(Object::toString)
                .collect(Collectors.joining(", ")));
      }
    });
  }
}
