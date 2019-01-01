package hmo;

import hmo.instance.SolutionInstance;
import hmo.instance.TrackInstance;
import hmo.problem.Problem;
import hmo.problem.Track;
import hmo.problem.Vehicle;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {

  public static void main(String[] args) throws IOException {
    final FileReader inputReader = new FileReader("instanca1.txt");
    final FileWriter outputWriter = new FileWriter("output.txt");

    Problem problem = readInput(inputReader);
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

    SolutionInstance solutionInstance = new SolutionInstance(problem);
    // TODO solve

    try (BufferedWriter bf = new BufferedWriter(outputWriter)) {
      for (TrackInstance track : solutionInstance.getTrackInstances()) {
        bf.write(track.printVehiclesInTrack());
      }
    }
  }

  private static Problem readInput(Reader reader) {
    List<Vehicle> vehicles = new ArrayList<>();
    List<Track> tracks = new ArrayList<>();
    HashMap<Integer, ArrayList<Integer>> blockades = new HashMap<>();

    Scanner sc = new Scanner(reader);
    int vehicleNum = Integer.parseInt(sc.nextLine().trim());
    int trackNum = Integer.parseInt(sc.nextLine().trim());
    sc.nextLine();
    String[] vehicleLengths = sc.nextLine().trim().split(" ");
    sc.nextLine();
    String[] series = sc.nextLine().trim().split(" ");
    sc.nextLine();
    List<String> limitations = new ArrayList<>();
    for (int i = 0; i < vehicleNum; i++) {
      limitations.add(sc.nextLine());
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
        ArrayList<Integer> blocks = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
          blocks.add(Integer.parseInt(lines[i]));
        }
        blockades.put(Integer.parseInt(lines[0]), blocks);
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
      ArrayList<Integer> rest = new ArrayList<>();
      for (int j = 0; j < vehicleNum; j++) {
        String[] lim = limitations.get(j).trim().split(" ");
        rest.add(Integer.parseInt(lim[i]));
      }
      int len = Integer.parseInt(trackLengths[i].trim());
      tracks.add(new Track(i, rest, len));
    }
    sc.close();

    return new Problem(tracks, vehicles, blockades);
  }

}
