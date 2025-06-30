package tes.BookingApp;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginForm extends JFrame {
    JTextField emailField;
    JPasswordField passwordField;

    public LoginForm() {
        setTitle("Login Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 220);
        setLocationRelativeTo(null);

        JLabel emailLabel = new JLabel("Email:");
        JLabel passwordLabel = new JLabel("Password:");
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> {
            dispose();
            new RegisterForm(); 
        });

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.add(emailLabel); formPanel.add(emailField);
        formPanel.add(passwordLabel); formPanel.add(passwordField);
        formPanel.add(loginButton); formPanel.add(registerButton);

        JLabel resetLabel = new JLabel("<HTML><U>Reset Password</U></HTML>");
        resetLabel.setForeground(Color.BLUE);
        resetLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resetLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new ResetPasswordForm(); 
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(resetLabel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void login() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            URL url = new URL("http://localhost:3000/api/admin/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode < 400) ? conn.getInputStream() : conn.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String response = reader.readLine();

            if (responseCode == 200) {
                JOptionPane.showMessageDialog(this, "Login berhasil!");
                dispose(); 
                new AdminDashboardForm();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal login: " + response);
            }

            reader.close();
            conn.disconnect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginForm::new);
    }
}
