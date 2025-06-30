package tes.BookingApp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class CustomerListForm extends JPanel {
    JTable customerTable;

    public CustomerListForm() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Tambah Customer");
        topPanel.add(addButton);
        add(topPanel, BorderLayout.NORTH);

        customerTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        loadCustomerData();

        addButton.addActionListener(e -> showAddCustomerDialog());
    }

    private void loadCustomerData() {
        try {
            URL url = new URL("http://localhost:3000/api/customers");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuffer = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBuffer.append(line);
            }
            in.close();

            JSONObject response = new JSONObject(responseBuffer.toString());
            JSONArray customers = response.getJSONArray("data");

            String[] columnNames = { "ID", "Nama", "Email", "Telepon" };
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);

            for (int i = 0; i < customers.length(); i++) {
                JSONObject customer = customers.getJSONObject(i);
                Object[] row = {
                    customer.getString("id"),
                    customer.getString("name"),
                    customer.getString("email"),
                    customer.getString("phone")
                };
                model.addRow(row);
            }

            customerTable.setModel(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data customer: " + e.getMessage());
        }
    }

    private void showAddCustomerDialog() {
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField phoneField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nama:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Telepon:"));
        panel.add(phoneField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tambah Customer Baru", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            addCustomer(nameField.getText(), emailField.getText(), new String(passwordField.getPassword()), phoneField.getText());
        }
    }

    private void addCustomer(String name, String email, String password, String phone) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field wajib diisi!");
            return;
        }

        try {
            URL url = new URL("http://localhost:3000/api/customer/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = String.format(
                "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"phone\":\"%s\"}",
                name, email, password, phone
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                JOptionPane.showMessageDialog(this, "Customer berhasil ditambahkan!");
                loadCustomerData(); // reload table
            } else if (responseCode == 409) {
                JOptionPane.showMessageDialog(this, "Email sudah digunakan!");
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String response = reader.readLine();
                JOptionPane.showMessageDialog(this, "Gagal menambahkan customer. " + response);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
