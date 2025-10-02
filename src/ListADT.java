public interface ListADT<T> {
    int size();
    T get(int index);
    void add(T item);
    void clear();
    boolean remove(T item);
	boolean isEmpty();
}
