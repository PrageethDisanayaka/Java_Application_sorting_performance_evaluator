package sortapp;
import javax.swing.*;
public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.install();
            new MainFrame().setVisible(true);
        });
    }
}
