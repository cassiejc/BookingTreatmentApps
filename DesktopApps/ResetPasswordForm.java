package tes.BookingApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ResetPasswordForm extends JFrame {

    private JTextField emailField;
    private JPasswordField newPasswordField;

    public ResetPasswordForm() {
        setTitle("Reset Password");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        emailField = new JTextField();
        newPasswordField = new JPasswordField();

        JButton submitButton = new JButton("Reset Password");
        submitButton.addActionListener(this::handleReset);

        add(new JLabel("Email:"));
        add(emailField);
        add(new JLabel("Password Baru:"));
        add(newPasswordField);
        add(submitButton);

        setVisible(true);
    }

    private void handleReset(ActionEvent e) {
        try {
            String email = emailField.getText();
            String newPassword = new String(newPasswordField.getPassword());

            if (email.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field wajib diisi!");
                return;
            }

            URL url = new URL("http://localhost:3000/api/admin/reset-password");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = String.format("{\"email\":\"%s\", \"newPassword\":\"%s\"}", email, newPassword);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                JOptionPane.showMessageDialog(this, "Password berhasil direset.");
                dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "Gagal reset password. Cek email Anda.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + ex.getMessage());
        }
    }
}

