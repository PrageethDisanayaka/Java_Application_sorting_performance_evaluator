package sortapp;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.*;

/** Swing components are updated on the event-dispatch thread; work runs in SwingWorker. */
public final class MainFrame extends JFrame {
    private final JButton upload = new JButton("1. Open CSV"), run = new JButton("Compare Algorithms"), export = new JButton("Export sorted CSV");
    private final JComboBox<String> columns = new JComboBox<>();
    private final JLabel status = new JLabel("Open a UTF-8 CSV file with a header row to begin."), winner = new JLabel("Fastest algorithm: run a comparison");
    private final JLabel fileInfo = new JLabel("No dataset loaded"), columnInfo = new JLabel("Select a numeric column.");
    private final JProgressBar progress = new JProgressBar();
    private final JTable preview = new JTable(), timings = new JTable();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JSpinner repeats = new JSpinner(new SpinnerListModel(new String[]{"3", "5", "7"}));
    private final TimingChart chart = new TimingChart();
    private final JLabel previewInfo = new JLabel("Upload a dataset to see its records");
    private Dataset dataset;
    private List<BenchmarkResult> results = Collections.emptyList();
    private double[] keys;
    private String[] columnErrors = new String[0];
    private boolean busy;
    public MainFrame() {
        super("Sorting Performance Evaluator | Programming III");
        UiTheme.install();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setMinimumSize(new Dimension(Math.min(940,screen.width),Math.min(600,screen.height)));
        setSize(Math.min(1240,screen.width),Math.min(900,screen.height));
        setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(0,0));
        root.setBackground(UiTheme.BACKGROUND); setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(16,4));
        header.setBackground(UiTheme.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(18,24,18,24));
        JPanel headings = new JPanel(new GridLayout(0,1,0,4)); headings.setOpaque(false);
        JLabel title = UiTheme.label("Sorting Performance Evaluator",24,true);
        title.setForeground(Color.WHITE);
        JLabel subtitle = UiTheme.label("PROGRAMMING III   /   CSV SORTING & ALGORITHM COMPARISON",11,false);
        subtitle.setForeground(new Color(178,199,217)); headings.add(title); headings.add(subtitle);
        header.add(headings,BorderLayout.CENTER);
        JLabel badge = UiTheme.label("5 ALGORITHMS",12,true); badge.setForeground(UiTheme.MINT);
        header.add(badge,BorderLayout.EAST); root.add(header,BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(14,14)); body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16,20,12,20));
        JPanel setup = UiTheme.card(new BorderLayout(12,10));
        JPanel controlRow = new JPanel(new GridBagLayout()); controlRow.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy=0; g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(0,0,0,14);
        upload.setText("Open CSV file"); run.setText("Compare Algorithms");
        UiTheme.button(upload,false); UiTheme.button(run,true); UiTheme.button(export,false);
        columns.setFont(UiTheme.font(13,false)); columns.setPreferredSize(new Dimension(200,36));
        columns.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                super.getListCellRendererComponent(list,value,index,selected,focus);
                int c = index < 0 ? columns.getSelectedIndex() : index;
                if (c >= 0 && c < columnErrors.length) {
                    setText(value + (columnErrors[c] == null ? " [numeric]" : " [not sortable]"));
                    setToolTipText(columnErrors[c]);
                }
                return this;
            }
        });
        repeats.setPreferredSize(new Dimension(64,36)); repeats.setFont(UiTheme.font(13,false));
        ((JSpinner.DefaultEditor)repeats.getEditor()).getTextField().setEditable(false);
        g.gridx=0; g.weightx=0; controlRow.add(step("01  DATASET",upload),g);
        g.gridx=1; g.weightx=1; controlRow.add(step("02  NUMERIC COLUMN",columns),g);
        g.gridx=2; g.weightx=0; controlRow.add(step("TIMED TRIALS",repeats),g);
        g.gridx=3; g.insets=new Insets(0,0,0,0); controlRow.add(step("03  PERFORMANCE",run),g);
        setup.add(controlRow,BorderLayout.NORTH);
        columnInfo.setFont(UiTheme.font(12,false)); columnInfo.setForeground(UiTheme.MUTED);
        // A scrollable one-line message keeps long column names from widening the window.
        JScrollPane validation = new JScrollPane(columnInfo,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        validation.setBorder(null); validation.setPreferredSize(new Dimension(100,32));
        validation.getViewport().setBackground(Color.WHITE); setup.add(validation,BorderLayout.SOUTH);
        body.add(setup,BorderLayout.NORTH);

        JPanel dataCard=UiTheme.card(new BorderLayout(0,12));
        JPanel dataHeading=new JPanel(new BorderLayout(0,6)); dataHeading.setOpaque(false);
        dataHeading.add(UiTheme.label("Dataset explorer",18,true),BorderLayout.NORTH);
        fileInfo.setFont(UiTheme.font(12,false)); fileInfo.setForeground(UiTheme.MUTED);
        dataHeading.add(fileInfo,BorderLayout.SOUTH); dataCard.add(dataHeading,BorderLayout.NORTH);
        UiTheme.table(preview); preview.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabs.setFont(UiTheme.font(12,true)); tabs.setBackground(Color.WHITE);
        tabs.addTab("Original dataset",UiTheme.scroll(preview)); dataCard.add(tabs,BorderLayout.CENTER);
        previewInfo.setFont(UiTheme.font(11,false)); previewInfo.setForeground(UiTheme.MUTED);
        dataCard.add(previewInfo,BorderLayout.SOUTH);

        JPanel comparison=UiTheme.card(new BorderLayout(0,12));
        JPanel resultHeading=new JPanel(new BorderLayout(0,6)); resultHeading.setOpaque(false);
        resultHeading.add(UiTheme.label("Performance comparison",18,true),BorderLayout.NORTH);
        winner.setFont(UiTheme.font(13,true)); winner.setForeground(UiTheme.GREEN);
        winner.setOpaque(true); winner.setBackground(UiTheme.PALE_GREEN);
        winner.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        resultHeading.add(winner,BorderLayout.CENTER); comparison.add(resultHeading,BorderLayout.NORTH);
        timings.setModel(resultsModel()); UiTheme.table(timings);
        timings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); timings.setRowHeight(28);
        timings.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JPanel evidence=new JPanel(new BorderLayout(0,12)); evidence.setOpaque(false);
        JScrollPane timingScroll=UiTheme.scroll(timings);
        timingScroll.setPreferredSize(new Dimension(430,176));
        evidence.add(timingScroll,BorderLayout.NORTH); evidence.add(chart,BorderLayout.CENTER);
        comparison.add(evidence,BorderLayout.CENTER);
        JLabel note=UiTheme.label("<html>Click a result to preview its sorted rows.<br>Median sort time after one warm-up; lower is faster.<br>Results vary by dataset and computer.</html>",11,false);
        note.setForeground(UiTheme.MUTED); comparison.add(note,BorderLayout.SOUTH);

        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,dataCard,comparison);
        split.setResizeWeight(0.51); split.setDividerLocation(570);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.48)); split.setDividerSize(12);
        split.setBorder(null); split.setOpaque(false);
        dataCard.setMinimumSize(new Dimension(360,510)); comparison.setMinimumSize(new Dimension(440,510));
        split.setPreferredSize(new Dimension(900,510));
        // Smaller laptop displays can scroll the workspace instead of losing controls.
        JScrollPane workspace=new JScrollPane(split,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        workspace.setBorder(null); workspace.getViewport().setBackground(UiTheme.BACKGROUND);
        workspace.getVerticalScrollBar().setUnitIncrement(20);
        body.add(workspace,BorderLayout.CENTER); root.add(body,BorderLayout.CENTER);

        JPanel bottom=new JPanel(new BorderLayout(12,0)); bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        status.setFont(UiTheme.font(12,false)); status.setForeground(UiTheme.MUTED);
        bottom.add(status,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); actions.setOpaque(false);
        progress.setPreferredSize(new Dimension(80,8)); progress.setForeground(UiTheme.GREEN);
        actions.add(progress); actions.add(export); bottom.add(actions,BorderLayout.EAST);
        root.add(bottom,BorderLayout.SOUTH);
        upload.addActionListener(e -> chooseFile()); columns.addActionListener(e -> selectColumn());
        run.addActionListener(e -> compare()); export.addActionListener(e -> exportCsv());
        repeats.addChangeListener(e -> { if (!busy) { clearResults(); updateContext(); updateControls(); status.setText("Trial count changed. Run a new comparison."); } });
        timings.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showSorted(); });
        clearResults(); updateControls();
    }
    private JPanel step(String text,JComponent control) {
        JPanel panel=new JPanel(new BorderLayout(0,6)); panel.setOpaque(false);
        JLabel label=UiTheme.label(text,11,true); label.setForeground(UiTheme.MUTED);
        panel.add(label,BorderLayout.NORTH); panel.add(control,BorderLayout.CENTER); return panel;
    }
    private DefaultTableModel resultsModel() {
        return new DefaultTableModel(new String[]{"Algorithm","Median (ns)","Median (ms)","Result"},0) {
            public boolean isCellEditable(int r,int c) { return false; }
        };
    }
    private void updateControls() {
        upload.setEnabled(!busy); columns.setEnabled(!busy && dataset != null); repeats.setEnabled(!busy);
        run.setEnabled(!busy && keys != null); export.setEnabled(!busy && timings.getSelectedRow() >= 0 && !results.isEmpty());
        progress.setIndeterminate(busy); progress.setVisible(busy);
    }
    private void clearResults() {
        results = Collections.emptyList(); timings.setModel(resultsModel());
        if (tabs.getTabCount() > 1) tabs.removeTabAt(1);
        winner.setText("Fastest algorithm: run a comparison");
        chart.setResults(Collections.emptyList());
    }
    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(new File("data"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)","csv"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) loadFile(chooser.getSelectedFile().toPath());
    }
    // Package visibility allows a GUI integration test to exercise the actual load flow.
    void loadFile(Path path) {
        if (busy) return;
        busy = true; keys = null; clearResults(); status.setText("Reading CSV and checking numeric columns..."); updateControls();
        new SwingWorker<Dataset,Void>() {
            private String[] errors;
            protected Dataset doInBackground() throws Exception {
                Dataset loaded = new CsvLoader().load(path);
                errors = new String[loaded.columnCount()];
                // Check every column off the UI thread; keep only errors, not a second dataset.
                for (int c=0;c<errors.length;c++) {
                    try { loaded.numericColumn(c); }
                    catch (IllegalArgumentException e) { errors[c] = e.getMessage(); }
                }
                return loaded;
            }
            protected void done() {
                try {
                    dataset = get(); columnErrors = errors; columns.removeAllItems();
                    for (int c=0;c<dataset.columnCount();c++) columns.addItem(dataset.header(c));
                    preview.setModel(dataModel(null)); setColumnWidths(preview);
                    previewInfo.setText("Preview: first "+Math.min(500,dataset.rowCount())+" of "+dataset.rowCount()+" rows | Sorting uses all rows");
                    fileInfo.setText(path.getFileName()+" | "+dataset.rowCount()+" rows | "+dataset.columnCount()+" columns");
                    status.setText("Loaded successfully. Preview displays up to 500 rows; sorting uses every row.");
                } catch (Exception e) {
                    dataset = null; keys = null; columnErrors = new String[0]; columns.removeAllItems(); preview.setModel(new DefaultTableModel());
                    fileInfo.setText("No dataset loaded"); previewInfo.setText("Upload a dataset to see its records"); showError(e);
                } finally { busy = false; selectColumn(); }
            }
        }.execute();
    }
    private void selectColumn() {
        if (busy) return;
        keys = null; clearResults(); columnInfo.setForeground(UiTheme.MUTED);
        preview.getTableHeader().repaint();
        if (dataset != null && columns.getSelectedIndex() >= 0) {
            final int c = columns.getSelectedIndex();
            if (columnErrors[c] != null) {
                columnInfo.setForeground(UiTheme.ERROR);
                columnInfo.setText(columnErrors[c]+" Select another column.");
                status.setText("Selected column cannot be sorted. Correct the CSV or select a numeric column.");
            } else {
                final Dataset snapshot = dataset;
                busy = true; columnInfo.setText("Preparing numeric column: "+snapshot.header(c));
                status.setText("Preparing numeric values..."); updateControls();
                new SwingWorker<double[],Void>() {
                    protected double[] doInBackground() { return snapshot.numericColumn(c); }
                    protected void done() {
                        try { keys = get(); updateContext(); status.setText("Ready. Compare Algorithms runs all five on the selected column."); }
                        catch (Exception e) { showError(e); }
                        finally { busy = false; updateControls(); }
                    }
                }.execute();
            }
        } else columnInfo.setText("Select a numeric column.");
        updateControls();
    }
    private void updateContext() {
        if (dataset == null || keys == null) return;
        columnInfo.setForeground(UiTheme.GREEN);
        columnInfo.setText("Column: "+dataset.header(columns.getSelectedIndex())+" | Ascending | "+dataset.rowCount()
            +" rows | Median of "+repeats.getValue()+" timed trials | Complete records preserved");
    }
    private TableModel dataModel(int[] order) {
        final Dataset snapshot = dataset;
        return new AbstractTableModel() {
            public int getRowCount() { return Math.min(snapshot.rowCount(),500); }
            public int getColumnCount() { return snapshot.columnCount(); }
            public String getColumnName(int c) { return snapshot.header(c); }
            public Object getValueAt(int r,int c) { return snapshot.cell(order == null ? r : order[r],c); }
        };
    }
    private void setColumnWidths(JTable table) {
        table.setAutoResizeMode(table.getColumnCount() <= 6 ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);
        for (int c=0;c<table.getColumnCount();c++) {
            table.getColumnModel().getColumn(c).setMinWidth(65);
            table.getColumnModel().getColumn(c).setPreferredWidth(150);
            table.getColumnModel().getColumn(c).setHeaderRenderer((t,value,selected,focus,row,column) -> {
                JLabel label = new JLabel(value.toString(),SwingConstants.CENTER);
                boolean sorting = t.convertColumnIndexToModel(column) == columns.getSelectedIndex();
                label.setOpaque(true); label.setFont(UiTheme.font(12,true));
                label.setBackground(sorting ? UiTheme.PALE_GREEN : UiTheme.BACKGROUND);
                label.setForeground(sorting ? UiTheme.GREEN : UiTheme.INK);
                if (sorting) label.setText(value+" [sort]");
                label.setToolTipText(value+(sorting ? " - selected column, ascending order" : ""));
                return label;
            });
        }
    }
    private void compare() {
        if (busy || keys == null) return;
        if (keys.length > 20000 && JOptionPane.showConfirmDialog(this,
            "Insertion Sort is quadratic. This dataset may take a long time. Continue?",
            "Large dataset",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        final double[] input = keys.clone(); final int trials = Integer.parseInt(repeats.getValue().toString());
        clearResults(); busy = true; updateControls();
        new SwingWorker<List<BenchmarkResult>,String>() {
            protected List<BenchmarkResult> doInBackground() {
                return new BenchmarkService().run(input,trials,s -> publish(s));
            }
            protected void process(List<String> messages) { status.setText(messages.get(messages.size()-1)); }
            protected void done() {
                try {
                    displayResults(get());
                    status.setText("Complete: all five outputs verified. "+trials+" timed trials per algorithm.");
                } catch (Exception e) { clearResults(); showError(e); }
                finally { busy=false; updateControls(); }
            }
        }.execute();
    }
    // Shared display path also lets tests supply exact timing ties deterministically.
    void displayResults(List<BenchmarkResult> measured) {
        results = List.copyOf(measured); DefaultTableModel model = resultsModel();
        long best = Long.MAX_VALUE;
        for (BenchmarkResult r:results) best = Math.min(best,r.medianNanos);
        for (BenchmarkResult r:results) {
            model.addRow(new Object[]{r.algorithm,r.medianNanos,String.format(Locale.ROOT,"%.6f",r.medianNanos/1e6),
                r.medianNanos == best ? "PASS / fastest" : "PASS"});
        }
        timings.setModel(model); chart.setResults(results);
        List<String> winners = new ArrayList<>(); int bestRow=0;
        for (int i=0;i<results.size();i++) if (results.get(i).medianNanos == best) {
            winners.add(results.get(i).algorithm); bestRow=i;
        }
        winner.setText("Fastest this run: "+String.join(" / ",winners)+" ("+String.format(Locale.ROOT,"%.6f",best/1e6)+" ms)");
        timings.setRowSelectionInterval(bestRow,bestRow);
    }
    private void showSorted() {
        int row = timings.getSelectedRow();
        if (row < 0 || row >= results.size()) { updateControls(); return; }
        JTable sorted = new JTable(dataModel(results.get(row).sortedRows)); UiTheme.table(sorted);
        sorted.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); setColumnWidths(sorted);
        if (tabs.getTabCount()>1) tabs.removeTabAt(1);
        tabs.addTab("Sorted: "+results.get(row).algorithm,UiTheme.scroll(sorted)); tabs.setSelectedIndex(1); updateControls();
    }
    private void exportCsv() {
        int row = timings.getSelectedRow(); if (row<0 || row>=results.size()) return;
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new File("sorted-data.csv"));
        if (chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) path=path.resolveSibling(path.getFileName()+".csv");
        if (Files.exists(path) && JOptionPane.showConfirmDialog(this,"Replace existing file?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        final Path destination=path; final Dataset snapshot=dataset; final int[] order=results.get(row).sortedRows;
        busy=true; updateControls(); status.setText("Exporting all sorted records...");
        new SwingWorker<Void,Void>() {
            protected Void doInBackground() throws Exception {
                try (BufferedWriter out=Files.newBufferedWriter(destination,StandardCharsets.UTF_8)) {
                    for (int c=0;c<snapshot.columnCount();c++) { if(c>0)out.write(','); out.write(escape(snapshot.header(c))); } out.newLine();
                    for (int r:order) {
                        for (int c=0;c<snapshot.columnCount();c++) { if(c>0)out.write(','); out.write(escape(snapshot.cell(r,c))); } out.newLine();
                    }
                }
                return null;
            }
            protected void done() {
                try { get(); status.setText("Exported "+snapshot.rowCount()+" rows to "+destination.getFileName()); }
                catch(Exception e) { showError(e); }
                finally { busy=false; updateControls(); }
            }
        }.execute();
    }
    static String escape(String s) { return "\""+s.replace("\"","\"\"")+"\""; }
    private void showError(Exception e) {
        Throwable cause=e; while(cause.getCause()!=null) cause=cause.getCause();
        status.setText("Operation failed. Check the input and try again.");
        JOptionPane.showMessageDialog(this,cause.getMessage(),"Input / operation error",JOptionPane.ERROR_MESSAGE);
    }
}
