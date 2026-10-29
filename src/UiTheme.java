package sortapp;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
/** Shared presentation settings, with no external UI dependency. */
public final class UiTheme {
    public static final Color NAVY=new Color(20,39,59), BACKGROUND=new Color(240,244,248);
    public static final Color INK=new Color(29,48,65), MUTED=new Color(93,111,129);
    public static final Color GREEN=new Color(12,112,92), MINT=new Color(119,224,193);
    public static final Color PALE_GREEN=new Color(228,247,240), LINE=new Color(219,228,236);
    public static final Color ERROR=new Color(166,52,45);
    private static boolean installed;
    private UiTheme() {}
    public static Font font(int size,boolean bold) { return new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size); }
    public static void install() {
        if(installed) return; installed=true;
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch(Exception ignored) { /* Portable Swing defaults are a safe fallback. */ }
        for(Object key:java.util.Collections.list(UIManager.getDefaults().keys()))
            if(key.toString().endsWith(".font")) UIManager.put(key,new javax.swing.plaf.FontUIResource(font(13,false)));
        UIManager.put("Panel.background",Color.WHITE); UIManager.put("Label.foreground",INK);
        UIManager.put("ComboBox.background",Color.WHITE); UIManager.put("ComboBox.selectionBackground",PALE_GREEN);
        UIManager.put("ComboBox.selectionForeground",INK); UIManager.put("TabbedPane.selected",Color.WHITE);
        UIManager.put("TabbedPane.contentAreaColor",Color.WHITE); UIManager.put("TabbedPane.focus",GREEN);
        UIManager.put("ScrollBar.width",12);
    }
    public static JLabel label(String text,int size,boolean bold) {
        JLabel l=new JLabel(text); l.setFont(font(size,bold)); l.setForeground(INK); return l;
    }
    public static JPanel card(LayoutManager layout) {
        JPanel p=new JPanel(layout); p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(LINE),new EmptyBorder(16,16,16,16))); return p;
    }
    public static void button(JButton b,boolean primary) {
        b.setFont(font(13,true)); b.setBackground(primary?GREEN:Color.WHITE);
        b.setForeground(primary?Color.WHITE:INK); b.setOpaque(true);
        b.setBorder(new CompoundBorder(new LineBorder(primary?GREEN:LINE),new EmptyBorder(10,16,10,16)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setFocusPainted(true);
    }
    public static JScrollPane scroll(JTable table) {
        JScrollPane s=new JScrollPane(table); s.setBorder(new LineBorder(LINE)); s.getViewport().setBackground(Color.WHITE); return s;
    }
    public static void table(JTable table) {
        table.setFont(font(13,false)); table.setForeground(INK); table.setRowHeight(32);
        table.setShowVerticalLines(false); table.setGridColor(LINE); table.setIntercellSpacing(new Dimension(0,1));
        table.setFillsViewportHeight(true); table.setSelectionBackground(PALE_GREEN); table.setSelectionForeground(INK);
        table.getTableHeader().setReorderingAllowed(false); table.getTableHeader().setPreferredSize(new Dimension(1,34));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean focus,int row,int col) {
                super.getTableCellRendererComponent(t,v,sel,focus,row,col);
                setFont(font(11,true)); setBackground(BACKGROUND); setForeground(MUTED);
                setBorder(new EmptyBorder(8,10,8,10)); return this;
            }
        });
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean focus,int row,int col) {
                super.getTableCellRendererComponent(t,v,sel,focus,row,col); setBorder(new EmptyBorder(6,10,6,10));
                setBackground(sel?PALE_GREEN:row%2==0?Color.WHITE:new Color(247,250,252));
                setToolTipText(v == null ? null : v.toString());
                setForeground("PASS".equals(v)?GREEN:INK); setFont(font(13,"PASS".equals(v))); return this;
            }
        });
    }
}
