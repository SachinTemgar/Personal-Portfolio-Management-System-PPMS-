public class MaxHeapImpl<T extends Comparable<T>> implements HeapADT<T> {

    private T[] heap;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;

    @SuppressWarnings("unchecked")
    public MaxHeapImpl() {
        heap = (T[]) new Comparable[DEFAULT_CAPACITY];
        size = 0;
    }

    @Override
    public void add(T element) {
        if (size == heap.length) {
            resize();
        }
        heap[size] = element;
        siftUp(size);
        size++;
    }

    @Override
    public T peek() {
        if (size == 0)
            return null;
        return heap[0];
    }

    @Override
    public T remove() {
        if (size == 0)
            return null;
        T removed = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null; 
        size--;
        siftDown(0);
        return removed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(heap[i]).append(" ");
        }
        return sb.toString();
    }

    private void resize() {
        T[] newHeap = (T[]) new Comparable[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, heap.length);
        heap = newHeap;
    }

    private void siftUp(int index) {
        int parent = (index - 1) / 2;
        while (index > 0 && heap[index].compareTo(heap[parent]) > 0) {
            swap(index, parent);
            index = parent;
            parent = (index - 1) / 2;
        }
    }

    private void siftDown(int index) {
        while (index < size) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int largest = index;
            if (left < size && heap[left].compareTo(heap[largest]) > 0) {
                largest = left;
            }
            if (right < size && heap[right].compareTo(heap[largest]) > 0) {
                largest = right;
            }
            if (largest != index) {
                swap(index, largest);
                index = largest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        T tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }
}
