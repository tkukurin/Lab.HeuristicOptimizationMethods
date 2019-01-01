package hmo.problem;

import java.util.Objects;

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

  public int getSeries() {
    return series;
  }

  public int getDeparture() {
    return departure;
  }

  public int getLayoutType() {
    return layoutType;
  }

  // ordinal number defines a single vehicle
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Vehicle vehicle = (Vehicle) o;
    return ord == vehicle.ord;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ord);
  }

  @Override
  public String toString() {
    return "Num = " + ord + "len = " + vehicleLength + ", ser = " + series + ", dep = " + departure
        + ", lay = " + layoutType;
  }
}
