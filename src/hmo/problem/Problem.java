package hmo.problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Problem {
  private List<Track> tracks;
  private List<Vehicle> vehicles;
  private HashMap<Integer, ArrayList<Integer>> blockades;

  public Problem(List<Track> tracks, List<Vehicle> vehicles,
      HashMap<Integer, ArrayList<Integer>> blockades) {
    this.tracks = tracks;
    this.vehicles = vehicles;
    this.blockades = blockades;
  }

  public List<Track> getTracks() {
    return tracks;
  }

  public List<Vehicle> getVehicles() {
    return vehicles;
  }

  public HashMap<Integer, ArrayList<Integer>> getBlockades() {
    return blockades;
  }
}
