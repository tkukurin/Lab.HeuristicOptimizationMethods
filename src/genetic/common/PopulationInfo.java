package genetic.common;

public class PopulationInfo {
  public int size;
  public int elitism;

  public double mutationProbability;
  public double crossoverProbability;

  public PopulationInfo(PopulationInfo populationInfo) {
    this(populationInfo.size, populationInfo.elitism, populationInfo.mutationProbability,
        populationInfo.crossoverProbability);
  }

  public PopulationInfo(int size, int elitism, double mutationProbability, double crossoverProbability) {
    this.size = size;
    this.elitism = elitism;
    this.mutationProbability = mutationProbability;
    this.crossoverProbability = crossoverProbability;
  }

  @Override
  public String toString() {
    return "PopulationInfo{" +
            "size=" + size +
            ", elitism=" + elitism +
            ", mutationProbability=" + mutationProbability +
            ", crossoverProbability=" + crossoverProbability +
            '}';
  }
}
