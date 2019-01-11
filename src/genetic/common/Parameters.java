package genetic.common;

import java.util.Random;

public class Parameters {
  public PopulationInfo populationInfo;
  public IterationBounds iterationBounds;
  public Random random;

  public Parameters(PopulationInfo populationInfo, IterationBounds iterationBounds,
      Random random) {
    this.populationInfo = populationInfo;
    this.iterationBounds = iterationBounds;
    this.random = random;
  }
}
