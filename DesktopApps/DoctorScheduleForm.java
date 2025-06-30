package tes.BookingApp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Vector;
import org.json.*;
import org.jdatepicker.impl.*;

public class DoctorScheduleForm extends JPanel {
    private JComboBox<DoctorItem> doctorComboBox;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JTextField timeStartField, timeEndField;
    private JButton addScheduleButton;
    private JDatePickerImpl datePicker;

    public DoctorScheduleForm() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(new JLabel("Pilih Dokter:"));
        doctorComboBox = new JComboBox<>();
        topPanel.add(doctorComboBox);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "Tanggal", "Jam Mulai", "Jam Selesai"});
        scheduleTable = new JTable(tableModel);
        add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Tambah Jadwal Baru"));

        bottomPanel.add(new JLabel("Tanggal:"));
        datePicker = createDatePicker();
        bottomPanel.add(datePicker);

        bottomPanel.add(new JLabel("Jam Mulai (HH:MM):"));
        timeStartField = new JTextField();
        bottomPanel.add(timeStartField);

        bottomPanel.add(new JLabel("Jam Selesai (HH:MM):"));
        timeEndField = new JTextField();
        bottomPanel.add(timeEndField);

        addScheduleButton = new JButton("Tambah Jadwal");
        bottomPanel.add(addScheduleButton);

        add(bottomPanel, BorderLayout.SOUTH);

        doctorComboBox.addActionListener(e -> {
            DoctorItem selected = (DoctorItem) doctorComboBox.getSelectedItem();
            if (selected != null) loadSchedules(selected.id);
        });

        addScheduleButton.addActionListener(e -> {
            DoctorItem selected = (DoctorItem) doctorComboBox.getSelectedItem();
            if (selected != null) {
                String tanggal = getDateFromPicker();
                addSchedule(selected.id, tanggal, timeStartField.getText(), timeEndField.getText());
            } else {
                JOptionPane.showMessageDialog(this, "Pilih dokter terlebih dahulu.");
            }
        });

        loadDoctors();
    }

    private JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Hari Ini");
        p.put("text.month", "Bulan");
        p.put("text.year", "Tahun");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        return new JDatePickerImpl(datePanel, new DateComponentFormatter());
    }

    private String getDateFromPicker() {
        java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
        if (selectedDate == null) return "";
        LocalDate localDate = new java.sql.Date(selectedDate.getTime()).toLocalDate();
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE); // format: YYYY-MM-DD
    }

    private void loadDoctors() {
        try {
            URL url = new URL("http://localhost:3000/api/doctors");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject response = new JSONObject(sb.toString());
            JSONArray data = response.getJSONArray("data");

            doctorComboBox.removeAllItems();
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                doctorComboBox.addItem(new DoctorItem(obj.getString("id"), obj.getString("name")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data dokter.");
        }
    }

    private void loadSchedules(String doctorId) {
        try {
            URL url = new URL("http://localhost:3000/api/doctors/" + doctorId + "/schedules");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject response = new JSONObject(sb.toString());
            JSONArray data = response.getJSONArray("data");

            tableModel.setRowCount(0);
            for (int i = 0; i < data.length(); i++) {
                JSONObject sched = data.getJSONObject(i);
                Vector<Object> row = new Vector<>();
                row.add(sched.getString("id"));
                row.add(sched.getString("tanggal"));
                row.add(sched.getString("time_start"));
                row.add(sched.getString("time_end"));
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat jadwal.");
        }
    }

    private void addSchedule(String doctorId, String tanggal, String timeStart, String timeEnd) {
        try {
            URL url = new URL("http://localhost:3000/api/schedule");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("doctor_id", doctorId);
            body.put("tanggal", tanggal);
            body.put("time_start", timeStart);
            body.put("time_end", timeEnd);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes());
                os.flush();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject response = new JSONObject(sb.toString());
            JOptionPane.showMessageDialog(this, response.getString("message"));
            loadSchedules(doctorId);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menambahkan jadwal: " + ex.getMessage());
        }
    }

    private static class DoctorItem {
        String id;
        String name;

        DoctorItem(String id, String name) {
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
            JFrame frame = new JFrame("Menu Jadwal Dokter");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 400);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new DoctorScheduleForm());
            frame.setVisible(true);
        });
    }
}
