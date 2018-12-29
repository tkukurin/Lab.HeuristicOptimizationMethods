public class Vehicle {
    private int ord;
    private int vehicleLength;
    private int series;
    private int departure;
    private int layoutType;

    public Vehicle(int ord, int vehicleLength, int series, int departure, int layoutType) {
        this.ord = ord;
        this.vehicleLength = vehicleLength;
        this.series = series;
        this.departure = departure;
        this.layoutType = layoutType;
    }

    public int getOrd() {
        return ord;
    }

    public int getVehicleLength() {
        return vehicleLength;
    }

    public void setVehicleLength(int vehicleLength) {
        this.vehicleLength = vehicleLength;
    }

    public int getSeries() {
        return series;
    }

    public void setSeries(int series) {
        this.series = series;
    }

    public int getDeparture() {
        return departure;
    }

    public void setDeparture(int departure) {
        this.departure = departure;
    }

    public int getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(int layoutType) {
        this.layoutType = layoutType;
    }

    @Override
    public String toString() {
        return "Num = " + ord + "len = " + vehicleLength + ", ser = " + series + ", dep = " + departure + ", lay = " + layoutType;
    }
}
