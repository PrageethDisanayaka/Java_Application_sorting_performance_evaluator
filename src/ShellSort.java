package sortapp;
public final class ShellSort implements SortAlgorithm {
    public String name() { return "Shell Sort"; }
    public void sort(int[] a, double[] k) {
        // Gapped insertion passes; the last gap of one completes the ordering.
        for (int gap = a.length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < a.length; i++) {
                int value = a[i], j = i;
                while (j >= gap && SortAlgorithm.compare(a[j-gap], value, k) > 0) {
                    a[j] = a[j-gap]; j -= gap;
                }
                a[j] = value;
            }
        }
    }
}
