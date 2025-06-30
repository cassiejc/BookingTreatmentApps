package tes.BookingApp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class AppointmentListForm extends JPanel {
    JTable table;
    DefaultTableModel model;
    JButton addButton;

    public AppointmentListForm() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{
                "ID", "Nama Pasien", "Treatment", "Dokter", "Tanggal", "Mulai", "Selesai"
        }, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        addButton = new JButton("Tambah Appointment");
        add(addButton, BorderLayout.SOUTH);

        addButton.addActionListener(e -> showAddDialog());

        loadAppointments();
    }

    private void loadAppointments() {
        try {
            URL url = new URL("http://localhost:3000/api/appointments");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            Scanner sc = new Scanner(conn.getInputStream());
            StringBuilder sb = new StringBuilder();
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }
            sc.close();

            JSONObject response = new JSONObject(sb.toString());
            JSONArray data = response.getJSONArray("data");

            model.setRowCount(0); // clear table
            for (int i = 0; i < data.length(); i++) {
                JSONObject a = data.getJSONObject(i);
                model.addRow(new Object[]{
                        a.getString("id"),
                        a.getString("patient_name"),
                        a.getString("treatment_name"),
                        a.getString("doctor_name"),
                        a.getString("tanggal"),
                        a.getString("time_start"),
                        a.getString("time_end")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data appointment: " + e.getMessage());
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tambah Appointment", true);
        dialog.setSize(400, 400);
        dialog.setLayout(new GridLayout(8, 2, 5, 5));
        dialog.setLocationRelativeTo(this);

        JTextField tfNama = new JTextField();
        JTextField tfTreatment = new JTextField();
        JTextField tfDokter = new JTextField();
        JTextField tfTanggal = new JTextField(); 
        JTextField tfMulai = new JTextField();   
        JTextField tfSelesai = new JTextField(); 

        dialog.add(new JLabel("Nama Pasien:"));
        dialog.add(tfNama);
        dialog.add(new JLabel("Treatment:"));
        dialog.add(tfTreatment);
        dialog.add(new JLabel("Dokter:"));
        dialog.add(tfDokter);
        dialog.add(new JLabel("Tanggal (YYYY-MM-DD):"));
        dialog.add(tfTanggal);
        dialog.add(new JLabel("Jam Mulai (HH:mm):"));
        dialog.add(tfMulai);
        dialog.add(new JLabel("Jam Selesai (HH:mm):"));
        dialog.add(tfSelesai);

        JButton btnSimpan = new JButton("Simpan");
        JButton btnBatal = new JButton("Batal");
        dialog.add(btnSimpan);
        dialog.add(btnBatal);

        btnSimpan.addActionListener(e -> {
            try {
                JSONObject newAppointment = new JSONObject();
                newAppointment.put("patient_name", tfNama.getText());
                newAppointment.put("treatment_name", tfTreatment.getText());
                newAppointment.put("doctor_name", tfDokter.getText());
                newAppointment.put("tanggal", tfTanggal.getText());
                newAppointment.put("time_start", tfMulai.getText());
                newAppointment.put("time_end", tfSelesai.getText());

                URL url = new URL("http://localhost:3000/api/appointments");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(newAppointment.toString().getBytes());
                os.flush();
                os.close();

                if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201) {
                    JOptionPane.showMessageDialog(dialog, "Berhasil menambahkan appointment.");
                    dialog.dispose();
                    loadAppointments();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Gagal menambahkan appointment: " + conn.getResponseCode());
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        btnBatal.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}
