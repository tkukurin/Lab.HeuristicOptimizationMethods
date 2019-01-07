package genetic.common;

public class Unit<T> {
  public T value;

  public Unit(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "Unit{" +
        "value=" + value +
        '}';
  }
}


