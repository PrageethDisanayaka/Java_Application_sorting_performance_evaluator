package sortapp;
import java.util.*;
import java.util.function.Consumer;
public final class BenchmarkService {
    public static SortAlgorithm[] algorithms() {
        return new SortAlgorithm[]{new InsertionSort(),new ShellSort(),new MergeSort(),new QuickSort(),new HeapSort()};
    }
    public static int[] identity(int size) {
        int[] rows = new int[size]; for (int i = 0; i < size; i++) rows[i] = i; return rows;
    }
    public List<BenchmarkResult> run(double[] keys, int repeats, Consumer<String> progress) {
        if (keys.length == 0 || repeats < 1) throw new IllegalArgumentException("Nonempty data and positive repetitions required.");
        for (double key : keys) if (!Double.isFinite(key)) throw new IllegalArgumentException("All keys must be finite.");
        List<BenchmarkResult> results = new ArrayList<>();
        int number = 0;
        for (SortAlgorithm algorithm : algorithms()) {
            progress.accept("Algorithm " + (++number) + " of 5: " + algorithm.name() + " (warm-up and " + repeats + " timed trials)...");
            algorithm.sort(identity(keys.length), keys); // One unmeasured warm-up.
            long[] times = new long[repeats]; int[] sorted = null;
            for (int trial = 0; trial < repeats; trial++) {
                sorted = identity(keys.length); // Fresh original ordering, outside timer.
                long start = System.nanoTime();
                algorithm.sort(sorted, keys);
                times[trial] = System.nanoTime() - start;
                validate(sorted, keys); // Verification is not timed.
            }
            Arrays.sort(times);
            results.add(new BenchmarkResult(algorithm.name(), times[repeats/2], sorted));
        }
        return results;
    }
    public static void validate(int[] rows, double[] keys) {
        if (rows.length != keys.length) throw new IllegalStateException("Incorrect result size.");
        boolean[] seen = new boolean[rows.length];
        for (int i = 0; i < rows.length; i++) {
            int r = rows[i];
            if (r < 0 || r >= rows.length || seen[r]) throw new IllegalStateException("Rows lost or duplicated.");
            seen[r] = true;
            if (i > 0 && Double.compare(keys[rows[i-1]],keys[r]) > 0) throw new IllegalStateException("Result is not sorted.");
        }
    }
}
