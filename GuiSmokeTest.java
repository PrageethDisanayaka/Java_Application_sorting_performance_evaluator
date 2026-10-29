package sortapp;
import javax.swing.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
/** Optional GUI test: needs a graphical display. Run separately from AppTests. */
public final class GuiSmokeTest {
    private static MainFrame frame;
    private static Object field(String name) throws Exception {
        Field f=MainFrame.class.getDeclaredField(name); f.setAccessible(true); return f.get(frame);
    }
    private static void idle() throws Exception {
        long limit=System.currentTimeMillis()+20000;
        while(System.currentTimeMillis()<limit) {
            boolean[] busy={true};
            SwingUtilities.invokeAndWait(()->{try{busy[0]=(boolean)field("busy");}catch(Exception e){throw new RuntimeException(e);}});
            if(!busy[0]) return;
            Thread.sleep(30);
        }
        throw new AssertionError("Worker did not finish");
    }
    interface Task { void run() throws Exception; }
    private static void edt(Task task) throws Exception {
        SwingUtilities.invokeAndWait(()->{try{task.run();}catch(Exception e){throw new RuntimeException(e);}});
    }
    public static void main(String[] args) throws Exception {
        try {
            edt(()->{frame=new MainFrame();frame.setVisible(true);frame.loadFile(Paths.get("data/students.csv"));});
            idle();
            edt(()->{
                JComboBox<?> columns=(JComboBox<?>)field("columns");
                AppTests.check(columns.getItemCount()==5,"GUI columns");
                columns.setSelectedIndex(1);
                AppTests.check(!((JButton)field("run")).isEnabled(),"GUI rejects text column");
                columns.setSelectedIndex(3);
            });
            idle();
            edt(()->{
                AppTests.check(((JButton)field("run")).getText().equals("Compare Algorithms"),"Comparison button label");
                AppTests.check(((JButton)field("run")).isEnabled(),"GUI accepts numeric column");
                String context=((JLabel)field("columnInfo")).getText();
                AppTests.check(context.contains("Marks") && context.contains("Ascending") && context.contains("3 timed trials"),"Sorting context");
                AppTests.check(((String[])field("columnErrors"))[1].contains("data row 1"),"Text column profiled with row context");
                ((JButton)field("run")).doClick();
                AppTests.check(!((JButton)field("upload")).isEnabled() && !((JComboBox<?>)field("columns")).isEnabled()
                    && !((JSpinner)field("repeats")).isEnabled() && !((JButton)field("run")).isEnabled(),"Conflicting actions disabled");
            });
            idle();
            edt(()->{
                JTable table=(JTable)field("timings");
                AppTests.check(table.getRowCount()==5,"GUI five results");
                AppTests.check(((JLabel)field("winner")).getText().startsWith("Fastest this run:"),"GUI winner");
                for(int i=0;i<5;i++) {
                    table.setRowSelectionInterval(i,i);
                    JTabbedPane tabs=(JTabbedPane)field("tabs");
                    JTable sorted=(JTable)((JScrollPane)tabs.getComponentAt(1)).getViewport().getView();
                    AppTests.check(sorted.getValueAt(0,1).equals("Kasun"),"GUI sorted records");
                }
                AppTests.check(((JButton)field("export")).isEnabled(),"GUI export enabled");
                if(args.length>0) {
                    BufferedImage img=new BufferedImage(frame.getWidth(),frame.getHeight(),BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g=img.createGraphics();frame.paint(g);g.dispose();
                    ImageIO.write(img,"png",Paths.get(args[0]).toFile());
                }
                ((JComboBox<?>)field("columns")).setSelectedIndex(2);
                AppTests.check(table.getRowCount()==0,"Changing column clears old results");
                AppTests.check(!((JButton)field("export")).isEnabled(),"Stale export disabled");
            });
            idle();
            edt(()->{
                frame.loadFile(Paths.get("data/invalid-missing.csv"));
            });
            idle();
            edt(()->{
                ((JComboBox<?>)field("columns")).setSelectedIndex(1);
                AppTests.check(!((JButton)field("run")).isEnabled(),"Missing value rejected in GUI");
                frame.loadFile(Paths.get("data/benchmark-3000.csv"));
            });
            idle();
            edt(()->{
                ((JComboBox<?>)field("columns")).setSelectedIndex(1);
            });
            idle();
            edt(()->{
                ((JButton)field("run")).doClick();
            });
            idle();
            edt(()->AppTests.check(((JTable)field("timings")).getRowCount()==5,"Larger GUI benchmark"));
            edt(()->{
                ((JSpinner)field("repeats")).setValue("5");
                AppTests.check(((JTable)field("timings")).getRowCount()==0,"Changing trials clears stale results");
                AppTests.check(!((JButton)field("export")).isEnabled(),"Changing trials disables stale export");
                AppTests.check(((JLabel)field("columnInfo")).getText().contains("5 timed trials"),"Updated trial context");
                Dataset d=(Dataset)field("dataset"); double[] keys=d.numericColumn(1);
                java.util.List<BenchmarkResult> tied=new java.util.ArrayList<>();
                int i=0;
                for(SortAlgorithm algorithm:BenchmarkService.algorithms()) {
                    int[] order=BenchmarkService.identity(keys.length); algorithm.sort(order,keys);
                    tied.add(new BenchmarkResult(algorithm.name(),i++<2 ? 100 : 200,order));
                }
                frame.displayResults(tied);
                JTable table=(JTable)field("timings");
                AppTests.check(((JLabel)field("winner")).getText().contains("Insertion Sort / Shell Sort"),"Exact tied winners");
                AppTests.check(table.getValueAt(0,3).equals("PASS / fastest") && table.getValueAt(1,3).equals("PASS / fastest")
                    && table.getValueAt(2,3).equals("PASS"),"All tied rows marked");
                table.setRowSelectionInterval(4,4);
                AppTests.check(((JTabbedPane)field("tabs")).getTitleAt(1).equals("Sorted: Heap Sort"),"Selected preview independent from winners");
                AppTests.check(((JLabel)field("winner")).getText().contains("Insertion Sort / Shell Sort"),"Preview selection retains winner");
                Field chartResults=TimingChart.class.getDeclaredField("results"); chartResults.setAccessible(true);
                AppTests.check(chartResults.get(field("chart")).equals(tied),"Chart uses same measured results");
            });
            for(String invalid:new String[]{"", "A,B\n", "A,B\n1\n", "A\n\"unclosed"}) {
                Path bad=Files.createTempFile("gui-invalid-", ".csv");
                try {
                    Files.writeString(bad,invalid);
                    // Close the expected modal error automatically; this tests the real load flow.
                    boolean[] shown={false};
                    Timer dismiss=new Timer(30,e->{
                        for(java.awt.Window w:java.awt.Window.getWindows()) if(w instanceof JDialog && w.isVisible()) { shown[0]=true; w.dispose(); }
                    });
                    edt(()->{dismiss.start();frame.loadFile(bad);});
                    try { idle(); } finally { edt(dismiss::stop); }
                    edt(()->{
                        AppTests.check(shown[0],"Invalid CSV error dialog displayed");
                        AppTests.check(field("dataset")==null && ((JTable)field("timings")).getRowCount()==0,"Failed load clears data and results");
                        AppTests.check(!((JButton)field("run")).isEnabled() && !((JButton)field("export")).isEnabled()
                            && ((JButton)field("upload")).isEnabled(),"Failed load allows recovery only");
                        frame.loadFile(Paths.get("data/students.csv"));
                    });
                    idle();
                    edt(()->AppTests.check(((JButton)field("run")).isEnabled(),"Valid file recovers after error"));
                } finally { Files.delete(bad); }
            }
            System.out.println("PASS: "+AppTests.checkCount()+" GUI assertions; load, validation, comparisons, ties, previews, reset and recovery.");
        } finally { if(frame!=null) edt(()->frame.dispose()); }
    }
}
