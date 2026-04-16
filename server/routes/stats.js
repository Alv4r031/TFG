const express = require('express');
const router = express.Router();
const pool = require('../db');

// Actualizar estadísticas: body { user_id, game_key, won: boolean }
router.post('/update', async (req, res) => {
  const { user_id, game_key, won } = req.body;
  if (!user_id || !game_key) return res.status(400).json({ error: 'Faltan campos' });
  const winVal = won ? 1 : 0;
  try {
    const sql = `INSERT INTO user_stats (user_id, game_key, plays, wins)
                 VALUES (?, ?, 1, ?)
                 ON DUPLICATE KEY UPDATE plays = plays + 1, wins = wins + VALUES(wins), updated_at = CURRENT_TIMESTAMP`;
    await pool.query(sql, [user_id, game_key, winVal]);
    return res.json({ ok: true });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ error: 'Error interno' });
  }
});

// Obtener estadísticas de un usuario
router.get('/:userId', async (req, res) => {
  const userId = req.params.userId;
  try {
    const sql = `SELECT game_key, plays, wins FROM user_stats WHERE user_id = ?`;
    const [rows] = await pool.query(sql, [userId]);
    return res.json(rows);
  } catch (err) {
    console.error(err);
    return res.status(500).json({ error: 'Error interno' });
  }
});

module.exports = router;
