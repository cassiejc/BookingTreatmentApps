package tes.BookingApp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class TreatmentListForm extends JPanel {
    JTable treatmentTable;

    public TreatmentListForm() {
        setLayout(new BorderLayout());
        treatmentTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(treatmentTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("Tambah Treatment");
        addButton.addActionListener(e -> showAddDialog());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadTreatments();
    }

    private void loadTreatments() {
        try {
            URL url = new URL("http://localhost:3000/api/treatments");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(buffer.toString());
            JSONArray data = json.getJSONArray("data");

            String[] columns = { "ID", "Nama", "Deskripsi", "Harga", "Durasi (menit)" };
            DefaultTableModel model = new DefaultTableModel(columns, 0);

            for (int i = 0; i < data.length(); i++) {
                JSONObject row = data.getJSONObject(i);
                model.addRow(new Object[] {
                    row.getString("id"),
                    row.getString("name"),
                    row.getString("description"),
                    row.getInt("price"),
                    row.getInt("duration_minutes")
                });
            }

            treatmentTable.setModel(model);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat treatment: " + ex.getMessage());
        }
    }

    private void showAddDialog() {
        JTextField nameField = new JTextField();
        JTextArea descArea = new JTextArea(4, 20);
        JScrollPane descScrollPane = new JScrollPane(descArea);

        JTextField priceField = new JTextField();
        JTextField durationField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nama Treatment:"));
        panel.add(nameField);
        panel.add(new JLabel("Deskripsi:"));
        panel.add(descScrollPane);
        panel.add(new JLabel("Harga:"));
        panel.add(priceField);
        panel.add(new JLabel("Durasi (menit):"));
        panel.add(durationField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tambah Treatment", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String description = descArea.getText().trim();
            String priceText = priceField.getText().trim();
            String durationText = durationField.getText().trim();

            if (name.isEmpty() || description.isEmpty() || priceText.isEmpty() || durationText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field wajib diisi.");
                return;
            }

            try {
                int price = Integer.parseInt(priceText);
                int duration = Integer.parseInt(durationText);
                if (price <= 0 || duration <= 0) {
                    JOptionPane.showMessageDialog(this, "Harga dan durasi harus angka positif.");
                    return;
                }

                URL url = new URL("http://localhost:3000/api/treatments");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = String.format(
                    "{\"name\":\"%s\", \"description\":\"%s\", \"price\":%d, \"duration_minutes\":%d}",
                    name, description, price, duration
                );

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes());
                }

                int code = conn.getResponseCode();
                if (code == 201) {
                    JOptionPane.showMessageDialog(this, "Treatment berhasil ditambahkan.");
                    loadTreatments();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menambahkan treatment. Kode: " + code);
                }

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Harga dan durasi harus berupa angka.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
}
