package tes.BookingApp;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterForm extends JFrame {
    JTextField nameField, emailField;
    JPasswordField passwordField;

    public RegisterForm() {
        setTitle("Register Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 250);
        setLocationRelativeTo(null);

        JLabel nameLabel = new JLabel("Nama:");
        JLabel emailLabel = new JLabel("Email:");
        JLabel passwordLabel = new JLabel("Password:");

        nameField = new JTextField(20);
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);

        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back");

        registerButton.addActionListener(e -> register());
        backButton.addActionListener(e -> {
            dispose();
            new LoginForm();
        });

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(nameLabel); panel.add(nameField);
        panel.add(emailLabel); panel.add(emailField);
        panel.add(passwordLabel); panel.add(passwordField);
        panel.add(registerButton); panel.add(backButton);

        add(panel);
        setVisible(true);
    }

    private void register() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            URL url = new URL("http://localhost:3000/api/admin/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = String.format("{\"name\":\"%s\", \"email\":\"%s\", \"password\":\"%s\"}", name, email, password);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode < 400) ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String response = reader.readLine();

            JOptionPane.showMessageDialog(this, responseCode == 201 ? "Registrasi berhasil!" : "Gagal registrasi: " + response);

            reader.close();
            conn.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
