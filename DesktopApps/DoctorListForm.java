package tes.BookingApp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class DoctorListForm extends JPanel {
    JTable doctorTable;

    public DoctorListForm() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Tambah Dokter");
        topPanel.add(addButton);
        add(topPanel, BorderLayout.NORTH);

        doctorTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(doctorTable);
        add(scrollPane, BorderLayout.CENTER);

        loadDoctorData();

        addButton.addActionListener(e -> showAddDoctorDialog());
    }

    private void loadDoctorData() {
        try {
            URL url = new URL("http://localhost:3000/api/doctors");
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
            JSONArray doctors = response.getJSONArray("data");

            String[] columnNames = { "ID", "Nama", "Spesialisasi" };
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);

            for (int i = 0; i < doctors.length(); i++) {
                JSONObject doctor = doctors.getJSONObject(i);
                Object[] row = {
                    doctor.getString("id"),
                    doctor.getString("name"),
                    doctor.getString("specialization")
                };
                model.addRow(row);
            }

            doctorTable.setModel(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data dokter: " + e.getMessage());
        }
    }

    private void showAddDoctorDialog() {
        JTextField nameField = new JTextField(20);
        JTextField specializationField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nama Dokter:"));
        panel.add(nameField);
        panel.add(new JLabel("Spesialisasi:"));
        panel.add(specializationField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tambah Dokter Baru", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            addDoctor(nameField.getText(), specializationField.getText());
        }
    }

    private void addDoctor(String name, String specialization) {
        if (name.isEmpty() || specialization.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field wajib diisi!");
            return;
        }

        try {
            URL url = new URL("http://localhost:3000/api/doctors");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = String.format(
                "{\"name\":\"%s\",\"specialization\":\"%s\"}",
                name, specialization
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                JOptionPane.showMessageDialog(this, "Dokter berhasil ditambahkan!");
                loadDoctorData(); // reload
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String response = reader.readLine();
                JOptionPane.showMessageDialog(this, "Gagal menambahkan dokter. " + response);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
