public class SortingUtil {
    public static <T extends Comparable<T>> ListADT<T> mergeSort(ListADT<T> list) {
        if (list.size() <= 1) {
            return list;
        }
        
        int mid = list.size() / 2;
        ListADT<T> left = new ArrayListImpl<>();
        ListADT<T> right = new ArrayListImpl<>();
        
        for (int i = 0; i < list.size(); i++) {
            if (i < mid) {
                left.add(list.get(i));
            } else {
                right.add(list.get(i));
            }
        }
        
        left = mergeSort(left);
        right = mergeSort(right);
        
        return merge(left, right);
    }
    
    private static <T extends Comparable<T>> ListADT<T> merge(ListADT<T> left, ListADT<T> right) {
        ListADT<T> result = new ArrayListImpl<>();
        int i = 0, j = 0;
        
        while (i < left.size() && j < right.size()) {
            if (left.get(i).compareTo(right.get(j)) <= 0) {
                result.add(left.get(i));
                i++;
            } else {
                result.add(right.get(j));
                j++;
            }
        }
        while (i < left.size()) {
            result.add(left.get(i));
            i++;
        }
        while (j < right.size()) {
            result.add(right.get(j));
            j++;
        }
        return result;
    }
}
