package sortapp;
public final class QuickSort implements SortAlgorithm {
    public String name() { return "Quick Sort"; }
    public void sort(int[] a, double[] k) { quick(a, k, 0, a.length-1); }
    private void quick(int[] a, double[] k, int lo, int hi) {
        while (lo < hi) {
            // Three-way partition handles repeated keys efficiently.
            double pivot = k[a[lo + (hi-lo)/2]];
            int lt = lo, i = lo, gt = hi;
            while (i <= gt) {
                int c = Double.compare(k[a[i]], pivot);
                if (c < 0) SortAlgorithm.swap(a, lt++, i++);
                else if (c > 0) SortAlgorithm.swap(a, i, gt--);
                else i++;
            }
            // Recurse on the smaller side to bound stack depth.
            if (lt-lo < hi-gt) { quick(a, k, lo, lt-1); lo = gt+1; }
            else { quick(a, k, gt+1, hi); hi = lt-1; }
        }
    }
}
