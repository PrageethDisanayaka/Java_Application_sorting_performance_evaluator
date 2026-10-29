package sortapp;
import java.io.*;
import java.util.*;
import java.nio.file.*;
/** No external testing framework required. Run test.bat or sh test.sh. */
public final class AppTests {
    private static int checks;
    static int checkCount() { return checks; }
    static void check(boolean condition,String message) {
        checks++; if (!condition) throw new AssertionError(message);
    }
    interface Action { void run() throws Exception; }
    static void rejects(Action action,String message) throws Exception {
        try { action.run(); } catch (IOException | IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError("Expected rejection: "+message);
    }
    public static void main(String[] args) throws Exception {
        Random random=new Random(4302);
        for (SortAlgorithm algorithm:BenchmarkService.algorithms()) {
            double[][] fixtures={{},{7},{3,2,1},{1,2,3},{2,2,2},{-4,3.5,0,-4,1e8},{-0.0,0.0,-1,1}};
            for(double[] values:fixtures) verify(algorithm,values);
            for(int trial=0;trial<150;trial++) {
                double[] values=new double[random.nextInt(250)];
                for(int i=0;i<values.length;i++) values[i]=(random.nextInt(101)-50)/3.0;
                verify(algorithm,values);
            }
        }
        CsvLoader csv=new CsvLoader();
        Dataset d=csv.parse(new StringReader("\uFEFFName,Score\r\n\"A, B\",-2.5\r\n\"C \"\"D\"\"\",1e3\r\n\"two\nlines\",0"));
        check(d.rowCount()==3,"CSV count"); check(d.cell(0,0).equals("A, B"),"Quoted comma");
        check(d.cell(1,0).equals("C \"D\""),"Escaped quote"); check(d.cell(2,0).equals("two\nlines"),"Multiline");
        check(Arrays.equals(d.numericColumn(1),new double[]{-2.5,1000,0}),"Numeric parsing");
        rejects(()->d.numericColumn(0),"Text column");
        for(String invalid:new String[]{"","A,B\n","A,A\n1,2","A,\n1,2","A,B\n1","A\n\"unclosed","A\n\"x\"junk","A\na\"b"})
            rejects(()->csv.parse(new StringReader(invalid)),"Invalid CSV");
        for(String invalid:new String[]{"","NaN","Infinity","1e999","abc","0x1.0p0"}) {
            Dataset bad=new Dataset(new String[]{"Value"},Collections.singletonList(new String[]{invalid}));
            rejects(()->bad.numericColumn(0),"Invalid numeric");
        }
        Dataset trailing=csv.parse(new StringReader("A,B\n1,\n\n"));
        check(trailing.cell(0,1).isEmpty(),"Trailing blank retained");
        rejects(()->trailing.numericColumn(1),"Empty numeric column");
        Path temp=Files.createTempFile("csv-test-",".txt");
        try { rejects(()->csv.load(temp),"Wrong extension"); } finally { Files.delete(temp); }
        double[] input={8,2,-3,2,0}; double[] copy=input.clone();
        List<String> progress = new ArrayList<>();
        List<BenchmarkResult> results=new BenchmarkService().run(input,3,progress::add);
        check(progress.size()==5,"One progress message per algorithm");
        for (int i=0;i<5;i++) check(progress.get(i).startsWith("Algorithm "+(i+1)+" of 5: "+results.get(i).algorithm),"Numbered algorithm progress");
        check(results.size()==5,"All algorithms measured"); check(Arrays.equals(input,copy),"Original preserved");
        for(BenchmarkResult r:results) { check(r.medianNanos>=0,"Timing"); BenchmarkService.validate(r.sortedRows,input); }
        rejects(()->new BenchmarkService().run(new double[0],3,s->{}),"Empty benchmark");
        rejects(()->new BenchmarkService().run(new double[]{Double.NaN},3,s->{}),"Nonfinite benchmark");
        Dataset students=csv.load(Paths.get("data/students.csv"));
        double[] marks=students.numericColumn(3);
        for(BenchmarkResult r:new BenchmarkService().run(marks,3,s->{})) {
            check(students.cell(r.sortedRows[0],1).equals("Kasun"),"Whole row association");
            check(students.cell(r.sortedRows[r.sortedRows.length-1],1).equals("Dilini"),"Maximum row");
        }
        String original="Hello, \"world\"\nNext line";
        Dataset roundTrip=csv.parse(new StringReader("Text,Value\n"+MainFrame.escape(original)+",2\n"));
        check(roundTrip.cell(0,0).equals(original),"Export escaping round trip");
        // Duplicate keys must retain every distinct record, including quoted/multiline fields.
        Dataset records=csv.parse(new StringReader("ID,Score,Note\na,-2.5,\"first, note\"\nb,3.25,second\nc,-2.5,\"two\nlines\"\nd,0,last\n"));
        for (SortAlgorithm algorithm:BenchmarkService.algorithms()) {
            double[] recordKeys=records.numericColumn(1);
            int[] order=BenchmarkService.identity(records.rowCount()); algorithm.sort(order,recordKeys);
            BenchmarkService.validate(order,recordKeys);
            StringBuilder exported=new StringBuilder("ID,Score,Note\n");
            for(int r:order) {
                for(int c=0;c<records.columnCount();c++) { if(c>0) exported.append(','); exported.append(MainFrame.escape(records.cell(r,c))); }
                exported.append('\n');
            }
            Dataset restored=csv.parse(new StringReader(exported.toString()));
            Set<String> ids=new HashSet<>();
            for(int r=0;r<order.length;r++) {
                ids.add(restored.cell(r,0));
                for(int c=0;c<records.columnCount();c++) check(restored.cell(r,c).equals(records.cell(order[r],c)),algorithm.name()+" complete record round trip");
            }
            check(ids.size()==records.rowCount(),algorithm.name()+" duplicate-key records retained");
        }
        for(String invalid:new String[]{"", "text", "1e999"}) {
            Dataset bad=new Dataset(new String[]{"Score"},Arrays.asList(new String[]{"1"},new String[]{invalid}));
            try { bad.numericColumn(0); throw new AssertionError("Expected numeric rejection"); }
            catch(IllegalArgumentException e) { check(e.getMessage().contains("'Score'") && e.getMessage().contains("data row 2"),"Numeric error identifies column and row"); }
        }
        // Large benchmark exercises the same sample used in the live demonstration.
        Dataset benchmark=csv.load(Paths.get("data/benchmark-3000.csv"));
        for(BenchmarkResult r:new BenchmarkService().run(benchmark.numericColumn(1),3,s->{}))
            System.out.println(r.algorithm+": "+r.medianNanos+" ns (machine-specific)");
        System.out.println("PASS: "+checks+" checks; all five algorithms and CSV/benchmark integration passed.");
    }
    private static void verify(SortAlgorithm algorithm,double[] values) {
        double[] expected=values.clone(); Arrays.sort(expected); // Oracle only, never used by application algorithms.
        int[] order=BenchmarkService.identity(values.length); algorithm.sort(order,values);
        BenchmarkService.validate(order,values);
        double[] actual=new double[values.length]; for(int i=0;i<actual.length;i++) actual[i]=values[order[i]];
        check(Arrays.equals(expected,actual),algorithm.name()+" correctness");
    }
}
