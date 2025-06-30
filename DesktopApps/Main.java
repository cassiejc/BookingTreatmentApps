package tes.BookingApp;

// import com.formdev.flatlaf.FlatDarculaLaf;
// import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // try {
            //     UIManager.setLookAndFeel(new FlatDarculaLaf());
            // } catch (UnsupportedLookAndFeelException e) {
            //     e.printStackTrace();
            // }
            new LoginForm(); 
        });
    }
}
