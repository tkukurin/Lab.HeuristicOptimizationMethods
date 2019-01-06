package hmo.common;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomAccessSet<E> extends AbstractSet<E> {

  private List<E> objects = new ArrayList<>();
  private Map<E, Integer> objectToIndex = new HashMap<>();

  public RandomAccessSet(Collection<E> items) {
    for (E item : items) {
      objectToIndex.put(item, objects.size());
      objects.add(item);
    }
  }

  @Override
  public boolean add(E item) {
    if (objectToIndex.containsKey(item)) {
      return false;
    }
    objectToIndex.put(item, objects.size());
    objects.add(item);
    return true;
  }

  public E removeAt(int id) {
    if (id >= objects.size()) {
      return null;
    }
    E res = objects.get(id);
    objectToIndex.remove(res);
    E last = objects.remove(objects.size() - 1);
    // skip filling the hole if last is removed
    if (id < objects.size()) {
      objectToIndex.put(last, id);
      objects.set(id, last);
    }
    return res;
  }

  @Override
  public boolean remove(Object item) {
    @SuppressWarnings(value = "element-type-mismatch")
    Integer id = objectToIndex.get(item);
    if (id == null) {
      return false;
    }
    removeAt(id);
    return true;
  }

  public E get(int i) {
    return objects.get(i);
  }

  public E pollRandom(Random rnd) {
    if (objects.isEmpty()) {
      return null;
    }
    int id = rnd.nextInt(objects.size());
    return removeAt(id);
  }

  @Override
  public int size() {
    return objects.size();
  }

  @Override
  public Iterator<E> iterator() {
    return objects.iterator();
  }
}
