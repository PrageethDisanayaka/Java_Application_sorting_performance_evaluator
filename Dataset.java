package sortapp;
import java.util.*;
/** Owns complete rows; sorting row indices preserves each record's associations. */
public final class Dataset {
    private final String[] headers;
    private final List<String[]> rows;
    public Dataset(String[] headers, List<String[]> rows) {
        this.headers = headers.clone(); this.rows = new ArrayList<>();
        for (String[] row : rows) this.rows.add(row.clone());
    }
    public int rowCount() { return rows.size(); }
    public int columnCount() { return headers.length; }
    public String header(int c) { return headers[c]; }
    public String cell(int r, int c) { return rows.get(r)[c]; }
    public double[] numericColumn(int c) {
        if (rows.isEmpty()) throw new IllegalArgumentException("The CSV has no data rows.");
        double[] keys = new double[rows.size()];
        for (int r = 0; r < keys.length; r++) {
            String s = cell(r,c).trim();
            // Decimal/scientific notation only; reject NaN, infinity and blank cells.
            if (!s.matches("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?"))
                throw new IllegalArgumentException("Column '"+header(c)+"': missing or nonnumeric value at data row "+(r+1)+".");
            keys[r] = Double.parseDouble(s);
            if (!Double.isFinite(keys[r])) throw new IllegalArgumentException("Column '"+header(c)+"': numeric value out of range at data row "+(r+1)+".");
        }
        return keys;
    }
}
