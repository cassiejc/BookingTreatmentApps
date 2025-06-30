package tes.BookingApp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class TreatmentDoctorListForm extends JPanel {
    private JComboBox<TreatmentItem> treatmentComboBox;
    private JTable doctorTable;
    private DefaultTableModel tableModel;

    public TreatmentDoctorListForm() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Pilih Treatment:"));

        treatmentComboBox = new JComboBox<>();
        topPanel.add(treatmentComboBox);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Nama", "Spesialisasi"});
        doctorTable = new JTable(tableModel);
        add(new JScrollPane(doctorTable), BorderLayout.CENTER);

        loadTreatments();

        treatmentComboBox.addActionListener(e -> {
            TreatmentItem selected = (TreatmentItem) treatmentComboBox.getSelectedItem();
            if (selected != null) {
                loadDoctorsForTreatment(selected.id);
            }
        });
    }

    private void loadTreatments() {
        try {
            URL url = new URL("http://localhost:3000/api/treatments");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject response = new JSONObject(sb.toString());
            JSONArray data = response.getJSONArray("data");

            treatmentComboBox.removeAllItems();
            for (int i = 0; i < data.length(); i++) {
                JSONObject treatment = data.getJSONObject(i);
                String id = treatment.getString("id");  
                String name = treatment.getString("name");
                treatmentComboBox.addItem(new TreatmentItem(id, name));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat treatment: " + ex.getMessage());
        }
    }

    private void loadDoctorsForTreatment(String treatmentId) {
        try {
            URL url = new URL("http://localhost:3000/api/treatments/" + treatmentId + "/doctors");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            String jsonResponse = sb.toString();
            System.out.println("Response Dokter: " + jsonResponse);  

            JSONObject response = new JSONObject(jsonResponse);
            JSONArray data = response.getJSONArray("data");

            tableModel.setRowCount(0); 

            for (int i = 0; i < data.length(); i++) {
                JSONObject doctor = data.getJSONObject(i);
                Vector<Object> row = new Vector<>();
                row.add(doctor.getString("id"));               
                row.add(doctor.getString("name"));
                row.add(doctor.getString("specialization"));   
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat dokter: " + ex.getMessage());
        }
    }

    private static class TreatmentItem {
        String id;
        String name;

        public TreatmentItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Treatment & Doctor List");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);

            TreatmentDoctorListForm form = new TreatmentDoctorListForm();
            frame.setContentPane(form);
            frame.setVisible(true);
        });
    }
}
