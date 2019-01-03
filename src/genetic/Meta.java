package genetic;

public class Meta {
    public final int nBits;
    public final double lowerBound;
    public final double upperBound;
    public final int dimension;

    public Meta(int nBits, double lowerBound, double upperBound, int dimension) {
        this.nBits = nBits;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.dimension = dimension;
    }
}
