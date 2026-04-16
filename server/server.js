const express = require('express');
const app = express();
const dotenv = require('dotenv');
const cors = require('cors');

dotenv.config();
app.use(cors());
app.use(express.json());

const authRoutes = require('./routes/auth');
const statsRoutes = require('./routes/stats');

app.use('/auth', authRoutes);
app.use('/stats', statsRoutes);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server listening on port ${PORT}`));
