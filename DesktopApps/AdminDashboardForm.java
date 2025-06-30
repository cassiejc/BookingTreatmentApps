package tes.BookingApp;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardForm extends JFrame {
    CardLayout cardLayout;
    JPanel contentPanel;

    CustomerListForm customerListForm;
    TreatmentListForm treatmentListForm;
    DoctorListForm doctorListForm;
    TreatmentDoctorForm treatmentDoctorForm;
    TreatmentDoctorListForm treatmentDoctorListForm;
    DoctorScheduleForm scheduleListForm;           
    AppointmentListForm appointmentListForm;       

    public AdminDashboardForm() {
        setTitle("Admin Menu");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        JMenuItem customerMenu = new JMenuItem("Daftar Customer");
        JMenuItem treatmentMenu = new JMenuItem("Daftar Treatment");
        JMenuItem doctorMenu = new JMenuItem("Daftar Dokter");
        JMenuItem treatmentDoctorMenuItem = new JMenuItem("Hubungkan Treatment - Dokter");
        JMenuItem treatmentDoctorListMenuItem = new JMenuItem("Lihat Dokter per Treatment");
        JMenuItem scheduleMenu = new JMenuItem("Daftar Jadwal Dokter");      
        JMenuItem appointmentMenu = new JMenuItem("Daftar Appointment");      
        JMenuItem logoutMenu = new JMenuItem("Logout");

        menu.add(customerMenu);
        menu.add(treatmentMenu);
        menu.add(doctorMenu);
        menu.add(treatmentDoctorMenuItem);
        menu.add(treatmentDoctorListMenuItem);
        menu.add(scheduleMenu);      
        menu.add(appointmentMenu);   
        menu.addSeparator();
        menu.add(logoutMenu);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        customerListForm = new CustomerListForm();
        treatmentListForm = new TreatmentListForm();
        doctorListForm = new DoctorListForm();
        treatmentDoctorForm = new TreatmentDoctorForm();
        treatmentDoctorListForm = new TreatmentDoctorListForm();
        scheduleListForm = new DoctorScheduleForm();         
        appointmentListForm = new AppointmentListForm();    

        contentPanel.add(customerListForm, "customers");
        contentPanel.add(treatmentListForm, "treatments");
        contentPanel.add(doctorListForm, "doctors");
        contentPanel.add(treatmentDoctorForm, "treatmentDoctors");
        contentPanel.add(treatmentDoctorListForm, "treatmentDoctorList");
        contentPanel.add(scheduleListForm, "schedules");             
        contentPanel.add(appointmentListForm, "appointments");       

        add(contentPanel);

        customerMenu.addActionListener(e -> cardLayout.show(contentPanel, "customers"));
        treatmentMenu.addActionListener(e -> cardLayout.show(contentPanel, "treatments"));
        doctorMenu.addActionListener(e -> cardLayout.show(contentPanel, "doctors"));
        treatmentDoctorMenuItem.addActionListener(e -> cardLayout.show(contentPanel, "treatmentDoctors"));
        treatmentDoctorListMenuItem.addActionListener(e -> cardLayout.show(contentPanel, "treatmentDoctorList"));
        scheduleMenu.addActionListener(e -> cardLayout.show(contentPanel, "schedules"));           
        appointmentMenu.addActionListener(e -> cardLayout.show(contentPanel, "appointments"));     

        logoutMenu.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin logout?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginForm();
            }
        });

        setVisible(true);
    }
}
