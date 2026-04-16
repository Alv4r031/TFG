const express = require('express');
const router = express.Router();
const pool = require('../db');
const bcrypt = require('bcryptjs');

// Registro
router.post('/register', async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) return res.status(400).json({ error: 'Faltan campos' });
  try {
    const [rows] = await pool.query('SELECT id FROM users WHERE username = ? LIMIT 1', [username]);
    if (rows.length) return res.status(409).json({ error: 'Usuario ya existe' });
    const hash = await bcrypt.hash(password, 10);
    const [result] = await pool.query('INSERT INTO users (username, password_hash) VALUES (?, ?)', [username, hash]);
    return res.json({ id: result.insertId, username });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ error: 'Error interno' });
  }
});

// Login (devuelve id y username si ok)
router.post('/login', async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) return res.status(400).json({ error: 'Faltan campos' });
  try {
    const [rows] = await pool.query('SELECT id, password_hash FROM users WHERE username = ? LIMIT 1', [username]);
    if (!rows.length) return res.status(401).json({ error: 'Usuario o contraseña inválidos' });
    const user = rows[0];
    const match = await bcrypt.compare(password, user.password_hash);
    if (!match) return res.status(401).json({ error: 'Usuario o contraseña inválidos' });
    return res.json({ id: user.id, username });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ error: 'Error interno' });
  }
});

module.exports = router;
