// server.js — FÄRDIG, FUNGERANDE, UPPFYLLER KRAVEN
// - Endast doctor kan ladda upp & annotera
// - Bilder kopplas till patientId
// - Patient ser endast sina egna bilder
// - JWT verifieras mot Keycloak (JWKS), K8s-säkert

const express = require("express");
const cors = require("cors");
const multer = require("multer");
const sharp = require("sharp");
const { randomUUID } = require("crypto");
const fs = require("fs");
const path = require("path");
const { createRemoteJWKSet, jwtVerify } = require("jose");

const app = express();
app.use(cors());
app.use(express.json({ limit: "10mb" }));

// =====================
// Storage (patient-bundet)
// =====================
const DATA_DIR = process.env.DATA_DIR || path.join(__dirname, "data");
const ORIG_DIR = path.join(DATA_DIR, "original");
const LATEST_DIR = path.join(DATA_DIR, "latest");

function ensureDir(p) { fs.mkdirSync(p, { recursive: true }); }
ensureDir(ORIG_DIR);
ensureDir(LATEST_DIR);

function patientDir(base, patientId) {
  const d = path.join(base, String(patientId));
  ensureDir(d);
  return d;
}
function filePath(dir, id) {
  return path.join(dir, `${id}.png`);
}

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 },
});

// =====================
// Keycloak JWT (K8s-safe)
// =====================
const KEYCLOAK_JWKS_REALM_URL =
  process.env.KEYCLOAK_JWKS_REALM_URL || "http://keycloak:8080/realms/clinic";
const KEYCLOAK_ISSUER =
  process.env.KEYCLOAK_ISSUER || "http://localhost:30080/realms/clinic";

const JWKS = createRemoteJWKSet(
  new URL(`${KEYCLOAK_JWKS_REALM_URL}/protocol/openid-connect/certs`)
);

async function verify(req) {
  const auth = req.header("Authorization");
  if (!auth?.startsWith("Bearer ")) throw new Error("Missing token");
  const token = auth.substring("Bearer ".length);
  const { payload } = await jwtVerify(token, JWKS);
  if (payload?.iss !== KEYCLOAK_ISSUER) throw new Error("Bad issuer");
  return payload;
}

async function requireDoctor(req, res, next) {
  try {
    const payload = await verify(req);
    const roles = payload?.realm_access?.roles || [];
    if (!roles.includes("doctor")) return res.status(403).end();
    req.user = payload;
    next();
  } catch (e) {
    res.status(401).send(e.message);
  }
}

async function requirePatientOrDoctor(req, res, next) {
  try {
    const payload = await verify(req);
    const roles = payload?.realm_access?.roles || [];
    const isDoctor = roles.includes("doctor");
    const isPatient = roles.includes("patient");
    if (!isDoctor && !isPatient) return res.status(403).end();

    // patient får bara se sina egna
    if (isPatient) {
      const patientId = String(req.params.patientId);
      if (payload.sub !== patientId) return res.status(403).end();
    }

    req.user = payload;
    next();
  } catch (e) {
    res.status(401).send(e.message);
  }
}

// =====================
// SVG overlay (text + strokes)
// =====================
function buildOverlaySvg(width, height, texts = [], strokes = []) {
  const esc = (s) =>
    String(s).replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));

  const textEls = texts.map(t => {
    const size = Number(t.size || 24);
    const x = Number(t.x || 0);
    const y = Number(t.y || 0);
    const color = t.color || "yellow";
    return `<text x="${x}" y="${y}" fill="${color}" font-size="${size}" font-family="Arial" font-weight="bold">${esc(t.text || "")}</text>`;
  }).join("");

  const strokeEls = strokes.map(s => {
    const color = s.color || "red";
    const w = Number(s.width || 4);
    const pts = Array.isArray(s.points) ? s.points : [];
    if (pts.length < 2) return "";
    const d = pts.map((p, i) => `${i === 0 ? "M" : "L"} ${Number(p.x)} ${Number(p.y)}`).join(" ");
    return `<path d="${d}" fill="none" stroke="${color}" stroke-width="${w}" stroke-linecap="round" stroke-linejoin="round" />`;
  }).join("");

  return `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}">${strokeEls}${textEls}</svg>`;
}

// =====================
// 1) Upload (doctor only) — patient-bundet
// =====================
app.post("/api/images/patients/:patientId/upload", requireDoctor, upload.single("file"), async (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ error: "file is required" });
    const { patientId } = req.params;

    const id = randomUUID();
    const png = await sharp(req.file.buffer).png().toBuffer();

    const oDir = patientDir(ORIG_DIR, patientId);
    const lDir = patientDir(LATEST_DIR, patientId);

    fs.writeFileSync(filePath(oDir, id), png);
    fs.writeFileSync(filePath(lDir, id), png);

    res.json({ id });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

// =====================
// 2) Get latest (patient sees own, doctor sees all)
// =====================
app.get("/api/images/patients/:patientId/latest", requirePatientOrDoctor, (req, res) => {
  const { patientId } = req.params;
  const dir = patientDir(LATEST_DIR, patientId);
  const files = fs.readdirSync(dir).filter(f => f.endsWith(".png"));
  if (!files.length) return res.status(404).end();

  files.sort((a, b) => fs.statSync(path.join(dir, b)).mtimeMs - fs.statSync(path.join(dir, a)).mtimeMs);
  const p = path.join(dir, files[0]);
  res.type("png").send(fs.readFileSync(p));
});

// =====================
// 3) Annotate (doctor only)
// =====================
app.post("/api/images/:imageId/annotate", requireDoctor, async (req, res) => {
  try {
    const { imageId } = req.params;
    const { patientId, texts = [], strokes = [] } = req.body || {};
    if (!patientId) return res.status(400).send("patientId required");

    const latestPath = filePath(patientDir(LATEST_DIR, patientId), imageId);
    if (!fs.existsSync(latestPath)) return res.status(404).end();

    const base = sharp(latestPath);
    const meta = await base.metadata();
    const w = meta.width || 800;
    const h = meta.height || 600;

    const svg = buildOverlaySvg(w, h, texts, strokes);
    const out = await base.composite([{ input: Buffer.from(svg), top: 0, left: 0 }]).png().toBuffer();
    fs.writeFileSync(latestPath, out);

    res.json({ ok: true });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

const port = process.env.PORT || 8080;
app.listen(port, () => console.log(`image-service listening on ${port}`));
