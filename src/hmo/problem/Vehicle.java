package hmo.problem;

import java.util.Objects;

public class Vehicle {

  private int id;
  private int vehicleLength;
  private int series;
  private int departure;
  private int layoutType;

  public Vehicle(int id, int vehicleLength, int series, int departure, int layoutType) {
    this.id = id;
    this.vehicleLength = vehicleLength;
    this.series = series;
    this.departure = departure;
    this.layoutType = layoutType;
  }

  public int getId() {
    return id;
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
    return id == vehicle.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Vehicle{" +
        "id=" + id +
        ", vehicleLength=" + vehicleLength +
        ", series=" + series +
        ", departure=" + departure +
        ", layoutType=" + layoutType +
        '}';
  }
}
