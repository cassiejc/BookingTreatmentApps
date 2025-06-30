package tes.BookingApp;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class TreatmentDoctorForm extends JPanel {
    private JComboBox<String> doctorDropdown;
    private JComboBox<String> treatmentDropdown;
    private JButton submitButton;

    private JSONObject doctorsData = new JSONObject(); 
    private JSONObject treatmentsData = new JSONObject(); 
    public TreatmentDoctorForm() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel doctorLabel = new JLabel("Pilih Dokter:");
        JLabel treatmentLabel = new JLabel("Pilih Treatment:");
        doctorDropdown = new JComboBox<>();
        treatmentDropdown = new JComboBox<>();
        submitButton = new JButton("Hubungkan");

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(doctorLabel, gbc);
        gbc.gridx = 1;
        add(doctorDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(treatmentLabel, gbc);
        gbc.gridx = 1;
        add(treatmentDropdown, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        add(submitButton, gbc);

        loadDoctors();
        loadTreatments();

        submitButton.addActionListener(e -> addTreatmentDoctorRelation());
    }

    private void loadDoctors() {
        try {
            URL url = new URL("http://localhost:3000/api/doctors");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();

            JSONArray doctors = new JSONObject(sb.toString()).getJSONArray("data");
            doctorDropdown.removeAllItems();
            doctorsData = new JSONObject();

            for (int i = 0; i < doctors.length(); i++) {
                JSONObject doctor = doctors.getJSONObject(i);
                String name = doctor.getString("name");
                String id = doctor.getString("id");
                doctorDropdown.addItem(name);
                doctorsData.put(name, id);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data dokter: " + e.getMessage());
        }
    }

    private void loadTreatments() {
        try {
            URL url = new URL("http://localhost:3000/api/treatments");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();

            JSONArray treatments = new JSONObject(sb.toString()).getJSONArray("data");
            treatmentDropdown.removeAllItems();
            treatmentsData = new JSONObject();

            for (int i = 0; i < treatments.length(); i++) {
                JSONObject treatment = treatments.getJSONObject(i);
                String name = treatment.getString("name");
                String id = treatment.getString("id");
                treatmentDropdown.addItem(name);
                treatmentsData.put(name, id);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data treatment: " + e.getMessage());
        }
    }

    private void addTreatmentDoctorRelation() {
        String selectedDoctor = (String) doctorDropdown.getSelectedItem();
        String selectedTreatment = (String) treatmentDropdown.getSelectedItem();

        if (selectedDoctor == null || selectedTreatment == null) {
            JOptionPane.showMessageDialog(this, "Pilih dokter dan treatment terlebih dahulu.");
            return;
        }

        String doctorId = doctorsData.getString(selectedDoctor);
        String treatmentId = treatmentsData.getString(selectedTreatment);

        try {
            URL url = new URL("http://localhost:3000/api/treatment-doctor");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = String.format("{\"treatment_id\":\"%s\", \"doctor_id\":\"%s\"}", treatmentId, doctorId);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                JOptionPane.showMessageDialog(this, "Relasi berhasil ditambahkan!");
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String response = reader.readLine();
                JOptionPane.showMessageDialog(this, "Gagal menambahkan relasi: " + response);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
