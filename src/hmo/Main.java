package hmo;

import genetic.Assignments.Parameters;
import genetic.GeneticAlgorithm.IterationBounds;
import genetic.GeneticAlgorithm.PopulationInfo;
import genetic.GeneticAlgorithm.Unit;
import genetic.Meta;
import genetic.generators.BinaryUnits;
import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.instance.VehicleInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import hmo.solver.GreedySolver;
import hmo.solver.Solver;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

  private static  final Logger LOG = Logger.getLogger(Main.class.toString());

  public static void main(String[] args) throws IOException {
    final FileReader inputReader = new FileReader("instanca1.txt");
    final FileWriter outputWriter = new FileWriter("output1.txt");

    Problem problem = readInput(inputReader);
    LOG.info(String.format("Solving problem with %s cars and %s tracks.",
        problem.getVehicles().size(),
        problem.getTracks().size()));

    Solver greedySolver = new GreedySolver(problem, new Random());
    SolutionInstance greedySolution = greedySolver.solve();
    try (BufferedWriter bf = new BufferedWriter(outputWriter)) {
      for (TrackInstance trackInstance : greedySolution.getTrackInstances()) {
        System.out.println("TI: " + trackInstance.toString());
        bf.write(trackInstance.toString());
        bf.newLine();
      }
    }
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

  private static SolutionInstance solveGeneticAlgorithm(Problem problem) {
    SolutionInstance solutionInstance = new SolutionInstance(problem);
    IterationBounds iterationBounds = new IterationBounds(25_000, 1e-6);
    int dimension = 20;

    // TODO create function
    Function<List<Double>, Double> function = null;
    Meta meta = new Meta(20, -50, 150, dimension);
    List<Parameters> parameters = Arrays.asList(
        new Parameters(new PopulationInfo(10, 2, 0.1, 0.68)),
        new Parameters(new PopulationInfo(20, 4, 0.2, 0.75)),
        new Parameters(new PopulationInfo(30, 4, 0.2, 0.75)),
        new Parameters(new PopulationInfo(100, 4, 0.35, 0.85)),
        new Parameters(new PopulationInfo(150, 4, 0.04, 0.75)),
        new Parameters(new PopulationInfo(200, 5, 0.5, 0.9))
    );

    // TODO use actual problem
    Random random = new Random(42L);
    BinaryUnits generator = new BinaryUnits();
    List<Unit<List<Boolean[]>>> results = generator.evaluate(
        function, random, iterationBounds, meta, parameters);

    return solutionInstance;
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

      for (int i = 0; i < vehicleNum; i++) {
        int len = Integer.parseInt(vehicleLengths[i]);
        int ser = Integer.parseInt(series[i]);
        int dep = Integer.parseInt(departureTimes[i]);
        int lay = Integer.parseInt(layoutTypes[i]);
        vehicles.add(new Vehicle(i, len, ser, dep, lay));
      }

      for (int i = 0; i < trackNum; i++) {
        int len = Integer.parseInt(trackLengths[i].trim());
        tracks.add(new Track(i, len, trackIdToVehicleIds.get(i)));
      }
    }

    return new Problem(tracks, vehicles, blockades, inverseBlockades);
  }

}
