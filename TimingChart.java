package sortapp;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Collections;
import java.util.Locale;
/** Linear chart using the exact measured medians displayed in the table. */
public final class TimingChart extends JPanel {
    private List<BenchmarkResult> results=Collections.emptyList();
    public TimingChart() {
        setBackground(Color.WHITE); setPreferredSize(new Dimension(400,190)); setMinimumSize(new Dimension(300,175));
        getAccessibleContext().setAccessibleName("Median sorting time comparison chart");
    }
    public void setResults(List<BenchmarkResult> results) { this.results=List.copyOf(results); repaint(); }
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics); Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(UiTheme.font(11,true)); g.setColor(UiTheme.MUTED); g.drawString("MEDIAN TIME  /  MILLISECONDS",0,15);
        if(results.isEmpty()) {
            g.setFont(UiTheme.font(13,false)); g.drawString("Run a comparison to see timing differences.",0,60); g.dispose(); return;
        }
        long max=1,min=Long.MAX_VALUE;
        for(BenchmarkResult r:results) { max=Math.max(max,r.medianNanos); min=Math.min(min,r.medianNanos); }
        int left=78,right=85,width=Math.max(30,getWidth()-left-right);
        int rowHeight=Math.max(24,Math.min(30,(getHeight()-35)/5));
        for(int i=0;i<results.size();i++) {
            BenchmarkResult r=results.get(i); int y=32+i*rowHeight;
            g.setFont(UiTheme.font(12,false)); g.setColor(UiTheme.INK); g.drawString(r.algorithm.replace(" Sort",""),0,y+13);
            g.setColor(UiTheme.BACKGROUND); g.fillRoundRect(left,y,width,18,6,6);
            int bar=(int)Math.round(width*(double)r.medianNanos/max);
            g.setColor(r.medianNanos==min?UiTheme.GREEN:new Color(115,147,180));
            if(bar>0)g.fillRoundRect(left,y,bar,18,6,6);
            g.setFont(UiTheme.font(11,r.medianNanos==min)); g.setColor(r.medianNanos==min?UiTheme.GREEN:UiTheme.MUTED);
            g.drawString(String.format(Locale.ROOT,"%.6f",r.medianNanos/1e6),left+width+8,y+13);
        }
        g.dispose();
    }
}
