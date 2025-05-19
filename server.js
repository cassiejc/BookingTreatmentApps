const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
const mysql = require('mysql');
const bcrypt = require('bcrypt');

var conn = mysql.createConnection({
    host: "localhost",
    database: "treatment_booking",  
    user: "root",      
    password: ""       
});

conn.connect(err => {
    if (err) throw err;
    console.log("Connected to MySQL server");
});

const app = express();
const port = 3000;

app.use(cors());
app.use(bodyParser.json());

app.post('/api/customer/register', async (req, res) => {
    const { name, email, password, phone } = req.body;
    if (!name || !email || !password || !phone) {
        return res.status(400).json({ message: "Semua field wajib diisi!" });
    }
    const hashedPassword = await bcrypt.hash(password, 10);
    const id = uuidv4();
    const query = 'INSERT INTO customers (id, name, email, password, phone) VALUES (?, ?, ?, ?, ?)';
    conn.query(query, [id, name, email, hashedPassword, phone], (err) => {
        if (err) {
            if (err.code === 'ER_DUP_ENTRY') {
                return res.status(409).json({ message: 'Email sudah digunakan' });
            }
            return res.status(500).json({ message: 'Gagal registrasi', error: err });
        }
        res.status(201).json({ message: "Registrasi berhasil", data: { id, name, email, phone } });
    });
});

app.post('/api/customer/login', (req, res) => {
    const {email, password} = req.body;
    if(!email || !password) {
        return res.status(400).json({message: "Email dan password wajib diisi!"});
    }
    const query = 'SELECT * FROM customers WHERE email = ?';
    conn.query(query, [email], async (err, results) => {
        if(err) return res.status(500).json({message: 'Terjadi kesalahan', error: err});
        if (results.length === 0) {
            return res.status(401).json({message: "Email tidak ditemukan"});
        }
        const user = results[0];
        const isPasswordValid = await bcrypt.compare(password, user.password);
        if (!isPasswordValid) {
            return res.status(401).json({message: "Password salah"});
        }
        res.json({message: "Login berhasil", data: {id: user.id, name: user.name, email: user.email}});
    });
});

app.put('/api/customer/reset-password', async (req,res) => {
    const {email, newPassword} = req.body;

    if (!email || !newPassword) {
        return res.status(400).json({message: 'Email dan password baru wajib diisi!'});
    }
    try{
        const hashedPassword = await bcrypt.hash(newPassword, 10);
        const query = 'UPDATE customers SET password = ? WHERE email = ?';

        conn.query(query, [hashedPassword, email], (err,result) => {
            if (err) {
                return res.status(500).json({message: 'Terjadi keslaahan saat mengganti password', error: err});
            }
            if (result.affectedRows === 0) {
                return res.status(404).json({message: 'Email tidak ditemukan'});
            }
            res.json({message: 'Password customer berhasil direset'});
        });
    } catch (err) {
        res.status(500).json({message: 'Kesalahan server', error: err});
    }
});

app.post('/api/admin/register', async (req,res) => {
    const {name, email, password} = req.body;

    if (!name || !email || !password) {
        return res.status(400).json({message: 'Nama, email, dan password wajib diisi!'});
    }
    try {
        const hashedPassword = await bcrypt.hash(password, 10);
        const adminId = uuidv4();
        const query = 'INSERT INTO admins (id, name, email, password) VALUES (?, ?, ?, ?)';
        conn.query(query, [adminId, name, email, hashedPassword], (err) => {
            if (err) {
                if (err.code === 'ER_DUP_ENTRY') {
                    return res.status(409).json({message: 'Email sudah terdaftar'});
                }
                return res.status(500).json({message: 'Gagal registrasi admin', error:err});
            }
            res.status(201).json({message: 'Admin berhasil didaftarkan', data: {id: adminId, name, email}});
        });
    } catch (err) {
        res.status(500).json({message: 'Terjadi kesalahan pada server', error: err});
    }
});

app.post('/api/admin/login', (req, res) => {
    const {email,password} = req.body;
    if (!email || !password) {
        return res.status(400).json({message: 'Email dan password wajib diisi!'});
    }
    const query = 'SELECT * FROM admins WHERE email = ?';
    conn.query(query, [email], async (err, results) => {
        if (err) return res.status(500).json({message: 'Terjadi kesalahan saat login', error: err});
        if (results.length === 0) return res.status(401).json({message: 'Email tidak ditemukan'});
        const admin = results[0];
        const isMatch = await bcrypt.compare(password, admin.password);
        if (!isMatch) return res.status(401).json({message: 'Password salah!'});  
        res.json({
            message: 'Login berhasil',
            data: {id: admin.id, name: admin.name, email: admin.email}
        });
    });
});;

app.put('/api/admin/reset-password', async (req,res) => {
    const {email, newPassword} = req.body;
    if (!email || !newPassword) {
        return res.status(400).json({message: 'Email dan password baru wajib diisi!'});
    }
    try {
        const hashedPassword = await bcrypt.hash(newPassword, 10);
        const query = 'UPDATE admins SET password = ? WHERE email = ?';
        conn.query(query, [hashedPassword, email], (err, result) => {
            if (err) {
                return res.status(500).json({message: 'Terjadi kesalahan saat mengganti password', error: err});
            }
            if (result.affectedRows === 0) {
                return res.status(404).json({message: 'Email tidak ditemukan'});
            }
            res.json({message: 'Password admin berhasil direset'});
        });
    } catch (err) {
        res.status(500).json({message: 'Kesalahan server', error: err});
    }
});

app.post('/api/treatments', (req, res) => {
    const { name, description } = req.body;
    if (!name || !description) {
        return res.status(400).json({ message: 'Nama dan deskripsi treatment wajib diisi!' });
    }
    const id = uuidv4();
    const query = 'INSERT INTO treatments (id, name, description) VALUES (?, ?, ?)';

    conn.query(query, [id, name, description], (err, result) => {
        if (err) {
            return res.status(500).json({ message: 'Gagal menambahkan treatment', error: err });
        }
        res.status(201).json({
            message: 'Treatment berhasil ditambahkan',
            data: { id, name, description }
        });
    });
});

app.get('/api/treatments', (req, res) => {
    const query = 'SELECT * FROM treatments';
    conn.query(query, (err, results) => {
        if (err) return res.status(500).json({ error: err });
        res.json({ data: results });
    });
});

app.post('/api/doctors', (req, res) => {
    const { name, specialization } = req.body;
    if (!name || !specialization) {
        return res.status(400).json({ message: 'Nama dan spesialisasi dokter wajib diisi!' });
    }
    const newDoctorId = uuidv4(); 
    const query = 'INSERT INTO doctors (id, name, specialization) VALUES (?, ?, ?)';
    conn.query(query, [newDoctorId, name, specialization], (err, result) => {
        if (err) {
            return res.status(500).json({ message: 'Terjadi kesalahan saat menyimpan dokter', error: err });
        }
        res.status(201).json({ message: 'Dokter berhasil ditambahkan', data: { id: newDoctorId, name, specialization } });
    });
});

app.get('/api/doctors', (req, res) => {
    const query = 'SELECT * FROM doctors';
    conn.query(query, (err, results) => {
        if (err) {
            return res.status(500).json({ message: 'Terjadi kesalahan saat mengambil data dokter', error: err });
        }
        res.json({ data: results });
    });
});

app.post('/api/treatment-doctor', (req, res) => {
    const { treatment_id, doctor_id } = req.body;

    if (!treatment_id || !doctor_id) {
        return res.status(400).json({ message: 'Treatment ID dan Doctor ID wajib diisi!' });
    }
    const query = 'INSERT INTO treatment_doctor (treatment_id, doctor_id) VALUES (?, ?)';
    conn.query(query, [treatment_id, doctor_id], (err, result) => {
        if (err) {
            return res.status(500).json({ message: 'Gagal menambahkan relasi treatment-doctor', error: err });
        }
        res.status(201).json({ message: 'Relasi treatment-doctor berhasil ditambahkan' });
    });
});

app.get('/api/treatments/:id/doctors', (req, res) => {
    const query = `
        SELECT d.*
        FROM doctors d
        JOIN treatment_doctor td ON d.id = td.doctor_id
        WHERE td.treatment_id = ?
    `;
    conn.query(query, [req.params.id], (err, results) => {
        if (err) return res.status(500).json({ error: err });
        res.json({ data: results });
    });
});

app.post('/api/schedule', (req,res) => {
    const {doctor_id, tanggal, time_start, time_end} = req.body;
    if (!doctor_id || !tanggal || !time_start || !time_end) {
        return res.status(400).json({message: 'Semua field wajib diisi!'});
    }
    const id = uuidv4();
    const query = 'INSERT INTO schedules (id, doctor_id, tanggal, time_start, time_end) VALUES (?, ?, ?, ?, ?)';
    conn.query(query, [id, doctor_id, tanggal, time_start, time_end], (err, result) => {
        if (err) {
            return res.status(500).json({message: 'Terjadi kesalahan saat menambahkan schedule', error: err});
        }
        res.status(201).json({message: 'Schedule berhasil ditambahkan', data: {id, doctor_id, tanggal, time_start, time_end}});
    });
});

app.get('/api/doctors/:id/schedules', (req, res) => {
    const query = 'SELECT * FROM schedules WHERE doctor_id = ? ORDER BY tanggal, time_start';
    conn.query(query, [req.params.id], (err, results) => {
        if (err) return res.status(500).json({ error: err });
        res.json({ data: results });
    });
});

app.post('/api/appointments', (req, res) => {
    const { treatment_id, doctor_id, schedule_id, customer_id } = req.body;
    if (!treatment_id || !doctor_id || !schedule_id || !customer_id) {
        return res.status(400).json({ message: 'Semua field wajib diisi!' });
    }
    const customerQuery = `SELECT name FROM customers WHERE id = ?`;
    conn.query(customerQuery, [customer_id], (err, customerResult) => {
        if (err) return res.status(500).json({ message: 'Gagal mengambil data customer', error: err });
        if (customerResult.length === 0) return res.status(404).json({ message: 'Customer tidak ditemukan' });
        const patient_name = customerResult[0].name;
        const checkQuery = `
            SELECT 1 FROM treatment_doctor
            WHERE treatment_id = ? AND doctor_id = ?
        `;
        conn.query(checkQuery, [treatment_id, doctor_id], (err, result) => {
            if (err) return res.status(500).json({ message: 'Gagal validasi treatment dan dokter', error: err });
            if (result.length === 0) {
                return res.status(400).json({ message: 'Dokter tidak menangani treatment ini' });
            }
            const scheduleQuery = `
                SELECT * FROM schedules
                WHERE id = ? AND doctor_id = ?
            `;
            conn.query(scheduleQuery, [schedule_id, doctor_id], (err, result2) => {
                if (err) return res.status(500).json({ message: 'Gagal validasi schedule', error: err });
                if (result2.length === 0) {
                    return res.status(400).json({ message: 'Schedule tidak cocok dengan dokter ini' });
                }
                const conflictQuery = `
                    SELECT 1 FROM appointments
                    WHERE doctor_id = ? AND schedule_id = ?
                `;
                conn.query(conflictQuery, [doctor_id, schedule_id], (err, conflicts) => {
                    if (err) return res.status(500).json({ message: 'Gagal cek konflik jadwal', error: err });
                    if (conflicts.length > 0) {
                        return res.status(400).json({ message: 'Jadwal sudah diambil oleh pasien lain untuk dokter ini' });
                    }
                    const id = uuidv4();
                    const insertQuery = `
                        INSERT INTO appointments (id, patient_name, treatment_id, doctor_id, schedule_id, customer_id)
                        VALUES (?, ?, ?, ?, ?, ?)
                    `;
                    conn.query(
                        insertQuery,
                        [id, patient_name, treatment_id, doctor_id, schedule_id, customer_id],
                        (err, result3) => {
                            if (err) return res.status(500).json({ message: 'Gagal menyimpan appointment', error: err });
                            res.status(201).json({
                                message: 'Appointment berhasil dibuat',
                                data: { id, patient_name, treatment_id, doctor_id, schedule_id, customer_id }
                            });
                        }
                    );
                });
            });
        });
    });
});

app.get('/api/appointments', (req, res) => {
    const query = `
        SELECT a.id, a.patient_name,
               t.name AS treatment_name,
               d.name AS doctor_name,
               s.tanggal, s.time_start, s.time_end
        FROM appointments a
        JOIN treatments t ON a.treatment_id = t.id
        JOIN doctors d ON a.doctor_id = d.id
        JOIN schedules s ON a.schedule_id = s.id
    `;
    conn.query(query, (err, results) => {
        if (err) {
            return res.status(500).json({ message: 'Gagal mengambil data appointments', error: err });
        }
        const formattedResults = results.map(a => ({
            ...a,
            tanggal: new Date(a.tanggal).toISOString().split('T')[0]
        }));
        res.json({ data: formattedResults });
    });
});

app.get('/api/customer/:customerId/appointments', (req, res) => {
    const { customerId } = req.params;
    const query = `
        SELECT a.id, a.patient_name,
               t.name AS treatment_name,
               d.name AS doctor_name,
               s.tanggal, s.time_start, s.time_end
        FROM appointments a
        JOIN treatments t ON a.treatment_id = t.id
        JOIN doctors d ON a.doctor_id = d.id
        JOIN schedules s ON a.schedule_id = s.id
        WHERE a.customer_id = ?
        ORDER BY s.tanggal DESC
    `;

    conn.query(query, [customerId], (err, results) => {
        if (err) {
            return res.status(500).json({ message: 'Gagal mengambil riwayat appointment', error: err });
        }
        const formattedResults = results.map(a => ({
            ...a,
            tanggal: new Date(a.tanggal).toISOString().split('T')[0] 
        }));
        res.json({ data: formattedResults });
    });
});

app.listen(port, () => {
    console.log(`Server berjalan di http://localhost:${port}`);
});

process.on('SIGINT', () => {
    conn.end(err => {
        if (err) {
            console.error('Error closing the database connection:', err);
        } else {
            console.log('Database connection closed.');
        }
        process.exit();
    });
});
