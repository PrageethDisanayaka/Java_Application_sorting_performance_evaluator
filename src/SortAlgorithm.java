package sortapp;
/** Each implementation sorts the supplied row indices by their numeric key. */
public interface SortAlgorithm {
    String name();
    void sort(int[] rows, double[] keys);
    static int compare(int a, int b, double[] keys) {
        return Double.compare(keys[a], keys[b]);
    }
    static void swap(int[] rows, int a, int b) {
        int t = rows[a]; rows[a] = rows[b]; rows[b] = t;
    }
}
