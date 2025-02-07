package genetic.common;

public class IterationBounds {
  int numIterations;
  double deltaThreshold;
  private boolean started;
  private long startTime;


  public IterationBounds(int numIterations, double deltaThreshold) {
    this.numIterations = numIterations;
    this.deltaThreshold = deltaThreshold;
    started = false;
  }

  public boolean isComplete(int numIterations) {
//      Double lastError = lastErrors.peekLast();
//      ArrayDeque<Double> errorsShifted = new ArrayDeque<>(lastErrors);
//      errorsShifted.addFirst(errorsShifted.pollLast());
//
//      double maxDeltaBetweenIterations = Utils
//          .zip(new ArrayList<>(lastErrors), new ArrayList<>(errorsShifted))
//          .stream()
//          .map(pair -> Math.abs(pair.first - pair.second))
//          .max(Comparator.comparingDouble(d -> d))
//          .orElse(Double.MAX_VALUE);

    return numIterations == this.numIterations;
//          || lastError <= this.deltaThreshold
//          || (errorsShifted.size() > 1 && maxDeltaBetweenIterations <= this.deltaThreshold);
  }

  public boolean timeRanOut(long durationInMilis) {
    if (!started) {
      started = true;
      startTime = System.currentTimeMillis();
      return false;
    } else {
      return (startTime + durationInMilis < System.currentTimeMillis());
    }
  }
}
