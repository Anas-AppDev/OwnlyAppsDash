const express = require('express');
const path = require('path');
const cors = require('cors');
const compression = require('compression');

const app = express();
const PORT = process.env.PORT || 8600;
const buildPath = path.join(__dirname, '../composeApp/build/dist/wasmJs/productionExecutable');

app.use(compression());
app.use(cors());

app.use((req, res, next) => {
  if (req.url.endsWith('.wasm')) {
    res.setHeader('Content-Type', 'application/wasm');
  }
  if (req.url.endsWith('.mjs')) {
    res.setHeader('Content-Type', 'application/javascript');
  }
  next();
});

app.get('/health', (req, res) => {
  res.json({
    status: 'OK',
    timestamp: new Date().toISOString(),
    buildPath,
  });
});

app.use(express.static(buildPath, { dotfiles: 'allow' }));

app.get('*', (req, res) => {
  if (req.url.includes('.')) {
    res.status(404).send('File not found');
    return;
  }

  res.sendFile(path.join(buildPath, 'index.html'), (err) => {
    if (err) {
      res.status(404).send(`
        <h1>OwnlyAppsDash Server</h1>
        <p>Build files not found. Please run the WASM build first:</p>
        <pre>./build-wasm.sh</pre>
        <p>Expected build output at: ${buildPath}</p>
      `);
    }
  });
});

app.listen(PORT, () => {
  console.log(`OwnlyAppsDash server running on http://localhost:${PORT}`);
  console.log(`Serving files from: ${buildPath}`);
});
