import fs from "fs";
import path from "path";
import { v4 as uuid } from "uuid";
import { annotateImage } from "../utils/annotate.js";

const ORIGINAL = "data/original";
const LATEST = "data/latest";

export async function uploadForPatient(req, res) {
  const { patientId } = req.params;
  const imageId = uuid();

  const filePath = path.join(ORIGINAL, `${imageId}.png`);
  fs.writeFileSync(filePath, req.file.buffer);

  fs.copyFileSync(filePath, path.join(LATEST, `${imageId}.png`));

  res.json({ id: imageId, patientId });
}

export async function annotate(req, res) {
  const { imageId } = req.params;

  await annotateImage(
    path.join(ORIGINAL, `${imageId}.png`),
    path.join(LATEST, `${imageId}.png`),
    req.body
  );

  res.json({ ok: true });
}

export function getLatest(req, res) {
  const { patientId } = req.params;

  const files = fs.readdirSync(LATEST);
  if (!files.length) return res.status(404).end();

  const latest = files[files.length - 1];
  res.sendFile(path.resolve(LATEST, latest));
}
