package sortapp;
public final class InsertionSort implements SortAlgorithm {
    public String name() { return "Insertion Sort"; }
    public void sort(int[] a, double[] k) {
        // Grow a sorted prefix by shifting larger elements to the right.
        for (int i = 1; i < a.length; i++) {
            int value = a[i], j = i - 1;
            while (j >= 0 && SortAlgorithm.compare(a[j], value, k) > 0) {
                a[j + 1] = a[j]; j--;
            }
            a[j + 1] = value;
        }
    }
}
