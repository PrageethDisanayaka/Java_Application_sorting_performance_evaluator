package sortapp;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
public final class CsvLoader {
    public Dataset load(Path file) throws IOException {
        if (!file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv"))
            throw new IOException("Please select a .csv file.");
        if (Files.size(file) > 50L*1024*1024) throw new IOException("Maximum supported file size is 50 MB.");
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parse(reader);
        }
    }
    /** Strict comma-separated UTF-8 CSV: quoted commas, escaped quotes and newlines. */
    public Dataset parse(Reader source) throws IOException {
        PushbackReader in = new PushbackReader(source, 1);
        List<String[]> records = new ArrayList<>(); List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false, closed = false, started = false, first = true;
        int n;
        while ((n = in.read()) != -1) {
            char c = (char)n;
            if (first) { first = false; if (c == '\uFEFF') continue; }
            if (quoted) {
                if (c == '"') {
                    int next = in.read();
                    if (next == '"') field.append('"');
                    else { quoted = false; closed = true; if (next != -1) in.unread(next); }
                } else field.append(c);
                continue;
            }
            if (closed && c != ',' && c != '\r' && c != '\n')
                throw new IOException("Unexpected character after closing quote.");
            if (c == '"') {
                if (started || field.length() > 0) throw new IOException("Quote inside an unquoted field.");
                quoted = true; started = true;
            } else if (c == ',') {
                row.add(field.toString()); field.setLength(0); closed = false; started = false;
            } else if (c == '\r' || c == '\n') {
                if (c == '\r') { int next = in.read(); if (next != '\n' && next != -1) in.unread(next); }
                if (started || closed || field.length() > 0 || !row.isEmpty()) {
                    row.add(field.toString()); records.add(row.toArray(new String[0]));
                }
                row = new ArrayList<>(); field.setLength(0); closed = false; started = false;
            } else { field.append(c); started = true; }
        }
        if (quoted) throw new IOException("Unclosed quoted field.");
        if (started || closed || field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString()); records.add(row.toArray(new String[0]));
        }
        if (records.isEmpty()) throw new IOException("The CSV is empty.");
        String[] headers = records.remove(0); Set<String> names = new HashSet<>();
        for (int c = 0; c < headers.length; c++) {
            headers[c] = headers[c].trim();
            if (headers[c].isEmpty() || !names.add(headers[c])) throw new IOException("Headers must be nonempty and unique.");
        }
        if (records.isEmpty()) throw new IOException("The CSV contains headers but no data.");
        for (int r = 0; r < records.size(); r++)
            if (records.get(r).length != headers.length) throw new IOException("Incorrect number of fields at data row "+(r+1)+".");
        return new Dataset(headers, records);
    }
}
