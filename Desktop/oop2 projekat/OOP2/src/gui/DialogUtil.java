package gui;

import java.awt.Component;
import javax.swing.JOptionPane;

final class DialogUtil {

    private DialogUtil() {
    }

    static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            "Greška",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
