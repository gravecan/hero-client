const express = require('express');
const mysql = require('mysql2');
const bodyParser = require('body-parser');
const cors = require('cors');
const bcrypt = require('bcrypt');
const path = require('path');

const app = express();
const PORT = 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(express.static(path.join(__dirname, '../'))); // Serve static files from parent web folder

// Database Connection
const db = mysql.createPool({
    host: '102.129.137.93',      // VPS IP Address
    user: 'sql_herowin_top',
    password: 'ed06627de5f77',
    database: 'sql_herowin_top',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// Test Connection
db.getConnection((err, connection) => {
    if (err) {
        console.error('Error connecting to MySQL:', err);
    } else {
        console.log('Connected to MySQL Database!');
        connection.release();
    }
});

// Routes

// Register
app.post('/registro', async (req, res) => {
    const { username, password, plan } = req.body;

    if (!username || !password) {
        return res.status(400).json({ error: 'All fields are required' });
    }

    // Generate dummy email since DB requires it unique
    const email = `${username.toLowerCase().replace(/\s+/g, '')}@herowin.local`;

    try {
        const hash = await bcrypt.hash(password, 10);

        // Calculate expiry if needed, for now default to NULL or handle in logic
        // Plan logic: if 'lifetime', expiry is NULL or far future
        let expiry = null;
        if (plan === '1_month') {
            let d = new Date();
            d.setMonth(d.getMonth() + 1);
            expiry = d;
        }

        const sql = 'INSERT INTO users (username, email, password, plan, expiry_date) VALUES (?, ?, ?, ?, ?)';

        db.query(sql, [username, email, hash, plan || 'free', expiry], (err, result) => {
            if (err) {
                console.error(err);
                if (err.code === 'ER_DUP_ENTRY') {
                    return res.status(409).json({ error: 'Username already exists' });
                }
                return res.status(500).json({ error: 'Database error' });
            }
            res.status(201).json({ message: 'User registered successfully' });
        });
    } catch (error) {
        res.status(500).json({ error: 'Server error' });
    }
});

// Login
app.post('/login', (req, res) => {
    const { username, password } = req.body;

    if (!username || !password) {
        return res.status(400).json({ error: 'Username and password required' });
    }

    const sql = 'SELECT * FROM users WHERE username = ?';
    db.query(sql, [username], (err, results) => {
        if (err) {
            console.error(err);
            return res.status(500).json({ error: 'Database error' });
        }

        if (results.length === 0) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }

        const user = results[0];

        bcrypt.compare(password, user.password, (err, isMatch) => {
            if (err) return res.status(500).json({ error: 'Server error' });

            if (isMatch) {
                // Return user data for session
                res.json({
                    username: user.username,
                    plan: user.plan,
                    expiry: user.expiry_date,
                    message: 'Login successful'
                });
            } else {
                res.status(401).json({ error: 'Invalid credentials' });
            }
        });
    });
});

// Forgot Password (Stub)
app.post('/recuperar', (req, res) => {
    const { email } = req.body;
    // Implement email sending logic here (Nodemailer, etc.)
    console.log(`Password reset requested for: ${email}`);

    // Check if email exists
    db.query('SELECT id FROM users WHERE email = ?', [email], (err, results) => {
        if (err) return res.status(500).json({ error: 'Database error' });

        if (results.length > 0) {
            res.json({ message: 'If the email exists, a reset link has been sent.' });
        } else {
            // Security: Don't reveal if email exists or not, simplified for now
            res.json({ message: 'If the email exists, a reset link has been sent.' });
        }
    });

});

// Fallback for SPA or static files
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, '../index.html'));
});

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
