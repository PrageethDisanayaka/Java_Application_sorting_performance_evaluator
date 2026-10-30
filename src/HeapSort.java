package sortapp;
public final class HeapSort implements SortAlgorithm {
    public String name() { return "Heap Sort"; }
    public void sort(int[] a, double[] k) {
        for (int i = a.length/2-1; i >= 0; i--) sift(a, k, a.length, i);
        for (int end = a.length-1; end > 0; end--) {
            SortAlgorithm.swap(a, 0, end); sift(a, k, end, 0);
        }
    }
    private void sift(int[] a, double[] k, int size, int root) {
        while (root < size/2) {
            int child = root*2+1;
            if (child+1 < size && SortAlgorithm.compare(a[child], a[child+1], k) < 0) child++;
            if (SortAlgorithm.compare(a[root], a[child], k) >= 0) return;
            SortAlgorithm.swap(a, root, child); root = child;
        }
    }
}
