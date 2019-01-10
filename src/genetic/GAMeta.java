package genetic;

/** Class containing meta info about the genetic algorithm. */
public class GAMeta {

  public double trackSwapProbability = 0.5;
  public double assignedVehiclesMultiplierProbability = 0.85;
  public double longestTrackCombinatorProbability = 0.1;

  public GAMeta() {
  }

  public GAMeta(double trackSwapProbability,
      double assignedVehiclesMultiplierProbability,
      double longestTrackCombinatorProbability) {
    this.trackSwapProbability = trackSwapProbability;
    this.assignedVehiclesMultiplierProbability = assignedVehiclesMultiplierProbability;
    this.longestTrackCombinatorProbability = longestTrackCombinatorProbability;
  }
}
