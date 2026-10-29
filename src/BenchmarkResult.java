package sortapp;
public final class BenchmarkResult {
    public final String algorithm;
    public final long medianNanos;
    public final int[] sortedRows;
    public BenchmarkResult(String algorithm, long medianNanos, int[] sortedRows) {
        this.algorithm = algorithm; this.medianNanos = medianNanos; this.sortedRows = sortedRows.clone();
    }
}
