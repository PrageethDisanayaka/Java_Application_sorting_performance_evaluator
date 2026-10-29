package sortapp;
public final class MergeSort implements SortAlgorithm {
    public String name() { return "Merge Sort"; }
    public void sort(int[] a, double[] k) { split(a, new int[a.length], k, 0, a.length); }
    private void split(int[] a, int[] temp, double[] k, int lo, int hi) {
        if (hi - lo < 2) return;
        int mid = lo + (hi - lo) / 2;
        split(a, temp, k, lo, mid); split(a, temp, k, mid, hi);
        int i = lo, j = mid, p = lo;
        while (i < mid && j < hi)
            temp[p++] = SortAlgorithm.compare(a[i], a[j], k) <= 0 ? a[i++] : a[j++];
        while (i < mid) temp[p++] = a[i++];
        while (j < hi) temp[p++] = a[j++];
        System.arraycopy(temp, lo, a, lo, hi-lo);
    }
}
