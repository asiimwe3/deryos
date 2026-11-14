import 'dotenv/config';
import express from 'express';
import rateLimit from 'express-rate-limit';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';

/**
 * Derycode Power API — Phase 1 skeleton.
 *
 * Three responsibilities, kept in one place on purpose so the contract is easy
 * to read:
 *   1. Derycode ID        register, sign in, issue tokens
 *   2. Derycode App Market  list published apps, record installs
 *   3. DeryAI gateway     proxy model calls so keys never reach a client
 *
 * Storage is in-memory until DATABASE_URL is provided. The Postgres schema in
 * sql/schema.sql is the target shape, and every handler below already speaks in
 * those records.
 */

const app = express();
app.disable('x-powered-by');
app.use(express.json({ limit: '32kb' }));

const PORT = Number(process.env.PORT || 8787);
const JWT_SECRET = process.env.JWT_SECRET || '';

if (!JWT_SECRET && process.env.NODE_ENV === 'production') {
  console.error('Refusing to start: JWT_SECRET is required in production.');
  process.exit(1);
}
const secret = JWT_SECRET || 'development-only-secret-do-not-ship';

/* ── storage (in-memory prototype) ───────────────────────────── */
const accounts = new Map(); // email -> account
const devices = [];
const installs = new Set(); // `${accountId}:${appId}`

const MARKET = [
  { id: 'derycode-studio', name: 'DeryCode Studio', developer: 'Derycode Technologies', summary: 'Build and ship DeryOS apps', version: '1.0.0', published: true },
  { id: 'sacco-wallet', name: 'Sacco Wallet', developer: 'Derycode Technologies', summary: 'Farmer wallet for Kyenjojo', version: '2.1.0', published: true },
  { id: 'school-sync', name: 'School Sync', developer: 'Derycode Technologies', summary: 'School management suite', version: '3.0.2', published: true },
  { id: 'derycare', name: 'DeryCare', developer: 'Derycode Technologies', summary: 'Clinic records and billing', version: '1.4.0', published: true }
];

/* ── helpers ─────────────────────────────────────────────────── */
const makeId = () => 'DC-' + Math.floor(100000 + Math.random() * 899999);

function sign(account) {
  return jwt.sign({ sub: account.id, did: account.derycodeId }, secret, { expiresIn: '12h' });
}

function auth(req, res, next) {
  const header = String(req.headers.authorization || '');
  const token = header.startsWith('Bearer ') ? header.slice(7) : '';
  if (!token) return res.status(401).json({ error: 'missing_token' });
  try {
    req.account = jwt.verify(token, secret);
    next();
  } catch {
    res.status(401).json({ error: 'invalid_token' });
  }
}

const publicAccount = (a) => ({
  id: a.id,
  derycodeId: a.derycodeId,
  email: a.email,
  displayName: a.displayName,
  createdAt: a.createdAt
});

/* ── 1. Derycode ID ──────────────────────────────────────────── */
app.post('/v1/auth/register', rateLimit({ windowMs: 60_000, max: 10 }), async (req, res) => {
  const { email, password, displayName } = req.body || {};
  if (!email || !password) return res.status(400).json({ error: 'email_and_password_required' });
  if (String(password).length < 10) return res.status(400).json({ error: 'password_too_short', minimum: 10 });
  if (accounts.has(String(email).toLowerCase())) return res.status(409).json({ error: 'email_already_registered' });

  const account = {
    id: crypto.randomUUID(),
    derycodeId: makeId(),
    email: String(email).toLowerCase(),
    displayName: displayName || null,
    passwordHash: await bcrypt.hash(String(password), 12),
    createdAt: new Date().toISOString()
  };
  accounts.set(account.email, account);
  res.status(201).json({ account: publicAccount(account), token: sign(account) });
});

app.post('/v1/auth/login', rateLimit({ windowMs: 60_000, max: 10 }), async (req, res) => {
  const { email, password } = req.body || {};
  const account = accounts.get(String(email || '').toLowerCase());
  // constant-ish response shape so the endpoint does not reveal which emails exist
  if (!account) return res.status(401).json({ error: 'invalid_credentials' });
  const ok = await bcrypt.compare(String(password || ''), account.passwordHash);
  if (!ok) return res.status(401).json({ error: 'invalid_credentials' });
  res.json({ account: publicAccount(account), token: sign(account) });
});

app.get('/v1/profile', auth, (req, res) => {
  const account = [...accounts.values()].find((a) => a.id === req.account.sub);
  if (!account) return res.status(404).json({ error: 'account_not_found' });
  res.json({
    account: publicAccount(account),
    devices: devices.filter((d) => d.accountId === account.id),
    installed: [...installs].filter((k) => k.startsWith(account.id + ':')).map((k) => k.split(':')[1])
  });
});

/* ── devices ─────────────────────────────────────────────────── */
app.post('/v1/devices', auth, (req, res) => {
  const { deviceName, platform, build } = req.body || {};
  if (!deviceName || !['mobile', 'desktop', 'tablet'].includes(platform)) {
    return res.status(400).json({ error: 'device_name_and_platform_required' });
  }
  const device = {
    id: crypto.randomUUID(),
    accountId: req.account.sub,
    deviceName,
    platform,
    build: build || null,
    lastSeenAt: new Date().toISOString()
  };
  devices.push(device);
  res.status(201).json({ device });
});

/* ── 2. Derycode App Market ──────────────────────────────────── */
app.get('/v1/market/apps', (_req, res) => {
  res.json({ apps: MARKET.filter((a) => a.published), count: MARKET.filter((a) => a.published).length });
});

app.post('/v1/market/install', auth, (req, res) => {
  const { appId } = req.body || {};
  if (!MARKET.some((a) => a.id === appId)) return res.status(404).json({ error: 'app_not_found' });
  installs.add(req.account.sub + ':' + appId);
  res.json({ ok: true, appId });
});

/* ── 3. DeryAI gateway ───────────────────────────────────────── */
app.post('/v1/ai/chat', auth, rateLimit({ windowMs: 60_000, max: 20 }), async (req, res) => {
  const { message } = req.body || {};
  if (!message || String(message).length > 4000) return res.status(400).json({ error: 'message_required' });

  // No provider configured yet: answer locally so the interface still works.
  if (!process.env.AI_PROVIDER_URL || !process.env.AI_API_KEY) {
    return res.json({
      reply: 'DeryAI is running in offline mode on this build. Configure AI_PROVIDER_URL and AI_API_KEY on the server to enable the model.',
      mode: 'offline'
    });
  }

  try {
    const upstream = await fetch(process.env.AI_PROVIDER_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${process.env.AI_API_KEY}`
      },
      body: JSON.stringify({ model: process.env.AI_MODEL, input: message })
    });
    if (!upstream.ok) return res.status(502).json({ error: 'ai_upstream_error' });
    const data = await upstream.json();
    res.json({ reply: data.output_text || data.reply || '', mode: 'live' });
  } catch {
    res.status(502).json({ error: 'ai_unreachable' });
  }
});

/* ── health + errors ─────────────────────────────────────────── */
app.get('/health', (_req, res) => {
  res.json({
    ok: true,
    service: 'deryos-api',
    version: '0.1.0',
    accounts: accounts.size,
    devices: devices.length,
    ai: process.env.AI_API_KEY ? 'configured' : 'offline'
  });
});

app.use((req, res) => res.status(404).json({ error: 'not_found', path: req.path }));
app.use((err, _req, res, _next) => {
  console.error(err);
  res.status(500).json({ error: 'server_error' });
});

app.listen(PORT, () => {
  console.log(`Derycode Power API listening on :${PORT}`);
});
