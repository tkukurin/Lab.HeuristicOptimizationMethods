package genetic.common;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/** Iterates from two lists based on some comparator. */
public class MergeIterator<T> implements Iterator<T> {
  private List<T> first;
  private List<T> second;
  private Comparator<T> comparator;
  private int iFirst = 0;
  private int iSecond = 0;

  public MergeIterator(List<T> first, List<T> second, Comparator<T> comparator) {
    this.first = first;
    this.second = second;
    this.comparator = comparator;
  }

  @Override
  public boolean hasNext() {
    return iFirst < first.size() || iSecond < second.size();
  }

  @Override
  public T next() {
    T nextFirst = iFirst < first.size() ? first.get(iFirst) : null;
    T nextSecond = iSecond < second.size() ? second.get(iSecond) : null;

    if (nextSecond == null || (nextFirst != null && comparator.compare(nextFirst, nextSecond) < 0)) {
      iFirst++;
      return nextFirst;
    }

    iSecond++;
    return nextSecond;
  }
}


