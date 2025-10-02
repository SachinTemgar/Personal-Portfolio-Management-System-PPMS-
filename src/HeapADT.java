public interface HeapADT<T extends Comparable<T>> {
    void add(T element);
    T peek();
    T remove();
    String toString();
}